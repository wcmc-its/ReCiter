package reciter.xml.retriever.pubmed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

public class RetrievalWindowTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 8, 3);

	private static Instant on(int y, int m, int d) {
		return LocalDate.of(y, m, d).atStartOfDay(ZoneOffset.UTC).toInstant();
	}

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
	public void dateMissedByTheWatermarkWindowFallsInsideTheFlooredWindow() {
		// A uid that last ran 2026-07-30 gets a watermark window starting 2026-07-29,
		// which excludes an article indexed 2026-06-04 — one missed run and it is gone
		// for good. The floored window must still cover that date.
		LocalDate start = RetrievalWindow.incrementalStart(on(2026, 7, 30), TODAY, 90);
		assertTrue(start.isBefore(LocalDate.of(2026, 6, 4)), "EDAT 2026-06-04 must fall inside the window");
	}

	@Test
	public void windowIsNeverNarrowerThanTheFloor() {
		LocalDate floorStart = TODAY.minusDays(90);
		for (int age = 0; age <= 120; age++) {
			Instant watermark = TODAY.minusDays(age).atStartOfDay(ZoneOffset.UTC).toInstant();
			LocalDate start = RetrievalWindow.incrementalStart(watermark, TODAY, 90);
			assertFalse(start.isAfter(floorStart),
					"watermark " + age + "d old gave window start " + start + ", inside the floor");
		}
	}

	@Test
	public void floorOfZeroKeepsLegacyWatermarkBehaviour() {
		// Legacy window: retrievalDate - 1 day, exactly.
		assertEquals(LocalDate.of(2026, 7, 29),
				RetrievalWindow.incrementalStart(on(2026, 7, 30), TODAY, 0));
		assertEquals(LocalDate.of(2025, 10, 31),
				RetrievalWindow.incrementalStart(on(2025, 11, 1), TODAY, 0));
		// Legacy behaviour with no prior run: yesterday.
		assertEquals(TODAY.minusDays(1), RetrievalWindow.incrementalStart(null, TODAY, 0));
	}

	@Test
	public void nullWatermarkFallsBackToTheFloor() {
		assertEquals(LocalDate.of(2026, 5, 5), RetrievalWindow.incrementalStart(null, TODAY, 90));
	}
}
