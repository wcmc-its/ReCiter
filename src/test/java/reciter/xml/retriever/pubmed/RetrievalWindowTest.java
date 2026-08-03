package reciter.xml.retriever.pubmed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;

public class RetrievalWindowTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 8, 3);

	private static Instant on(int y, int m, int d) {
		return LocalDate.of(y, m, d).atStartOfDay(ZoneOffset.UTC).toInstant();
	}

	// --- incrementalStart -------------------------------------------------

	@Test
	public void floorAppliesWhenTheUidRanRecently() {
		// Ran yesterday: the legacy watermark window would start 2026-07-30 and miss
		// anything indexed earlier. The floor pulls it back to 90 days.
		assertEquals(LocalDate.of(2026, 5, 5),
				RetrievalWindow.incrementalStart(on(2026, 7, 31), TODAY, 90));
	}

	@Test
	public void watermarkWinsWhenItIsOlderThanTheFloor() {
		// Nine months since the last run: keep reaching back that far, do not shorten
		// the window to the floor.
		assertEquals(LocalDate.of(2025, 10, 31),
				RetrievalWindow.incrementalStart(on(2025, 11, 1), TODAY, 90));
	}

	@Test
	public void regressionCam4024ArticleFallsInsideTheFlooredWindow() {
		// PMID 42242702: EDAT 2026-06-04, uid last ran 2026-07-30. The 1-day watermark
		// window (start 2026-07-29) excluded it; the floored window must include it.
		LocalDate start = RetrievalWindow.incrementalStart(on(2026, 7, 30), TODAY, 90);
		assertTrue(start.isBefore(LocalDate.of(2026, 6, 4)), "EDAT 2026-06-04 must fall inside the window");
	}

	@Test
	public void floorOfZeroKeepsLegacyWatermarkBehaviour() {
		assertEquals(LocalDate.of(2026, 7, 29),
				RetrievalWindow.incrementalStart(on(2026, 7, 30), TODAY, 0));
	}

	@Test
	public void nullWatermarkFallsBackToTheFloor() {
		assertEquals(LocalDate.of(2026, 5, 5), RetrievalWindow.incrementalStart(null, TODAY, 90));
	}

	// --- fullSweepDue -----------------------------------------------------

	@Test
	public void freshSweepIsNotDue() {
		assertFalse(RetrievalWindow.fullSweepDue(on(2026, 7, 1), TODAY, "cam4024", 180, 45));
	}

	@Test
	public void staleSweepIsDueEvenWithMaximumJitter() {
		// 180 + at most 44 days of jitter = 224; two years is past any jitter value.
		assertTrue(RetrievalWindow.fullSweepDue(on(2024, 8, 3), TODAY, "cam4024", 180, 45));
	}

	@Test
	public void neverSweptIsDue() {
		assertTrue(RetrievalWindow.fullSweepDue(null, TODAY, "cam4024", 180, 45));
	}

	@Test
	public void maxAgeOfZeroDisablesReSweeping() {
		assertFalse(RetrievalWindow.fullSweepDue(null, TODAY, "cam4024", 0, 45));
		assertFalse(RetrievalWindow.fullSweepDue(on(2000, 1, 1), TODAY, "cam4024", 0, 45));
	}

	@Test
	public void jitterSpreadsACohortSweptOnTheSameDay() {
		// 4,898 uids share a 2026-05-15 sweep date. They must not all come due at once.
		Instant cohortSweep = on(2026, 5, 15);
		int due = 0;
		for (int i = 0; i < 400; i++) {
			if (RetrievalWindow.fullSweepDue(cohortSweep, LocalDate.of(2026, 11, 20), "uid" + i, 180, 45)) {
				due++;
			}
		}
		assertTrue(due > 0 && due < 400, "jitter must delay part of the cohort, saw " + due + "/400");
	}

	@Test
	public void jitterIsStablePerUid() {
		for (int i = 0; i < 50; i++) {
			LocalDate day = LocalDate.of(2026, 11, 1).plusDays(i);
			assertEquals(RetrievalWindow.fullSweepDue(on(2026, 5, 15), day, "cam4024", 180, 45),
					RetrievalWindow.fullSweepDue(on(2026, 5, 15), day, "cam4024", 180, 45));
		}
	}

	// --- lastFullSweep ----------------------------------------------------

	@Test
	public void lastFullSweepIgnoresIncrementalEntries() {
		ESearchResult r = new ESearchResult();
		r.setESearchPmids(Arrays.asList(
				new ESearchPmid(Collections.singletonList(1L), "AffiliationInDbRetrievalStrategy", on(2026, 5, 9),
						ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS),
				new ESearchPmid(Collections.singletonList(2L), "OrcidRetrievalStrategy", on(2026, 4, 1),
						ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS),
				// newer, but incremental - must not count as a full sweep
				new ESearchPmid(Collections.singletonList(3L), "FullNameRetrievalStrategy", on(2026, 7, 30),
						ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS)));
		assertEquals(on(2026, 5, 9), RetrievalWindow.lastFullSweep(r));
	}

	@Test
	public void lastFullSweepIsNullWhenOnlyIncrementalEntriesExist() {
		ESearchResult r = new ESearchResult();
		r.setESearchPmids(Collections.singletonList(
				new ESearchPmid(Collections.singletonList(3L), "FullNameRetrievalStrategy", on(2026, 7, 30),
						ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS)));
		assertNull(RetrievalWindow.lastFullSweep(r));
		assertNull(RetrievalWindow.lastFullSweep(null));
		assertNull(RetrievalWindow.lastFullSweep(new ESearchResult()));
	}
}
