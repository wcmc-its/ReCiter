package reciter.xml.retriever.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.model.identity.Identity;
import reciter.model.pubmed.MedlineCitation;
import reciter.model.pubmed.MedlineCitationPMID;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.xml.retriever.pubmed.RetrievalErrorTracker;

/**
 * Covers the known-pmid persistence skip (#695): with a floored lookback window the
 * same span is re-retrieved nightly, so incremental runs must not re-write articles
 * the uid's ESearchResult already records. Also covers the two #696 engine rules:
 * the lookupType downgrade guard on strategy-entry upserts and the clean-completion
 * gate on the lastFullSweep stamp.
 */
public class AbstractReCiterRetrievalEngineTest {

	private static PubMedArticle article(long pmid) {
		MedlineCitationPMID id = new MedlineCitationPMID();
		id.setPmid(pmid);
		MedlineCitation citation = new MedlineCitation();
		citation.setMedlinecitationpmid(id);
		PubMedArticle article = new PubMedArticle();
		article.setMedlinecitation(citation);
		return article;
	}

	private static List<Long> pmidsOf(List<PubMedArticle> articles) {
		return articles.stream()
				.map(a -> a.getMedlinecitation().getMedlinecitationpmid().getPmid())
				.collect(Collectors.toList());
	}

	private static ESearchResult resultWithKnownPmids() {
		ESearchResult existing = new ESearchResult();
		existing.setESearchPmids(Arrays.asList(
				new ESearchPmid(Arrays.asList(1L, 2L), "EmailRetrievalStrategy", Instant.now(),
						ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS),
				new ESearchPmid(Collections.singletonList(3L), "FullNameRetrievalStrategy", Instant.now(),
						ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS)));
		return existing;
	}

	@Test
	public void incrementalRunSkipsPmidsAlreadyOnTheESearchResult() {
		// 2 and 3 are already recorded (under different strategies); only 4 is new.
		List<PubMedArticle> toPersist = AbstractReCiterRetrievalEngine.articlesToPersist(
				Arrays.asList(article(2L), article(3L), article(4L)),
				resultWithKnownPmids(), RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);
		assertEquals(Collections.singletonList(4L), pmidsOf(toPersist));
	}

	@Test
	public void fullSweepRePersistsEverything() {
		// A full sweep refreshes stored article payloads, so nothing is skipped.
		List<PubMedArticle> toPersist = AbstractReCiterRetrievalEngine.articlesToPersist(
				Arrays.asList(article(2L), article(3L), article(4L)),
				resultWithKnownPmids(), RetrievalRefreshFlag.ALL_PUBLICATIONS);
		assertEquals(Arrays.asList(2L, 3L, 4L), pmidsOf(toPersist));
	}

	@Test
	public void incrementalRunWithNoPriorResultPersistsEverything() {
		List<PubMedArticle> retrieved = Arrays.asList(article(5L), article(6L));

		assertEquals(Arrays.asList(5L, 6L), pmidsOf(AbstractReCiterRetrievalEngine.articlesToPersist(
				retrieved, null, RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS)));
		assertEquals(Arrays.asList(5L, 6L), pmidsOf(AbstractReCiterRetrievalEngine.articlesToPersist(
				retrieved, new ESearchResult(), RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS)));
	}

	@Test
	public void entriesWithoutPmidListsAreTolerated() {
		ESearchResult existing = new ESearchResult();
		existing.setESearchPmids(Arrays.asList(
				null,
				new ESearchPmid(null, "EmailRetrievalStrategy", Instant.now(),
						ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS)));
		List<PubMedArticle> toPersist = AbstractReCiterRetrievalEngine.articlesToPersist(
				Collections.singletonList(article(7L)),
				existing, RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);
		assertEquals(Collections.singletonList(7L), pmidsOf(toPersist));
	}

	// ---- lookupType downgrade guard (#696 / E13) ----

	private static final Instant SWEEP_DATE = Instant.parse("2026-05-15T00:00:00Z");
	private static final Instant TONIGHT = Instant.parse("2026-08-03T00:00:00Z");

	@Test
	public void incrementalUpsertMustNotDowngradeAnAllPublicationsEntry() {
		// ArticleSizeStrategy counts pmids on entries whose lookupType is
		// ALL_PUBLICATIONS, and the escalation fallback infers last-full-sweep from
		// the same entries. The stored entry must therefore keep the marker AND the
		// sweep's retrievalDate (a bumped date would fake a fresh sweep and suppress
		// a due escalation), while merging in the incremental run's new pmids.
		ESearchPmid existing = new ESearchPmid(Arrays.asList(1L, 2L, 3L), "EmailRetrievalStrategy",
				SWEEP_DATE, ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS);
		ESearchPmid incoming = new ESearchPmid(Arrays.asList(3L, 4L), "EmailRetrievalStrategy",
				TONIGHT, ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);

		ESearchPmid stored = AbstractReCiterRetrievalEngine.upsertedStrategyEntry(existing, incoming);

		assertEquals(ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS, stored.getLookupType());
		assertEquals(SWEEP_DATE, stored.getRetrievalDate());
		assertEquals(Arrays.asList(1L, 2L, 3L, 4L), stored.getPmids());
	}

