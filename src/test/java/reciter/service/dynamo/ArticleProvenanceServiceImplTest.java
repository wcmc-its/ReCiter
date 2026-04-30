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

import java.util.Collections;
import java.util.HashMap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;

import reciter.feedback.EntryPath;

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

    // -----------------------------------------------------------------------------------------
    // Phase 33-02: D-11 transition table tests via computeNewSrc + upsertCuratorAction
    // -----------------------------------------------------------------------------------------

    @Test
    public void testComputeNewSrc_AbsentToMan() {
        assertEquals("MAN", ArticleProvenanceServiceImpl.computeNewSrc(null));
    }

    @Test
    public void testComputeNewSrc_GsToMan() {
        assertEquals("MAN", ArticleProvenanceServiceImpl.computeNewSrc("GS"));
    }

    @Test
    public void testComputeNewSrc_PmToManFromPm() {
        assertEquals("MAN_FROM_PM", ArticleProvenanceServiceImpl.computeNewSrc("PM"));
    }

    @Test
    public void testComputeNewSrc_CtscToManFromCtsc() {
        assertEquals("MAN_FROM_CTSC", ArticleProvenanceServiceImpl.computeNewSrc("CTSC"));
    }

    @Test
    public void testComputeNewSrc_ManIsNoop() {
        assertEquals("MAN", ArticleProvenanceServiceImpl.computeNewSrc("MAN"));
    }

    @Test
    public void testComputeNewSrc_ManFromPmIsNoop() {
        assertEquals("MAN_FROM_PM", ArticleProvenanceServiceImpl.computeNewSrc("MAN_FROM_PM"));
    }

    @Test
    public void testComputeNewSrc_ManFromCtscIsNoop() {
        assertEquals("MAN_FROM_CTSC", ArticleProvenanceServiceImpl.computeNewSrc("MAN_FROM_CTSC"));
    }

    private void stubGetItemSrc(String existingSrc) {
        GetItemResult result = new GetItemResult();
        if (existingSrc != null) {
            HashMap<String, AttributeValue> item = new HashMap<>();
            item.put("src", new AttributeValue().withS(existingSrc));
            result.setItem(item);
        }
        when(amazonDynamoDB.getItem(any(GetItemRequest.class))).thenReturn(result);
    }

    @Test
    public void testCuratorAction_NoExisting_CandidateList_WritesSrcMan() {
        stubGetItemSrc(null);
        service.upsertCuratorAction("uid1", 100L, EntryPath.CANDIDATE_LIST, 1700000000L);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(amazonDynamoDB).updateItem(captor.capture());
        UpdateItemRequest req = captor.getValue();
        assertEquals("MAN", req.getExpressionAttributeValues().get(":new").getS());
        assertEquals("attribute_not_exists(src)", req.getConditionExpression());
        assertTrue("UpdateExpression must SET src and frd: " + req.getUpdateExpression(),
                req.getUpdateExpression().contains("SET src = :new")
                        && req.getUpdateExpression().contains("frd = if_not_exists(frd, :ts)"));
    }

    @Test
    public void testCuratorAction_ExistingPm_CandidateList_TransitionsToManFromPm() {
        stubGetItemSrc("PM");
        service.upsertCuratorAction("uid1", 100L, EntryPath.CANDIDATE_LIST, 1700000000L);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(amazonDynamoDB).updateItem(captor.capture());
        UpdateItemRequest req = captor.getValue();
        assertEquals("MAN_FROM_PM", req.getExpressionAttributeValues().get(":new").getS());
        assertEquals("PM", req.getExpressionAttributeValues().get(":existing").getS());
        assertEquals("src = :existing", req.getConditionExpression());
    }

    @Test
    public void testCuratorAction_ExistingCtsc_CandidateList_TransitionsToManFromCtsc() {
        stubGetItemSrc("CTSC");
        service.upsertCuratorAction("uid1", 100L, EntryPath.CANDIDATE_LIST, 1700000000L);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(amazonDynamoDB).updateItem(captor.capture());
        UpdateItemRequest req = captor.getValue();
        assertEquals("MAN_FROM_CTSC", req.getExpressionAttributeValues().get(":new").getS());
        assertEquals("CTSC", req.getExpressionAttributeValues().get(":existing").getS());
    }

    @Test
    public void testCuratorAction_ExistingManFromPm_CandidateList_NoSrcChangeOnlyFrdMaintenance() {
        stubGetItemSrc("MAN_FROM_PM");
        service.upsertCuratorAction("uid1", 100L, EntryPath.CANDIDATE_LIST, 1700000000L);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(amazonDynamoDB).updateItem(captor.capture());
        UpdateItemRequest req = captor.getValue();
        // Should write only frd (idempotent on src)
        assertTrue("Expression must be only frd if_not_exists: " + req.getUpdateExpression(),
                req.getUpdateExpression().equals("SET frd = if_not_exists(frd, :ts)"));
        // No condition (since src is already correct)
        assertTrue("No conditional needed for noop: " + req.getConditionExpression(),
                req.getConditionExpression() == null);
    }

    @Test
    public void testCuratorAction_PubmedSearch_NewPmid_WritesPmUiSearchThenManFromPm() {
        stubGetItemSrc(null);
        // First updateItem call is the PM_UI_SEARCH retrieval-record write (D-13)
        // Second updateItem call is the D-11 transition (which sees no existing src after the
        // mock GetItem; in real life GetItem would read what D-13 just wrote, but we control
        // the mock to verify the two-step sequence).
        service.upsertCuratorAction("uid1", 100L, EntryPath.PUBMED_SEARCH, 1700000000L);

        ArgumentCaptor<UpdateItemRequest> captor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        verify(amazonDynamoDB, times(2)).updateItem(captor.capture());
        UpdateItemRequest first = captor.getAllValues().get(0);
        // Step 1: PM_UI_SEARCH retrieval-record write
        assertTrue("First call must SET rs to PM_UI_SEARCH if absent: " + first.getUpdateExpression(),
                first.getUpdateExpression().contains("rs  = if_not_exists(rs,  :rs)"));
        assertEquals("PM_UI_SEARCH", first.getExpressionAttributeValues().get(":rs").getS());
        assertEquals("PM", first.getExpressionAttributeValues().get(":pm").getS());
        assertNotNull(first.getExpressionAttributeValues().get(":rsSet"));
        assertEquals("PM_UI_SEARCH", first.getExpressionAttributeValues().get(":rsSet").getSS().get(0));
    }

    @Test
    public void testCuratorAction_PubmedSearch_TwoUpdateItemCallsSequence() {
        stubGetItemSrc(null);
        service.upsertCuratorAction("uid1", 100L, EntryPath.PUBMED_SEARCH, 1700000000L);
        // PUBMED_SEARCH path: 1 PM_UI_SEARCH write + 1 D-11 transition = 2 updateItem calls
        verify(amazonDynamoDB, times(2)).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testCuratorAction_CandidateList_OneUpdateItemCall() {
        stubGetItemSrc(null);
        service.upsertCuratorAction("uid1", 100L, EntryPath.CANDIDATE_LIST, 1700000000L);
        // CANDIDATE_LIST path: only the D-11 transition = 1 updateItem call
        verify(amazonDynamoDB, times(1)).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testCuratorAction_NullUid_SkipsCallEntirely() {
        service.upsertCuratorAction(null, 100L, EntryPath.CANDIDATE_LIST, 1700000000L);
        verify(amazonDynamoDB, never()).updateItem(any(UpdateItemRequest.class));
        verify(amazonDynamoDB, never()).getItem(any(GetItemRequest.class));
    }

    @Test
    public void testCuratorAction_NullEntryPath_DefaultsCandidateList() {
        stubGetItemSrc("PM");
        service.upsertCuratorAction("uid1", 100L, null, 1700000000L);
        // Should be 1 updateItem call (CANDIDATE_LIST path)
        verify(amazonDynamoDB, times(1)).updateItem(any(UpdateItemRequest.class));
    }

    @Test
    public void testCuratorAction_RaceOnUpdate_RetriesOnce() {
        stubGetItemSrc("PM");
        when(amazonDynamoDB.updateItem(any(UpdateItemRequest.class)))
                .thenThrow(new ConditionalCheckFailedException("race"))
                .thenReturn(null);  // second attempt succeeds

        service.upsertCuratorAction("uid1", 100L, EntryPath.CANDIDATE_LIST, 1700000000L);
        // 2 updateItem invocations: original + 1 retry
        verify(amazonDynamoDB, times(2)).updateItem(any(UpdateItemRequest.class));
        // GetItem is called twice (once initial, once on retry)
        verify(amazonDynamoDB, times(2)).getItem(any(GetItemRequest.class));
    }
}
