package reciter.service.dynamo;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import reciter.service.FeedbackLogService;

/**
 * Phase 33-02 implementation of {@link FeedbackLogService}.
 *
 * <p>Uses raw {@code AmazonDynamoDB.putItem} with {@code attribute_not_exists(sk)}
 * for idempotency. The sk is {@code "<epoch_seconds>#<nano-derived-hex>"} which
 * gives microsecond-resolution uniqueness within a single process; cross-process
 * collisions are caught by the conditional and retried with a fresh suffix.
 *
 * <p>{@code src='MAN'} on every row — Phase 32 D-03: PM UI is the only source for
 * this table going forward (CTSC ingestion bypasses PM and writes directly to
 * GoldStandard).
 */
@Service
public class FeedbackLogServiceImpl implements FeedbackLogService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackLogServiceImpl.class);
    private static final String TABLE_NAME = "FeedbackLog";
    private static final String SRC_MAN = "MAN";
    private static final int SK_RETRY_LIMIT = 5;

    private final AmazonDynamoDB amazonDynamoDB;

    public FeedbackLogServiceImpl(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void recordAction(String uid, long pmid, Feedback feedback, int curatedBy, long actionEpochSeconds) {
        if (uid == null || uid.isEmpty()) {
            log.warn("recordAction called with null/empty uid; skipping (pmid={})", pmid);
            return;
        }
        if (feedback == null) {
            log.warn("recordAction called with null feedback; skipping (uid={} pmid={})", uid, pmid);
            return;
        }

        for (int attempt = 0; attempt < SK_RETRY_LIMIT; attempt++) {
            String sk = buildSk(actionEpochSeconds);
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("uid", new AttributeValue().withS(uid));
            item.put("sk", new AttributeValue().withS(sk));
            item.put("articleId", new AttributeValue().withS(String.valueOf(pmid)));
            item.put("feedback", new AttributeValue().withS(feedback.name()));
            item.put("curatedBy", new AttributeValue().withN(String.valueOf(curatedBy)));
            item.put("src", new AttributeValue().withS(SRC_MAN));
            item.put("createTimestamp", new AttributeValue().withN(String.valueOf(actionEpochSeconds)));
            item.put("modifyTimestamp", new AttributeValue().withN(String.valueOf(actionEpochSeconds)));

            PutItemRequest req = new PutItemRequest()
                    .withTableName(TABLE_NAME)
                    .withItem(item)
                    .withConditionExpression("attribute_not_exists(sk)");

            try {
                amazonDynamoDB.putItem(req);
                return;
            } catch (ConditionalCheckFailedException race) {
                // sk collision (extremely unlikely with 8 hex chars); retry with fresh suffix
                log.info("FeedbackLog sk collision for uid={} sk={}; retrying (attempt {})",
                        uid, sk, attempt + 1);
            } catch (AmazonDynamoDBException e) {
                log.warn("FeedbackLog write failed for uid={} pmid={}: {}", uid, pmid, e.getMessage());
                return;
            }
        }
        log.warn("FeedbackLog write hit retry limit for uid={} pmid={}; giving up", uid, pmid);
    }

    /**
     * Build a unique sk: epoch + '#' + last 8 hex digits of nanoTime.
     * Within a single JVM, nanoTime is monotonically increasing, so collisions
     * within a process are vanishingly rare. Cross-process collisions are caught
     * by the conditional put.
     */
    private String buildSk(long epochSeconds) {
        long nano = System.nanoTime();
        // Mask to 32 bits (8 hex digits)
        String hexSuffix = String.format("%08x", (int) (nano & 0xFFFFFFFFL));
        return epochSeconds + "#" + hexSuffix;
    }
}