	@Test
	public void fullSweepUpsertReplacesTheEntryWholesale() {
		ESearchPmid existing = new ESearchPmid(Arrays.asList(1L, 2L), "EmailRetrievalStrategy",
				SWEEP_DATE, ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS);
		ESearchPmid incoming = new ESearchPmid(Arrays.asList(2L, 5L), "EmailRetrievalStrategy",
				TONIGHT, ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS);

		assertSame(incoming, AbstractReCiterRetrievalEngine.upsertedStrategyEntry(existing, incoming));
	}

	@Test
	public void incrementalOverIncrementalReplacesTheEntry() {
		ESearchPmid existing = new ESearchPmid(Arrays.asList(1L), "EmailRetrievalStrategy",
				SWEEP_DATE, ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);
		ESearchPmid incoming = new ESearchPmid(Arrays.asList(2L), "EmailRetrievalStrategy",
				TONIGHT, ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);

		assertSame(incoming, AbstractReCiterRetrievalEngine.upsertedStrategyEntry(existing, incoming));
	}

	@Test
	public void firstEntryForAStrategyIsStoredAsIs() {
		ESearchPmid incoming = new ESearchPmid(Arrays.asList(2L), "EmailRetrievalStrategy",
				TONIGHT, ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);
		assertSame(incoming, AbstractReCiterRetrievalEngine.upsertedStrategyEntry(null, incoming));
	}

	@Test
	public void downgradeGuardToleratesNullPmidLists() {
		ESearchPmid existing = new ESearchPmid(null, "EmailRetrievalStrategy",
				SWEEP_DATE, ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS);
		ESearchPmid incoming = new ESearchPmid(Arrays.asList(4L), "EmailRetrievalStrategy",
				TONIGHT, ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);

		ESearchPmid stored = AbstractReCiterRetrievalEngine.upsertedStrategyEntry(existing, incoming);
		assertEquals(ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS, stored.getLookupType());
		assertEquals(Arrays.asList(4L), stored.getPmids());
	}

	// ---- Clean-completion gate on the lastFullSweep stamp (#696) ----

	@AfterEach
	public void clearErrorFlag() {
		RetrievalErrorTracker.reset();
	}

	private static AbstractReCiterRetrievalEngine engineWith(ESearchResultService service) {
		AbstractReCiterRetrievalEngine engine = new AbstractReCiterRetrievalEngine() {
			@Override
			public boolean retrieveArticlesByDateRange(List<Identity> identities, java.util.Date startDate,
					java.util.Date endDate, RetrievalRefreshFlag refreshFlag) {
				return true;
			}

			@Override
			public void retrieveByPmids(String uid, List<Long> pmids) {
			}
		};
		engine.eSearchResultService = service;
		return engine;
	}

	@Test
	public void cleanFullSweepStampsLastFullSweep() {
		ESearchResultService service = mock(ESearchResultService.class);
		RetrievalErrorTracker.reset();

		engineWith(service).stampLastFullSweepIfClean("cam4024", RetrievalRefreshFlag.ALL_PUBLICATIONS);

		verify(service).stampLastFullSweepIfNewer(eq("cam4024"), any(Instant.class));
	}

	@Test
	public void sweepThatHitTheSwallowPathIsNotStamped() {
		// Stamping after a swallowed PubMed failure would lock the loss in for a full
		// interval; an unstamped person simply stays due and the sweep is retried.
		ESearchResultService service = mock(ESearchResultService.class);
		RetrievalErrorTracker.reset();
		RetrievalErrorTracker.markError();

		engineWith(service).stampLastFullSweepIfClean("cam4024", RetrievalRefreshFlag.ALL_PUBLICATIONS);

		verify(service, never()).stampLastFullSweepIfNewer(anyString(), any(Instant.class));
	}

	@Test
	public void incrementalRunsNeverStamp() {
		ESearchResultService service = mock(ESearchResultService.class);
		RetrievalErrorTracker.reset();

		engineWith(service).stampLastFullSweepIfClean("cam4024", RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);
		engineWith(service).stampLastFullSweepIfClean("cam4024", null);

		verify(service, never()).stampLastFullSweepIfNewer(anyString(), any(Instant.class));
	}
}
