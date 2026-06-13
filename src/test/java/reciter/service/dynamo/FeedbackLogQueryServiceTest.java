package reciter.service.dynamo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

/**
 * Unit tests for {@link FeedbackLogQueryService#getAuditHistory(String)} (Phase 34
 * audit-history read path). Verifies: null/empty short-circuit, field mapping +
 * provenance join, multi-page concatenation, the {@code MAX_ROWS} truncation cap,
 * graceful degradation when the provenance join fails, and the
 * {@code NumberFormatException -> 0} fallback for malformed numeric attributes.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeedbackLogQueryServiceTest {

    private static final String FEEDBACK_LOG = "FeedbackLog";
    private static final String ARTICLE_PROVENANCE = "ArticleProvenance";

    @Mock
    private AmazonDynamoDB amazonDynamoDB;

    private FeedbackLogQueryService service;

    @Before
    public void setUp() {
        service = new FeedbackLogQueryService(amazonDynamoDB);
    }

    // ---- helpers ----

    private static Map<String, AttributeValue> s(String key, String val, Map<String, AttributeValue> m) {
        m.put(key, new AttributeValue().withS(val));
        return m;
    }

    private static Map<String, AttributeValue> feedbackItem(String articleId, String feedback,
                                                            String curatedByN, String tsN, String sk, String src) {
        Map<String, AttributeValue> m = new HashMap<>();
        if (articleId != null) m.put("articleId", new AttributeValue().withS(articleId));
        if (feedback != null) m.put("feedback", new AttributeValue().withS(feedback));
        if (curatedByN != null) m.put("curatedBy", new AttributeValue().withN(curatedByN));
        if (tsN != null) m.put("createTimestamp", new AttributeValue().withN(tsN));
        if (sk != null) m.put("sk", new AttributeValue().withS(sk));
        if (src != null) m.put("src", new AttributeValue().withS(src));
        return m;
    }

    private static Map<String, AttributeValue> provItem(String articleId, String rs, String frdN, String src) {
        Map<String, AttributeValue> m = new HashMap<>();
        if (articleId != null) m.put("articleId", new AttributeValue().withS(articleId));
        if (rs != null) m.put("rs", new AttributeValue().withS(rs));
        if (frdN != null) m.put("frd", new AttributeValue().withN(frdN));
        if (src != null) m.put("src", new AttributeValue().withS(src));
        return m;
    }

    @SafeVarargs
    private static QueryResult page(Map<String, AttributeValue> lastKey, Map<String, AttributeValue>... items) {
        List<Map<String, AttributeValue>> list = new ArrayList<>();
        for (Map<String, AttributeValue> i : items) list.add(i);
        QueryResult r = new QueryResult().withItems(list);
        if (lastKey != null) r.setLastEvaluatedKey(lastKey);
        return r;
    }

    /** Route query() by table name: ArticleProvenance -> prov; FeedbackLog -> successive pages. */
    private void stubQueries(QueryResult prov, QueryResult... feedbackPages) {
        AtomicInteger fb = new AtomicInteger(0);
        when(amazonDynamoDB.query(any(QueryRequest.class))).thenAnswer(inv -> {
            QueryRequest r = inv.getArgument(0);
            if (ARTICLE_PROVENANCE.equals(r.getTableName())) {
                return prov;
            }
            int idx = Math.min(fb.getAndIncrement(), feedbackPages.length - 1);
            return feedbackPages[idx];
        });
    }

    // ---- tests ----

    @Test
    public void nullOrEmptyUid_returnsEmptyAndNeverQueries() {
        assertTrue(service.getAuditHistory(null).isEmpty());
        assertTrue(service.getAuditHistory("").isEmpty());
        verify(amazonDynamoDB, never()).query(any(QueryRequest.class));
    }

    @Test
    public void singlePage_mapsFieldsAndJoinsProvenanceByArticleId() {
        stubQueries(
                page(null, provItem("100", "PUBMED_NAME_AFFIL", "1699000000", "MAN")),
                page(null, feedbackItem("100", "ACCEPTED", "42", "1700000000", "1700000000#ab", "MAN")));

        List<AuditHistoryEntry> history = service.getAuditHistory("uid1");

        assertEquals(1, history.size());
        AuditHistoryEntry e = history.get(0);
        assertEquals("100", e.articleId);
        assertEquals("ACCEPTED", e.feedback);
        assertEquals(42, e.curatedBy);
        assertEquals(1700000000L, e.createTimestamp);
        assertEquals("1700000000#ab", e.sk);
        assertEquals("MAN", e.src);
        // provenance join
        assertEquals("PUBMED_NAME_AFFIL", e.provenanceRs);
        assertEquals(Long.valueOf(1699000000L), e.provenanceFrd);
        assertEquals("MAN", e.provenanceSrc);
    }

    @Test
    public void multiplePages_areConcatenated() {
        Map<String, AttributeValue> lastKey = s("uid", "uid1", s("sk", "1700000001#a", new HashMap<>()));
        stubQueries(
                page(null), // no provenance
                page(lastKey, feedbackItem("100", "ACCEPTED", "1", "1700000002", "1700000002#a", "MAN")),
                page(null, feedbackItem("200", "REJECTED", "1", "1700000001", "1700000001#b", "MAN")));

        List<AuditHistoryEntry> history = service.getAuditHistory("uid1");

        assertEquals(2, history.size());
        assertEquals("100", history.get(0).articleId);
        assertEquals("200", history.get(1).articleId);
    }

    @Test
    public void resultsTruncateAtMaxRows() {
        Map<String, AttributeValue>[] many = buildItems(2500);
        // single page well over the cap; truncation happens inside the item loop
        stubQueries(page(null), page(null, many));

        List<AuditHistoryEntry> history = service.getAuditHistory("uid1");

        assertEquals(2000, history.size());
    }

    @Test
    public void provenanceLoadFailure_stillReturnsEntriesWithNullProvenance() {
        when(amazonDynamoDB.query(any(QueryRequest.class))).thenAnswer(inv -> {
            QueryRequest r = inv.getArgument(0);
            if (ARTICLE_PROVENANCE.equals(r.getTableName())) {
                throw new RuntimeException("provenance table unavailable");
            }
            return page(null, feedbackItem("100", "ACCEPTED", "7", "1700000000", "1700000000#z", "MAN"));
        });

        List<AuditHistoryEntry> history = service.getAuditHistory("uid1");

        assertEquals(1, history.size());
        AuditHistoryEntry e = history.get(0);
        assertEquals("100", e.articleId);   // audit row still returned
        assertEquals(7, e.curatedBy);
        assertNull("provenance fields stay null when the join fails", e.provenanceRs);
        assertNull(e.provenanceFrd);
        assertNull(e.provenanceSrc);
    }

    @Test
    public void malformedNumericAttributes_fallBackToZero() {
        Map<String, AttributeValue> corrupt = new HashMap<>();
        corrupt.put("articleId", new AttributeValue().withS("100"));
        corrupt.put("feedback", new AttributeValue().withS("ACCEPTED"));
        corrupt.put("curatedBy", new AttributeValue().withN("not-a-number"));
        corrupt.put("createTimestamp", new AttributeValue().withN("garbage"));
        stubQueries(page(null), page(null, corrupt));

        List<AuditHistoryEntry> history = service.getAuditHistory("uid1");

        assertEquals(1, history.size());
        AuditHistoryEntry e = history.get(0);
        assertEquals("100", e.articleId);
        assertEquals(0, e.curatedBy);          // NumberFormatException -> 0
        assertEquals(0L, e.createTimestamp);   // NumberFormatException -> 0
    }

    @SuppressWarnings("unchecked")
    private static Map<String, AttributeValue>[] buildItems(int n) {
        List<Map<String, AttributeValue>> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            list.add(feedbackItem(String.valueOf(i), "ACCEPTED", "1", "1700000000", i + "#x", "MAN"));
        }
        return list.toArray(new Map[0]);
    }
}
