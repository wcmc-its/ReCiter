/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.xml.retriever.pubmed;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;

/**
 * Decides how far back an ONLY_NEWLY_ADDED retrieval reaches, and when a uid is
 * overdue for a full ALL_PUBLICATIONS sweep.
 *
 * <p>Background: the incremental window used to be {@code retrievalDate - 1 day}, and
 * nothing ever revisits a window once it has been passed. So any single run that
 * failed to surface an article — a day the uid was not scheduled, an identity that
 * only later gained the alternate name / grant / email that makes an article findable,
 * or a PubMed record revised after the fact — dropped that article permanently, until
 * somebody noticed and triggered a manual full sweep. (#691 closed the narrower case
 * where a PubMed call errored and the watermark advanced anyway.)
 *
 * <p>Two bounds make coverage self-healing instead:
 * <ol>
 *   <li>{@link #incrementalStart} floors the lookback, so ordinary run-cadence gaps
 *       cannot open a hole.</li>
 *   <li>{@link #fullSweepDue} caps how stale a uid's last full sweep may get, which
 *       heals holes older than the floor and picks up articles that only became
 *       attributable after an identity change.</li>
 * </ol>
 *
 * Pure and side-effect free so the decisions are unit-testable without Spring or AWS.
 */
public final class RetrievalWindow {

	private RetrievalWindow() {
	}

	/**
	 * Start of the incremental {@code [EDAT]}/{@code [DP]} window: the earlier of the
	 * previous run's watermark and the configured floor.
	 *
	 * @param lastRetrieval  ESearchResult.retrievalDate, or null if never run
	 * @param today          current date (injected so this stays testable)
	 * @param lookbackFloorDays minimum days to reach back; &lt;= 0 keeps the legacy
	 *                          watermark-only behaviour
	 */
	public static LocalDate incrementalStart(Instant lastRetrieval, LocalDate today, int lookbackFloorDays) {
		LocalDate watermark = lastRetrieval == null ? null : toUtcDate(lastRetrieval).minusDays(1);
		if (lookbackFloorDays <= 0) {
			// Floor disabled: fall back to the historical watermark-only window.
			return watermark == null ? today.minusDays(1) : watermark;
		}
		LocalDate floor = today.minusDays(lookbackFloorDays);
		if (watermark == null) {
			return floor;
		}
		return watermark.isBefore(floor) ? watermark : floor;
	}

	/**
	 * True when this uid's last full sweep is older than {@code maxAgeDays} (plus a
	 * per-uid jitter, so cohorts that were swept on the same day do not all come due
	 * together and stampede a single night's run).
	 *
	 * @param lastFullSweep most recent ALL_PUBLICATIONS retrieval, or null if never
	 * @param maxAgeDays    &lt;= 0 disables periodic re-sweeping entirely
	 * @param jitterDays    width of the per-uid spread; &lt;= 0 means no jitter
	 */
	public static boolean fullSweepDue(Instant lastFullSweep, LocalDate today, String uid, int maxAgeDays,
			int jitterDays) {
		if (maxAgeDays <= 0) {
			return false;
		}
		if (lastFullSweep == null) {
			return true;
		}
		int jitter = jitterDays <= 0 ? 0 : Math.floorMod(uid == null ? 0 : uid.hashCode(), jitterDays);
		LocalDate due = toUtcDate(lastFullSweep).plusDays((long) maxAgeDays + jitter);
		return !today.isBefore(due);
	}

	/**
	 * Most recent ALL_PUBLICATIONS retrieval recorded on the item, or null if the uid
	 * has no full sweep on record. Read from the per-strategy entries that are already
	 * persisted, so this needs no schema change.
	 *
	 * <p>Entries are only written when a strategy returned at least one pmid, so this is
	 * a lower bound on the true last-sweep date — erring toward re-sweeping sooner.
	 */
	public static Instant lastFullSweep(ESearchResult eSearchResult) {
		if (eSearchResult == null || eSearchResult.getESearchPmids() == null) {
			return null;
		}
		Instant latest = null;
		for (ESearchPmid entry : eSearchResult.getESearchPmids()) {
			if (entry == null || entry.getRetrievalDate() == null
					|| entry.getLookupType() != ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS) {
				continue;
			}
			if (latest == null || entry.getRetrievalDate().isAfter(latest)) {
				latest = entry.getRetrievalDate();
			}
		}
		return latest;
	}

	private static LocalDate toUtcDate(Instant instant) {
		return instant.atZone(ZoneOffset.UTC).toLocalDate();
	}
}
