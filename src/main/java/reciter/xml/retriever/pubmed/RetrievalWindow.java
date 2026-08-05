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

/**
 * Decides how far back an ONLY_NEWLY_ADDED retrieval reaches.
 *
 * <p>Background: the incremental window used to be {@code retrievalDate - 1 day}, and
 * nothing ever revisits a window once it has been passed. So any single run that
 * failed to surface an article — a swallowed PubMed failure, a client read timeout,
 * a night the job did not run — dropped that article permanently, until somebody
 * noticed and triggered a manual full sweep. (#691 closed the narrower case where a
 * PubMed call errored and the watermark advanced anyway.)
 *
 * <p>{@link #incrementalStart} floors the lookback so a missed window degrades from
 * permanent loss to a bounded delay: the next run re-covers everything inside the
 * floor no matter what happened to the runs before it.
 *
 * <p>Pure and side-effect free so the decision is unit-testable without Spring or AWS.
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

	private static LocalDate toUtcDate(Instant instant) {
		return instant.atZone(ZoneOffset.UTC).toLocalDate();
	}
}
