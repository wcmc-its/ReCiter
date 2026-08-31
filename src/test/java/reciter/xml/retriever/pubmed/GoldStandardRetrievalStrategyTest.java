package reciter.xml.retriever.pubmed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import reciter.database.dynamodb.model.GoldStandard;
import reciter.model.identity.Identity;
import reciter.service.dynamo.IDynamoDbGoldStandardService;

@ExtendWith(MockitoExtension.class)
public class GoldStandardRetrievalStrategyTest {

	@Mock
	private IDynamoDbGoldStandardService dynamoDbGoldStandardService;

	private GoldStandardRetrievalStrategy strategy;

	@BeforeEach
	public void setUp() {
		strategy = new GoldStandardRetrievalStrategy();
		ReflectionTestUtils.setField(strategy, "dynamoDbGoldStandardService", dynamoDbGoldStandardService);
	}

	// Test 1 (spec: buildQuery_emptyGoldStandard_returnsEmptyList)
	@Test
	public void testEmptyGoldStandard() {
		when(dynamoDbGoldStandardService.findByUid("u1")).thenReturn(new GoldStandard());
		List<PubMedQueryType> qs = strategy.buildQuery(identity("u1"), Collections.emptyMap());
		assertTrue(qs.isEmpty(), "Empty GoldStandard should produce no queries");
	}

	// Test 2 (spec: buildQuery_under100Pmids_singleChunk)
	@Test
	public void testSingleChunk() {
		GoldStandard gs = goldStandardWithKnownPmids(50);
		when(dynamoDbGoldStandardService.findByUid("u1")).thenReturn(gs);
		List<PubMedQueryType> qs = strategy.buildQuery(identity("u1"), Collections.emptyMap());
		assertEquals(1, qs.size(), "50 PMIDs should fit in 1 chunk");
		assertEquals(50, countPmidsAcross(qs), "Total PMIDs across chunks");
	}

	// Test 3 (spec: buildQuery_2060Pmids_chunksTo21Queries — using 2056 to match Phase 35 baseline)
	@Test
	public void testShariatScale2056Pmids() {
		GoldStandard gs = goldStandardWithKnownPmids(2056);
		when(dynamoDbGoldStandardService.findByUid("sfs2002")).thenReturn(gs);
		List<PubMedQueryType> qs = strategy.buildQuery(identity("sfs2002"), Collections.emptyMap());
		assertEquals(21, qs.size(), "2056 PMIDs should produce 21 chunks (20*100 + 1*56)");
		for (PubMedQueryType qt : qs) {
			int n = countPmidsInChunk(qt);
			assertTrue(n <= 100, "Each chunk must have <= 100 PMIDs (got " + n + ")");
		}
		assertEquals(2056, countPmidsAcross(qs), "Total PMIDs across chunks");
	}

	// Test 4 (spec: buildQueryGoldStandard_dedupesAgainstUniquePmids)
	@Test
	public void testDedupesAgainstUniquePmids() {
		GoldStandard gs = goldStandardWithKnownPmids(150);
		when(dynamoDbGoldStandardService.findByUid("u1")).thenReturn(gs);
		Set<Long> already = LongStream.range(0, 50).boxed().collect(Collectors.toSet());
		List<PubMedQueryType> qs = strategy.buildQueryGoldStandard(identity("u1"), already);
		assertEquals(100, countPmidsAcross(qs), "150 known minus 50 already-retrieved = 100");
	}

	// Test 5 (spec: buildQuery_dateRangeVariant_appliesDateRangeOnlyToTermNotCount)
	@Test
	public void testDateRangeVariant() throws Exception {
		GoldStandard gs = goldStandardWithKnownPmids(50);
		when(dynamoDbGoldStandardService.findByUid("u1")).thenReturn(gs);
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date start = fmt.parse("2026-01-01");
		Date end = fmt.parse("2026-04-30");
		List<PubMedQueryType> qs = strategy.buildQuery(identity("u1"), Collections.emptyMap(), start, end);
		assertEquals(1, qs.size());
		PubMedQueryType qt = qs.get(0);
		assertNotNull(qt.getLenientQuery().getQuery().getStart(), "term query should have start date applied");
		assertNull(qt.getLenientCountQuery().getQuery().getStart(),"count query should NOT have date applied (would trigger threshold gate)");
	}

	// Test 6 (spec: buildQuery_blankUid_throws)
	@Test
	public void testBlankUidThrows() {
		Identity i = new Identity();
		i.setUid("");
		assertThrows(IllegalArgumentException.class, () -> strategy.buildQuery(i, Collections.emptyMap()));
	}

	// Test 7 (spec: rejected PMIDs included in no-args buildQuery)
	@Test
	public void testRejectedPmidsIncluded() {
		GoldStandard gs = new GoldStandard();
		gs.setKnownPmids(new ArrayList<>(Arrays.asList(111L, 222L)));
		gs.setRejectedPmids(new ArrayList<>(Arrays.asList(333L)));
		when(dynamoDbGoldStandardService.findByUid("u1")).thenReturn(gs);
		List<PubMedQueryType> qs = strategy.buildQuery(identity("u1"), Collections.emptyMap());
		assertFalse(qs.isEmpty(), "At least one query expected");
		String q = qs.get(0).getLenientQuery().getQuery().getStrategyQuery();
		assertTrue(q.contains("111"), "query should contain known PMID 111");
		assertTrue(q.contains("222"), "query should contain known PMID 222");
		assertTrue(q.contains("333"), "query should contain rejected PMID 333");
	}

	// Test 8 (OPTIONAL — defensive guard against URL-length regression)
	@Test
	public void testQueryLengthUnder2000Chars() {
		GoldStandard gs = goldStandardWithKnownPmids(100);
		when(dynamoDbGoldStandardService.findByUid("u1")).thenReturn(gs);
		List<PubMedQueryType> qs = strategy.buildQuery(identity("u1"), Collections.emptyMap());
		for (PubMedQueryType qt : qs) {
			int len = qt.getLenientQuery().getQuery().getStrategyQuery().length();
			assertTrue(len < 2000, "Chunk query exceeded 2000 chars (len=" + len + ")");
		}
	}

	// --- helpers ---

	private Identity identity(String uid) {
		Identity i = new Identity();
		i.setUid(uid);
		return i;
	}

	private GoldStandard goldStandardWithKnownPmids(int n) {
		GoldStandard gs = new GoldStandard();
		gs.setKnownPmids(LongStream.range(0, n).boxed().collect(Collectors.toList()));
		return gs;
	}

	private int countPmidsAcross(List<PubMedQueryType> qs) {
		return qs.stream().mapToInt(this::countPmidsInChunk).sum();
	}

	private int countPmidsInChunk(PubMedQueryType qt) {
		String q = qt.getLenientQuery().getQuery().getStrategyQuery();
		if (q == null || q.isEmpty()) {
			return 0;
		}
		return q.split(",").length;
	}
}
