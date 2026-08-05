package reciter.xml.retriever.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.model.pubmed.MedlineCitation;
import reciter.model.pubmed.MedlineCitationPMID;
import reciter.model.pubmed.PubMedArticle;

/**
 * Covers the known-pmid persistence skip (#695): with a floored lookback window the
 * same span is re-retrieved nightly, so incremental runs must not re-write articles
 * the uid's ESearchResult already records.
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
}
