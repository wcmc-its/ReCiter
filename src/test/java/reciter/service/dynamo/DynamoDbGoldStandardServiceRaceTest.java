package reciter.service.dynamo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.repository.DynamoDbGoldStandardRepository;
import reciter.feedback.EntryPath;
import reciter.service.ArticleProvenanceService;
import reciter.service.ESearchResultService;
import reciter.service.FeedbackLogService;
import reciter.service.PmidProvenanceService;

/**
 * Regression coverage for the GoldStandard lost-update race: two concurrent single-pmid
 * accepts against the same baseline used to clobber each other because the persist step was an
 * unconditional putItem. The fix persists via a conditional putItem guarded on the pre-image of
 * knownpmids/rejectedpmids and retries the read-merge-write on conflict, deferring side-effect
 * writes until after a durable commit so retries do not duplicate them.
 */
@ExtendWith(MockitoExtension.class)
public class DynamoDbGoldStandardServiceRaceTest {

	@Mock
	private DynamoDbGoldStandardRepository goldStandardRepository;
	@Mock
	private ESearchResultService eSearchResultService;
	@Mock
	private PmidProvenanceService pmidProvenanceService;
	@Mock
	private FeedbackLogService feedbackLogService;
	@Mock
	private ArticleProvenanceService articleProvenanceService;

	private DynamoDbGoldStandardService service;

	private static final String UID = "uid1";

	@BeforeEach
	public void setUp() {
		service = new DynamoDbGoldStandardService(
				goldStandardRepository,
				eSearchResultService,
				pmidProvenanceService,
				feedbackLogService,
				articleProvenanceService);
	}

	/** Fresh baseline on every read: a concurrent writer already committed pmid 100. */
	private GoldStandard baselineWith100() {
		return new GoldStandard(UID, new ArrayList<>(Collections.singletonList(100L)), new ArrayList<>(), null);
	}

	/**
	 * The first conditional write loses the race (returns false); the service must re-read,
	 * re-merge against the now-visible baseline [100], and commit [100, 200] on the retry —
	 * with the FeedbackLog side effect for pmid 200 written exactly once, not per attempt.
	 */
	@Test
	public void retryOnConflictMergesBothPmidsAndWritesSideEffectOnce() {
		// Every read sees the concurrent writer's 100 (fresh copy so attempts don't alias state).
		when(goldStandardRepository.findById(UID)).thenAnswer(inv -> Optional.of(baselineWith100()));
		// First persist conflicts, second succeeds.
		when(goldStandardRepository.saveIfUnchanged(any(GoldStandard.class), any(), any()))
				.thenReturn(false)
				.thenReturn(true);

		GoldStandard request = new GoldStandard(UID, new ArrayList<>(Collections.singletonList(200L)), null, null);
		service.save(request, GoldStandardUpdateFlag.UPDATE, "TestSource", EntryPath.CANDIDATE_LIST, 7);

		// Persisted twice (one conflict, one success); the committed item carries BOTH pmids.
		ArgumentCaptor<GoldStandard> persisted = ArgumentCaptor.forClass(GoldStandard.class);
		verify(goldStandardRepository, times(2)).saveIfUnchanged(persisted.capture(), any(), any());
		GoldStandard committed = persisted.getAllValues().get(persisted.getAllValues().size() - 1);
		assertTrue(committed.getKnownPmids().contains(100L), "lost update: 100 missing after retry");
		assertTrue(committed.getKnownPmids().contains(200L), "lost update: 200 missing after retry");

		// The single-accept side effect for pmid 200 fires exactly once despite the retry.
		verify(feedbackLogService, times(1))
				.recordAction(argThat(fl -> "200".equals(fl.getArticleId())));
	}

	/** No contention: a single conditional write commits and there is no retry. */
	@Test
	public void noConflictCommitsInOneAttempt() {
		when(goldStandardRepository.findById(UID))
				.thenReturn(Optional.of(new GoldStandard(UID, new ArrayList<>(Arrays.asList(100L)), new ArrayList<>(), null)));
		when(goldStandardRepository.saveIfUnchanged(any(GoldStandard.class), any(), any())).thenReturn(true);

		GoldStandard request = new GoldStandard(UID, new ArrayList<>(Collections.singletonList(200L)), null, null);
		service.save(request, GoldStandardUpdateFlag.UPDATE, "TestSource", EntryPath.CANDIDATE_LIST, 7);

		ArgumentCaptor<GoldStandard> persisted = ArgumentCaptor.forClass(GoldStandard.class);
		verify(goldStandardRepository, times(1)).saveIfUnchanged(persisted.capture(), any(), any());
		List<Long> known = persisted.getValue().getKnownPmids();
		assertTrue(known.contains(100L) && known.contains(200L), "merge must union existing and incoming pmids");
		verify(feedbackLogService, times(1))
				.recordAction(argThat(fl -> "200".equals(fl.getArticleId())));
	}
}
