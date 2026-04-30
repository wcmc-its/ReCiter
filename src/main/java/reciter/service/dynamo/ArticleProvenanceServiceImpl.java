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
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

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
}
