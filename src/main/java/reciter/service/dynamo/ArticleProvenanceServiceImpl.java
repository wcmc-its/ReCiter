package reciter.service.dynamo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

import reciter.feedback.EntryPath;
import reciter.service.ArticleProvenanceService;

/**
 * Phase 33-01 implementation of {@link ArticleProvenanceService}.
 *
 * <p>Uses raw {@code AmazonDynamoDB.updateItem} so the {@code UpdateExpression} can
 * combine {@code if_not_exists(...)} with {@code ADD ads :strategySet} in a single
 * request. {@link com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper}
 * does not support this expression shape directly.
 *
 * <p>Single-round-trip semantics: no read-then-write, no race; concurrent retrieval
 * strategies on the same PMID are safe because String Set ADD is commutative and
 * {@code if_not_exists} is atomic per UpdateItem.
 */
@Service
public class ArticleProvenanceServiceImpl implements ArticleProvenanceService {

    private static final Logger log = LoggerFactory.getLogger(ArticleProvenanceServiceImpl.class);
    private static final String TABLE_NAME = "ArticleProvenance";
    private static final String PM_UI_SEARCH_RS = "PM_UI_SEARCH";
    private static final String SRC_PM = "PM";
    private static final String SRC_CTSC = "CTSC";
    private static final String SRC_GS_PLACEHOLDER = "GS";
    private static final String SRC_MAN = "MAN";
    private static final String SRC_MAN_FROM_PM = "MAN_FROM_PM";
    private static final String SRC_MAN_FROM_CTSC = "MAN_FROM_CTSC";

    private final AmazonDynamoDB amazonDynamoDB;

    public ArticleProvenanceServiceImpl(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void upsertRetrievalProvenance(String uid, long pmid, String strategyCode, long epochSeconds) {
        if (uid == null || uid.isEmpty()) {
            log.warn("upsertRetrievalProvenance called with null/empty uid; skipping (pmid={})", pmid);
            return;
        }
        if (strategyCode == null || strategyCode.isEmpty()) {
            log.warn("upsertRetrievalProvenance called with null/empty strategyCode; skipping (uid={} pmid={})",
                    uid, pmid);
            return;
        }

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uid", new AttributeValue().withS(uid));
        key.put("articleId", new AttributeValue().withS(String.valueOf(pmid)));

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":strategy", new AttributeValue().withS(strategyCode));
        values.put(":now", new AttributeValue().withN(String.valueOf(epochSeconds)));
        values.put(":strategySet", new AttributeValue().withSS(Collections.singletonList(strategyCode)));

        UpdateItemRequest req = new UpdateItemRequest()
                .withTableName(TABLE_NAME)
                .withKey(key)
                .withUpdateExpression(
                        "SET rs = if_not_exists(rs, :strategy), " +
                        "    frd = if_not_exists(frd, :now) " +
                        "ADD ads :strategySet")
                .withExpressionAttributeValues(values);

        try {
            amazonDynamoDB.updateItem(req);
        } catch (AmazonDynamoDBException e) {
            // Provenance failures must not break retrieval. Log and continue.
            log.warn("ArticleProvenance upsert failed for uid={} pmid={} strategy={}: {}",
                    uid, pmid, strategyCode, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("ArticleProvenance upsert unexpected error for uid={} pmid={} strategy={}: {}",
                    uid, pmid, strategyCode, e.getMessage());
        }
    }

    @Override
    public void upsertCuratorAction(String uid, long pmid, EntryPath entryPath, long epochSeconds) {
        if (uid == null || uid.isEmpty()) {
            log.warn("upsertCuratorAction called with null/empty uid; skipping (pmid={})", pmid);
            return;
        }
        EntryPath path = (entryPath == null) ? EntryPath.CANDIDATE_LIST : entryPath;

        // D-13: PUBMED_SEARCH path writes a retrieval-style record FIRST so D-11
        // sees src='PM' and lifts to MAN_FROM_PM. CANDIDATE_LIST skips this step.
        if (path == EntryPath.PUBMED_SEARCH) {
            writePmUiSearchRecord(uid, pmid, epochSeconds);
        }

        // D-11 transition with one retry on race
        try {
            applyD11Transition(uid, pmid, epochSeconds, /*allowRetry=*/ true);
        } catch (RuntimeException e) {
            log.warn("D-11 upsert unexpected error for uid={} pmid={} entryPath={}: {}",
                    uid, pmid, path, e.getMessage());
        }
    }

    private void writePmUiSearchRecord(String uid, long pmid, long epochSeconds) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uid", new AttributeValue().withS(uid));
        key.put("articleId", new AttributeValue().withS(String.valueOf(pmid)));

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":rs", new AttributeValue().withS(PM_UI_SEARCH_RS));
        values.put(":pm", new AttributeValue().withS(SRC_PM));
        values.put(":ts", new AttributeValue().withN(String.valueOf(epochSeconds)));
        values.put(":rsSet", new AttributeValue().withSS(Collections.singletonList(PM_UI_SEARCH_RS)));

