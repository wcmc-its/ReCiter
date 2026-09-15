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
package reciter.xml.retriever.engine;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.model.QueryType;
import reciter.engine.StrategyParameters;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.xml.retriever.pubmed.AffiliationInDbRetrievalStrategy;
import reciter.xml.retriever.pubmed.AffiliationRetrievalStrategy;
import reciter.xml.retriever.pubmed.DepartmentRetrievalStrategy;
import reciter.xml.retriever.pubmed.EmailRetrievalStrategy;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.FullNameRetrievalStrategy;
import reciter.xml.retriever.pubmed.GoldStandardRetrievalStrategy;
import reciter.xml.retriever.pubmed.GrantRetrievalStrategy;
import reciter.xml.retriever.pubmed.KnownRelationshipRetrievalStrategy;
import reciter.xml.retriever.pubmed.OrcidRetrievalStrategy;
import reciter.xml.retriever.pubmed.PubMedQueryResult;
import reciter.xml.retriever.pubmed.SecondInitialRetrievalStrategy;

@Component("abstractReCiterRetrievalEngine")
public abstract class AbstractReCiterRetrievalEngine implements ReCiterRetrievalEngine {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractReCiterRetrievalEngine.class);

	@Autowired
	protected PubMedService pubMedService;

	@Autowired
	protected ESearchResultService eSearchResultService;

	@Autowired
	protected ScopusService scopusService;

	@Autowired
	protected IdentityService identityService;

	@Autowired
	protected StrategyParameters strategyParameters;

	@Autowired
	protected AffiliationInDbRetrievalStrategy affiliationInDbRetrievalStrategy;
	
	@Autowired
	protected AffiliationRetrievalStrategy affiliationRetrievalStrategy;
	
	@Autowired
	protected DepartmentRetrievalStrategy departmentRetrievalStrategy;
	
	@Autowired
	protected EmailRetrievalStrategy emailRetrievalStrategy;
	
	@Autowired
	protected FirstNameInitialRetrievalStrategy firstNameInitialRetrievalStrategy;
	
	@Autowired
	protected FullNameRetrievalStrategy fullNameRetrievalStrategy;
	
	@Autowired
	protected SecondInitialRetrievalStrategy secondIntialRetrievalStrategy;
	
	@Autowired
	protected KnownRelationshipRetrievalStrategy knownRelationshipRetrievalStrategy;
	
	@Autowired
	protected GoldStandardRetrievalStrategy goldStandardRetrievalStrategy;

	@Autowired
	protected OrcidRetrievalStrategy orcidRetrievalStrategy;

	@Autowired
	protected GrantRetrievalStrategy grantRetrievalStrategy;
	
	/**
	 * Save the PubMed articles and the ESearch results.
	 * @param pubMedArticles
	 * @param uid
	 */
	protected void savePubMedArticles(Collection<PubMedArticle> pubMedArticles, String uid, String retrievalStrategyName, List<PubMedQueryResult> pubMedQueryResults, QueryType queryType, RetrievalRefreshFlag refreshFlag) {
		// Read the existing search result first: it receives the upserted strategy entry
		// below, and it also tells us which pmids this uid already has persisted.
		ESearchResult eSearchResultDb = eSearchResultService.findByUid(uid);

		// Save the articles. On incremental runs, skip articles whose pmid is already on
		// the uid's ESearchResult (#695): the floored lookback window re-retrieves the same
		// span nightly, and without this every run would re-write that whole span to
		// DynamoDB/S3. Full sweeps still re-persist everything, so revised PubMed records
		// are refreshed at sweep cadence.
		List<PubMedArticle> pubMedArticleList = articlesToPersist(pubMedArticles, eSearchResultDb, refreshFlag);
		log.info("pubMedArticleList size {} (retrieved {})", pubMedArticleList.size(), pubMedArticles.size());
		pubMedService.save(pubMedArticleList);

		// Save the search result. The strategy entry always records every retrieved pmid,
		// including ones whose article write was skipped above.
		List<Long> pmids = new ArrayList<>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid());
		}

		ESearchPmid eSearchPmid = null;
		if(!pmids.isEmpty()){
			reciter.database.dynamodb.model.ESearchPmid.RetrievalRefreshFlag eSearchPmidRefreshFlag;
			if(refreshFlag == RetrievalRefreshFlag.ALL_PUBLICATIONS) {
				eSearchPmidRefreshFlag = reciter.database.dynamodb.model.ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS;
			} else if(refreshFlag == RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS) {
				eSearchPmidRefreshFlag = reciter.database.dynamodb.model.ESearchPmid.RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS;
			} else {
				eSearchPmidRefreshFlag = reciter.database.dynamodb.model.ESearchPmid.RetrievalRefreshFlag.FALSE;
			}
			eSearchPmid = new ESearchPmid(pmids, retrievalStrategyName, Instant.now(), eSearchPmidRefreshFlag);
			if(eSearchPmid != null) {
				log.info("eSearchPmid {} ",eSearchPmid);
			}
		}
		if (eSearchResultDb == null) {
			List<ESearchPmid> eSearchPmids = new ArrayList<>();
			if(eSearchPmid != null) {
				eSearchPmids.add(eSearchPmid);
			}
			if(!eSearchPmids.isEmpty()) {
				eSearchResultService.save(new ESearchResult(uid, Instant.now(), eSearchPmids, queryType));
			}
		} else {
			List<ESearchPmid> eSearchPmids = eSearchResultDb.getESearchPmids();
			if(eSearchPmid != null) {
				// FIX (#640-B): upsert by retrievalStrategyName. Previously every re-run
				// appended a new ESearchPmid for the same strategy without removing the prior
				// entry, growing the ESearchResult item unbounded toward the 400KB DynamoDB cap.
				String newStrategyName = eSearchPmid.getRetrievalStrategyName();
				ESearchPmid existingEntry = eSearchPmids.stream()
						.filter(existing -> existing != null
								&& existing.getRetrievalStrategyName() != null
								&& existing.getRetrievalStrategyName().equalsIgnoreCase(newStrategyName))
						.findFirst().orElse(null);
				eSearchPmids.removeIf(existing -> existing != null
						&& existing.getRetrievalStrategyName() != null
						&& existing.getRetrievalStrategyName().equalsIgnoreCase(newStrategyName));
				// #737: a run that hit PubMed failures for this uid must not let a partial
				// batch shrink the stored strategy entry — union instead of replace.
				boolean runHadFailures = reciter.xml.retriever.pubmed.RetrievalErrorTracker.hadError();
				ESearchPmid upserted = upsertedStrategyEntry(existingEntry, eSearchPmid, runHadFailures);
				if (runHadFailures && existingEntry != null
						&& existingEntry.getPmids() != null && eSearchPmid.getPmids() != null) {
					int storedCount = upserted.getPmids() == null ? 0 : upserted.getPmids().size();
					int incomingCount = eSearchPmid.getPmids().size();
					if (storedCount > incomingCount) {
						log.warn("uid=[{}] strategy [{}]: run hit PubMed failures; kept {} prior pmids the run did not return (incoming {}, stored {})",
								uid, newStrategyName, storedCount - incomingCount, incomingCount, storedCount);
					}
				}
				eSearchPmids.add(upserted);
			}
			if(!eSearchPmids.isEmpty()) {
				eSearchResultService.save(new ESearchResult(uid, Instant.now(), eSearchPmids, queryType));
			} else {
				eSearchResultDb.setRetrievalDate(Instant.now());
				eSearchResultDb.setQueryType(queryType);
				eSearchResultService.save(eSearchResultDb);
			}
		}
	}

	/**
	 * The strategy entry to store when a retrieval upserts over an existing one.
	 *
	 * <p>Only a CLEAN ALL_PUBLICATIONS sweep replaces the stored entry wholesale. It is
	 * authoritative, it is what prunes pmids the person's queries no longer match, and
	 * it is what bounds the merged list's growth against the 400KB DynamoDB item cap
	 * (#640-B). Everything else merges pmids into the stored list:
	 *
	 * <p>(1) An incremental run's pmid list is only that night's window, so it must be
	 * MERGED into the stored list, never substituted for it. Substituting is how a
	 * strategy's block silently shrinks to a handful of recent pmids:
	 * {@code ReCiterController.initializeEngineParameters} builds the scoring candidate
	 * set as the union of these blocks, so any pmid dropped here vanishes from the
	 * person's scoring input even though the article is still in the PubMedArticle cache
	 * and still in PmidProvenance. Measured on prod 2026-09-05: 1,446 authorship-review
	 * rows had lost their pmid this way, 1,445 of them behind a block whose stored
	 * lookupType was already incremental — the exact case the pre-fix guard let through,
	 * because it only refused the downgrade when the STORED entry was ALL_PUBLICATIONS.
	 * A within-person paired control put the loss rate at 67% behind an incremental block
	 * versus 0% behind a full sweep. This also covers the swallowed-429 case: a strategy
	 * that returns zero articles tonight no longer erases what it found before.
	 *
	 * <p>(2) A run that hit PubMed failures for this uid (#737) must not let a partial
	 * batch shrink the stored entry either, even when it was a full sweep: on 2026-09-01
	 * 3 of 8 GoldStandardRetrievalStrategy batches for rharrington failed with NCBI
	 * connection resets and the wholesale replace shrank the entry from 794 to 394 pmids.
	 * When a strategy returns zero pmids the caller never builds an incoming entry at
	 * all, so a total failure leaves the existing entry untouched (:137, :158); only a
	 * partial batch reaches this path.
	 *
	 * <p>Marker and date on a merge: when the stored entry is ALL_PUBLICATIONS and the
	 * incoming one is not, the merged entry keeps that marker AND the full sweep's
	 * retrievalDate (#696/E13): {@code ArticleSizeStrategy} filters entries on the marker
	 * to compute {@code articleCountScore}, so erasing it perturbs scoring, and the
	 * escalation fallback infers last-full-sweep from that same date — bumping it on an
	 * incremental run would make the person look freshly swept and suppress a due
	 * escalation. Otherwise the lookupType is ALL_PUBLICATIONS if either side carries it
	 * (a failed full sweep still counts as one) and the date is incoming's — a failed run
	 * should still look attempted. Pure so the rules are unit-testable — no
	 * {@code RetrievalErrorTracker} read here.
	 */
	static ESearchPmid upsertedStrategyEntry(ESearchPmid existing, ESearchPmid incoming, boolean runHadFailures) {
		if (existing == null || incoming == null) {
			return incoming;
		}
		boolean existingIsAll = existing.getLookupType() == ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS;
		boolean incomingIsAll = incoming.getLookupType() == ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS;

		if (incomingIsAll && !runHadFailures) {
			// A clean full sweep is authoritative — replace wholesale.
			return incoming;
		}

		List<Long> mergedPmids = mergePmids(existing.getPmids(), incoming.getPmids());
		boolean keepFullSweepMarker = existingIsAll && !incomingIsAll;
		log.info("Merging upsert for strategy {} : incoming pmids {} -> stored {}; runHadFailures={} "
				+ "keepFullSweepMarker={} (sweep date {})", incoming.getRetrievalStrategyName(),
				incoming.getPmids() == null ? 0 : incoming.getPmids().size(), mergedPmids.size(),
				runHadFailures, keepFullSweepMarker, existing.getRetrievalDate());
		return new ESearchPmid(mergedPmids, incoming.getRetrievalStrategyName(),
				keepFullSweepMarker ? existing.getRetrievalDate() : incoming.getRetrievalDate(),
				(existingIsAll || incomingIsAll) ? ESearchPmid.RetrievalRefreshFlag.ALL_PUBLICATIONS
						: incoming.getLookupType());
	}

	/**
	 * Union of two pmid lists, existing's entries first in their original order, then
	 * incoming's not-yet-seen entries in their original order. Nulls are skipped.
	 */
	private static List<Long> mergePmids(List<Long> existingPmids, List<Long> incomingPmids) {
		List<Long> mergedPmids = new ArrayList<>();
		Set<Long> seen = new HashSet<>();
		for (List<Long> pmidList : Arrays.asList(existingPmids, incomingPmids)) {
			if (pmidList == null) {
				continue;
			}
			for (Long pmid : pmidList) {
				if (pmid != null && seen.add(pmid)) {
					mergedPmids.add(pmid);
				}
			}
		}
		return mergedPmids;
	}

	/**
	 * Stamp {@code lastFullSweep} after an ALL_PUBLICATIONS retrieval — but only when
	 * every strategy completed without entering the error-swallow path (#696). Stamping
	 * after a swallowed failure would lock the loss in until the next scheduled sweep,
	 * 30–365 days away, which is strictly worse than not stamping: an unstamped person
	 * simply stays due and the sweep is retried. The write itself is conditional and
	 * never regresses a newer stamp (see ESearchResultRepository).
	 */
	protected void stampLastFullSweepIfClean(String uid, RetrievalRefreshFlag refreshFlag) {
		if (refreshFlag != RetrievalRefreshFlag.ALL_PUBLICATIONS) {
			return;
		}
		if (reciter.xml.retriever.pubmed.RetrievalErrorTracker.hadError()) {
			log.warn("Full sweep for uid=[{}] hit PubMed failures; NOT stamping lastFullSweep so the "
					+ "sweep stays due and is retried.", uid);
			return;
		}
		eSearchResultService.stampLastFullSweepIfNewer(uid, Instant.now());
	}

	/**
	 * Articles worth persisting from this retrieval. On an ONLY_NEWLY_ADDED run, pmids
	 * already recorded on the uid's ESearchResult (under any strategy) were persisted by
	 * an earlier run and are dropped here; everything else — full sweeps, uids with no
	 * prior result — is persisted unchanged. Pure so the filter is unit-testable.
	 */
	static List<PubMedArticle> articlesToPersist(Collection<PubMedArticle> pubMedArticles, ESearchResult existingResult, RetrievalRefreshFlag refreshFlag) {
		List<PubMedArticle> articles = new ArrayList<>(pubMedArticles);
		if (refreshFlag != RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS
				|| existingResult == null || existingResult.getESearchPmids() == null) {
			return articles;
		}
		Set<Long> knownPmids = new HashSet<>();
		for (ESearchPmid entry : existingResult.getESearchPmids()) {
			if (entry != null && entry.getPmids() != null) {
				knownPmids.addAll(entry.getPmids());
			}
		}
		if (knownPmids.isEmpty()) {
			return articles;
		}
		List<PubMedArticle> toPersist = new ArrayList<>(articles.size());
		for (PubMedArticle article : articles) {
			if (!knownPmids.contains(article.getMedlinecitation().getMedlinecitationpmid().getPmid())) {
				toPersist.add(article);
			}
		}
		return toPersist;
	}
}
