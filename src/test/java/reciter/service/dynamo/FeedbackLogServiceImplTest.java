package reciter.service.dynamo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import reciter.service.FeedbackLogService;
import reciter.service.FeedbackLogService.Feedback;

/**
 * Unit tests for {@link FeedbackLogServiceImpl}.
 *
 * <p>Covers Phase 32 D-01 (schema), D-02 (sk format), D-03 (src=MAN), D-04 (feedback enum),
 * and Phase 33-02 idempotency / retry semantics.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedbackLogServiceImplTest {

    @Mock
    private AmazonDynamoDB amazonDynamoDB;

    private FeedbackLogServiceImpl service;

    @Before
    public void setUp() {
        service = new FeedbackLogServiceImpl(amazonDynamoDB);
    }

    private PutItemRequest captureRequest() {
        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(amazonDynamoDB).putItem(captor.capture());
        return captor.getValue();
    }

    @Test
    public void testRecordAction_TableNameIsFeedbackLog() {
        service.recordAction("ajg9004", 12345L, Feedback.ACCEPTED, 3, 1700000000L);
        assertEquals("FeedbackLog", captureRequest().getTableName());
    }

    @Test
    public void testRecordAction_ItemHasFullSchema() {
        service.recordAction("ajg9004", 12345L, Feedback.ACCEPTED, 3, 1700000000L);
        Map<String, AttributeValue> item = captureRequest().getItem();
        assertEquals("ajg9004", item.get("uid").getS());
        assertEquals("12345", item.get("articleId").getS());
        assertEquals("ACCEPTED", item.get("feedback").getS());
        assertEquals("3", item.get("curatedBy").getN());
        assertEquals("MAN", item.get("src").getS());
        assertEquals("1700000000", item.get("createTimestamp").getN());
        assertEquals("1700000000", item.get("modifyTimestamp").getN());
        assertNotNull(item.get("sk"));
    }

    @Test
    public void testRecordAction_SkFormat_EpochHashHex() {
        service.recordAction("uid1", 1L, Feedback.ACCEPTED, 0, 1700000000L);
        String sk = captureRequest().getItem().get("sk").getS();
        // Format: "<epoch>#<8-hex>"
        assertTrue("sk should match epoch#hex format: " + sk,
                sk.matches("^\\d+#[0-9a-f]{8}$"));
        assertTrue("sk should start with the epoch: " + sk,
                sk.startsWith("1700000000#"));
    }

    @Test
    public void testRecordAction_ConditionExpression_PreventsCollision() {
        service.recordAction("uid1", 1L, Feedback.ACCEPTED, 0, 1700000000L);
        assertEquals("attribute_not_exists(sk)", captureRequest().getConditionExpression());
    }

    @Test
    public void testRecordAction_FeedbackRejected_WritesRejected() {
        service.recordAction("uid1", 1L, Feedback.REJECTED, 0, 1700000000L);
        assertEquals("REJECTED", captureRequest().getItem().get("feedback").getS());
    }

    @Test
    public void testRecordAction_FeedbackPending_WritesPending() {
        service.recordAction("uid1", 1L, Feedback.PENDING, 0, 1700000000L);
        assertEquals("PENDING", captureRequest().getItem().get("feedback").getS());
    }

    @Test
    public void testRecordAction_SkCollision_RetriesWithFreshSuffix() {
        when(amazonDynamoDB.putItem(any(PutItemRequest.class)))
                .thenThrow(new ConditionalCheckFailedException("collision"))
                .thenReturn(null);  // second attempt succeeds

        service.recordAction("uid1", 1L, Feedback.ACCEPTED, 0, 1700000000L);
        verify(amazonDynamoDB, times(2)).putItem(any(PutItemRequest.class));
    }

    @Test
    public void testRecordAction_DynamoDbException_LoggedNotThrown() {
        when(amazonDynamoDB.putItem(any(PutItemRequest.class)))
                .thenThrow(new AmazonDynamoDBException("simulated"));

        service.recordAction("uid1", 1L, Feedback.ACCEPTED, 0, 1700000000L);
        // Single attempt (non-CCE exceptions are not retried)
        verify(amazonDynamoDB, times(1)).putItem(any(PutItemRequest.class));
    }

    @Test
    public void testRecordAction_NullUid_SkipsCallEntirely() {
        service.recordAction(null, 1L, Feedback.ACCEPTED, 0, 1700000000L);
        verify(amazonDynamoDB, never()).putItem(any(PutItemRequest.class));
    }

    @Test
    public void testRecordAction_EmptyUid_SkipsCallEntirely() {
        service.recordAction("", 1L, Feedback.ACCEPTED, 0, 1700000000L);
        verify(amazonDynamoDB, never()).putItem(any(PutItemRequest.class));
    }

    @Test
    public void testRecordAction_NullFeedback_SkipsCallEntirely() {
        service.recordAction("uid1", 1L, null, 0, 1700000000L);
        verify(amazonDynamoDB, never()).putItem(any(PutItemRequest.class));
    }
}
