package reciter.service.dynamo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

/**
 * Unit tests for {@link ArticleProvenanceServiceImpl}.
 *
 * <p>Verifies Phase 33-01 D-01..D-04 invariants:
 * <ul>
 *   <li>UpdateExpression uses {@code if_not_exists(rs, ...)} so the first retrieval strategy wins</li>
 *   <li>UpdateExpression uses {@code if_not_exists(frd, ...)} so first-retrieval-date is preserved</li>
 *   <li>UpdateExpression uses {@code ADD ads :strategySet} for additional-strategy tracking</li>
 *   <li>UpdateExpression does NOT touch {@code src} (curator/CTSC paths own it)</li>
 *   <li>DynamoDB exceptions are logged but not thrown (provenance must not break retrieval)</li>
 *   <li>Null/empty {@code uid} or {@code strategyCode} short-circuit safely</li>
 * </ul>
 */
@RunWith(MockitoJUnitRunner.class)
public class ArticleProvenanceServiceImplTest {

    @Mock
    private AmazonDynamoDB amazonDynamoDB;

    private ArticleProvenanceServiceImpl service;

    @Before
    public void setUp() {
        service = new ArticleProvenanceServiceImpl(amazonDynamoDB);
    }

    private UpdateItemRequest captureRequest() {
        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(amazonDynamoDB).updateItem(captor.capture());
        return captor.getValue();
    }

    @Test
    public void testUpsert_TableNameAndKey_MatchSchema() {
        service.upsertRetrievalProvenance("ajg9004", 12345L, "PUBMED_NAME_AFFIL", 1700000000L);

        UpdateItemRequest req = captureRequest();
        assertEquals("ArticleProvenance", req.getTableName());

        Map<String, AttributeValue> key = req.getKey();
        assertEquals("ajg9004", key.get("uid").getS());
        assertEquals("12345", key.get("articleId").getS());
    }

    @Test
    public void testUpsert_PreservesExistingRsFrd_OnRewrite() {
        service.upsertRetrievalProvenance("uid1", 100L, "STRAT_A", 1700000000L);
        UpdateItemRequest req = captureRequest();

        String expr = req.getUpdateExpression();
        assertTrue("UpdateExpression must use if_not_exists(rs, :strategy): " + expr,
                expr.contains("rs = if_not_exists(rs, :strategy)"));
        assertTrue("UpdateExpression must use if_not_exists(frd, :now): " + expr,
                expr.contains("frd = if_not_exists(frd, :now)"));
    }

    @Test
    public void testUpsert_AddsStrategyToAds() {
        service.upsertRetrievalProvenance("uid1", 100L, "STRAT_A", 1700000000L);
        UpdateItemRequest req = captureRequest();

        String expr = req.getUpdateExpression();
        assertTrue("UpdateExpression must contain ADD ads :strategySet: " + expr,
                expr.contains("ADD ads :strategySet"));

        AttributeValue ssVal = req.getExpressionAttributeValues().get(":strategySet");
        assertNotNull(":strategySet must be present", ssVal);
        assertNotNull(":strategySet must be a String Set", ssVal.getSS());
        assertEquals(1, ssVal.getSS().size());
        assertEquals("STRAT_A", ssVal.getSS().get(0));
    }

    @Test
    public void testUpsert_DoesNotTouchSrc() {
        service.upsertRetrievalProvenance("uid1", 100L, "STRAT_A", 1700000000L);
        UpdateItemRequest req = captureRequest();

        String expr = req.getUpdateExpression();
        // Phase 33 D-04: retrieval path never writes src.
        assertTrue("UpdateExpression must not contain 'src': " + expr,
                !expr.contains("src"));
    }

    @Test
    public void testUpsert_ExpressionAttributeValues_HaveCorrectTypes() {
        service.upsertRetrievalProvenance("uid1", 100L, "PUBMED_GRANT", 1700000000L);
        UpdateItemRequest req = captureRequest();

        Map<String, AttributeValue> values = req.getExpressionAttributeValues();
        assertEquals("PUBMED_GRANT", values.get(":strategy").getS());
        assertEquals("1700000000", values.get(":now").getN());
        assertEquals(1, values.get(":strategySet").getSS().size());
        assertEquals("PUBMED_GRANT", values.get(":strategySet").getSS().get(0));
    }

    @Test
    public void testUpsert_DynamoDbExceptionLogged_NotThrown() {
        when(amazonDynamoDB.updateItem(any(UpdateItemRequest.class)))
                .thenThrow(new AmazonDynamoDBException("simulated DDB failure"));

        // Must not throw — provenance failures must not break retrieval.
        service.upsertRetrievalProvenance("uid1", 100L, "STRAT_A", 1700000000L);

        // updateItem was called once and the exception was swallowed.
        verify(amazonDynamoDB).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testUpsert_RuntimeExceptionLogged_NotThrown() {
        when(amazonDynamoDB.updateItem(any(UpdateItemRequest.class)))
                .thenThrow(new IllegalStateException("simulated unexpected failure"));

        // Must not throw — even unexpected runtime exceptions are swallowed.
        service.upsertRetrievalProvenance("uid1", 100L, "STRAT_A", 1700000000L);

        verify(amazonDynamoDB).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testUpsert_NullUid_SkipsCallEntirely() {
        service.upsertRetrievalProvenance(null, 100L, "STRAT_A", 1700000000L);
        verify(amazonDynamoDB, never()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testUpsert_EmptyUid_SkipsCallEntirely() {
        service.upsertRetrievalProvenance("", 100L, "STRAT_A", 1700000000L);
        verify(amazonDynamoDB, never()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testUpsert_NullStrategyCode_SkipsCallEntirely() {
        service.upsertRetrievalProvenance("uid1", 100L, null, 1700000000L);
        verify(amazonDynamoDB, never()).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testUpsert_EmptyStrategyCode_SkipsCallEntirely() {
        service.upsertRetrievalProvenance("uid1", 100L, "", 1700000000L);
        verify(amazonDynamoDB, never()).updateItem(any(UpdateItemRequest.class));
    }
}
