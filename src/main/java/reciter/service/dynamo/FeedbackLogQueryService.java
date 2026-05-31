package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

/**
 * Read-side service for curation audit history (Phase 34).
 *
 * <p>Queries the {@code FeedbackLog} table (curator accept/reject/pending actions)
 * by {@code uid} and enriches each row with the matching {@code ArticleProvenance}
 * record (how the PMID first arrived). Uses raw {@code AmazonDynamoDB.query},
 * mirroring the write side ({@link FeedbackLogServiceImpl},
 * {@link ArticleProvenanceServiceImpl}).
 */
@Service
public class FeedbackLogQueryService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackLogQueryService.class);
    private static final String FEEDBACK_LOG_TABLE = "FeedbackLog";
    private static final String ARTICLE_PROVENANCE_TABLE = "ArticleProvenance";
    private static final int MAX_ROWS = 2000;

    private final AmazonDynamoDB amazonDynamoDB;

    @Autowired
    public FeedbackLogQueryService(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    /**
     * Return the curation audit history for a uid, newest action first, each row
     * enriched with the article's retrieval provenance. Capped at {@value #MAX_ROWS}
     * rows (logged if truncated).
     */
    public List<AuditHistoryEntry> getAuditHistory(String uid) {
        List<AuditHistoryEntry> entries = new ArrayList<>();
        if (uid == null || uid.isEmpty()) {
            return entries;
        }

        Map<String, ProvenanceInfo> provenance = loadProvenance(uid);

        Map<String, AttributeValue> lastKey = null;
        do {
            QueryRequest req = new QueryRequest()
                    .withTableName(FEEDBACK_LOG_TABLE)
                    .withKeyConditionExpression("uid = :uid")
                    .addExpressionAttributeValuesEntry(":uid", new AttributeValue().withS(uid))
                    .withScanIndexForward(false); // newest sk (highest epoch) first
            if (lastKey != null) {
                req.setExclusiveStartKey(lastKey);
            }
            QueryResult result = amazonDynamoDB.query(req);
            for (Map<String, AttributeValue> item : result.getItems()) {
                entries.add(toEntry(item, provenance));
                if (entries.size() >= MAX_ROWS) {
                    log.warn("FeedbackLog audit history for uid={} truncated at {} rows", uid, MAX_ROWS);
                    return entries;
                }
            }
            lastKey = result.getLastEvaluatedKey();
        } while (lastKey != null && !lastKey.isEmpty());

        return entries;
    }

    private AuditHistoryEntry toEntry(Map<String, AttributeValue> item, Map<String, ProvenanceInfo> provenance) {
        AuditHistoryEntry e = new AuditHistoryEntry();
        e.articleId = getS(item, "articleId");
        e.feedback = getS(item, "feedback");
        e.curatedBy = getNInt(item, "curatedBy");
        e.createTimestamp = getNLong(item, "createTimestamp");
        e.sk = getS(item, "sk");
        e.src = getS(item, "src");
        ProvenanceInfo p = (e.articleId != null) ? provenance.get(e.articleId) : null;
        if (p != null) {
            e.provenanceRs = p.rs;
            e.provenanceFrd = p.frd;
            e.provenanceSrc = p.src;
        }
        return e;
    }

    /** Load all ArticleProvenance rows for a uid into an articleId -> provenance map. */
    private Map<String, ProvenanceInfo> loadProvenance(String uid) {
        Map<String, ProvenanceInfo> map = new HashMap<>();
        try {
            Map<String, AttributeValue> lastKey = null;
            do {
                QueryRequest req = new QueryRequest()
                        .withTableName(ARTICLE_PROVENANCE_TABLE)
                        .withKeyConditionExpression("uid = :uid")
                        .addExpressionAttributeValuesEntry(":uid", new AttributeValue().withS(uid));
                if (lastKey != null) {
                    req.setExclusiveStartKey(lastKey);
                }
                QueryResult result = amazonDynamoDB.query(req);
                for (Map<String, AttributeValue> item : result.getItems()) {
                    String articleId = getS(item, "articleId");
                    if (articleId == null) {
                        continue;
                    }
                    ProvenanceInfo p = new ProvenanceInfo();
                    p.rs = getS(item, "rs");
                    p.frd = item.containsKey("frd") ? Long.valueOf(getNLong(item, "frd")) : null;
                    p.src = getS(item, "src");
                    map.put(articleId, p);
                }
                lastKey = result.getLastEvaluatedKey();
            } while (lastKey != null && !lastKey.isEmpty());
        } catch (Exception e) {
            // Provenance is enrichment only; never fail the audit-history read on it.
            log.warn("Failed to load ArticleProvenance for uid={}: {}", uid, e.getMessage());
        }
        return map;
    }

    private static String getS(Map<String, AttributeValue> item, String key) {
        AttributeValue v = item.get(key);
        return (v == null) ? null : v.getS();
    }

    private static int getNInt(Map<String, AttributeValue> item, String key) {
        AttributeValue v = item.get(key);
        if (v == null || v.getN() == null) {
            return 0;
        }
        try {
            return Integer.parseInt(v.getN());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long getNLong(Map<String, AttributeValue> item, String key) {
        AttributeValue v = item.get(key);
        if (v == null || v.getN() == null) {
            return 0L;
        }
        try {
            return Long.parseLong(v.getN());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static class ProvenanceInfo {
        String rs;
        Long frd;
        String src;
    }
}