        UpdateItemRequest req = new UpdateItemRequest()
                .withTableName(TABLE_NAME)
                .withKey(key)
                .withUpdateExpression(
                        "SET rs  = if_not_exists(rs,  :rs), " +
                        "    src = if_not_exists(src, :pm), " +
                        "    frd = if_not_exists(frd, :ts) " +
                        "ADD ads :rsSet")
                .withExpressionAttributeValues(values);

        try {
            amazonDynamoDB.updateItem(req);
        } catch (AmazonDynamoDBException e) {
            log.warn("PM_UI_SEARCH retrieval-record write failed for uid={} pmid={}: {}",
                    uid, pmid, e.getMessage());
        }
    }

    private void applyD11Transition(String uid, long pmid, long epochSeconds, boolean allowRetry) {
        // 1. Read current src
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uid", new AttributeValue().withS(uid));
        key.put("articleId", new AttributeValue().withS(String.valueOf(pmid)));

        GetItemRequest getReq = new GetItemRequest()
                .withTableName(TABLE_NAME)
                .withKey(key)
                .withProjectionExpression("src")
                .withConsistentRead(true);

        GetItemResult getRes;
        try {
            getRes = amazonDynamoDB.getItem(getReq);
        } catch (AmazonDynamoDBException e) {
            log.warn("D-11 read failed for uid={} pmid={}: {}", uid, pmid, e.getMessage());
            return;
        }

        String existingSrc = null;
        if (getRes.getItem() != null && getRes.getItem().containsKey("src")) {
            AttributeValue v = getRes.getItem().get("src");
            existingSrc = v != null ? v.getS() : null;
        }

        String newSrc = computeNewSrc(existingSrc);

        // 2. Build conditional UpdateItem
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":new", new AttributeValue().withS(newSrc));
        values.put(":ts", new AttributeValue().withN(String.valueOf(epochSeconds)));

        UpdateItemRequest req = new UpdateItemRequest()
                .withTableName(TABLE_NAME)
                .withKey(key)
                .withExpressionAttributeValues(values);

        if (existingSrc == null) {
            // New row: write src + frd, condition on src absence
            req.withUpdateExpression("SET src = :new, frd = if_not_exists(frd, :ts)")
               .withConditionExpression("attribute_not_exists(src)");
        } else if (!newSrc.equals(existingSrc)) {
            // Transition: condition on observed existing value to detect concurrent write
            values.put(":existing", new AttributeValue().withS(existingSrc));
            req.withUpdateExpression("SET src = :new, frd = if_not_exists(frd, :ts)")
               .withConditionExpression("src = :existing");
        } else {
            // No-op on src; ensure frd is present
            req.withUpdateExpression("SET frd = if_not_exists(frd, :ts)");
        }

        try {
            amazonDynamoDB.updateItem(req);
        } catch (ConditionalCheckFailedException race) {
            if (allowRetry) {
                log.info("D-11 race for uid={} pmid={} (existing src changed since GetItem); retrying once",
                        uid, pmid);
                applyD11Transition(uid, pmid, epochSeconds, /*allowRetry=*/ false);
            } else {
                log.warn("D-11 retry also failed for uid={} pmid={}; giving up (PM UI request continues)",
                        uid, pmid);
            }
        } catch (AmazonDynamoDBException e) {
            log.warn("D-11 update failed for uid={} pmid={}: {}", uid, pmid, e.getMessage());
        }
    }

    /** D-11 transition table. Package-private for testability. */
    static String computeNewSrc(String existingSrc) {
        if (existingSrc == null || SRC_GS_PLACEHOLDER.equals(existingSrc)) {
            return SRC_MAN;
        }
        if (SRC_PM.equals(existingSrc)) {
            return SRC_MAN_FROM_PM;
        }
        if (SRC_CTSC.equals(existingSrc)) {
            return SRC_MAN_FROM_CTSC;
        }
        // MAN, MAN_FROM_PM, MAN_FROM_CTSC, or any future value: leave as-is
        return existingSrc;
    }
}
