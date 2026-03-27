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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import reciter.algorithm.evidence.targetauthor.TargetAuthorSelection;
import reciter.algorithm.util.ArticleTranslator;
import reciter.utils.ReCiterStringUtil;
import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.database.dynamodb.model.ESearchCount;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.model.QueryType;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.PubMedAlias;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.database.dynamodb.model.PmidProvenance;
import reciter.service.ESearchCountService;
import reciter.service.ESearchResultService;
import reciter.service.PmidProvenanceService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.utils.AuthorNameSanitizationUtils;
import reciter.utils.AuthorNameUtils;
import reciter.utils.ThreadDelay;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;

@Component("aliasReCiterRetrievalEngine")
public class AliasReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AliasReCiterRetrievalEngine.class);

	@Value("${use.scopus.articles}")
	private boolean useScopusArticles;
	
	@Value("${searchStrategy-lenient-threshold}")
	private double searchStrategyLenientThreshold;
	
	@Autowired
	private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
	
	@Autowired
	private ESearchResultService eSearchResultService;

	@Autowired
	private ESearchCountService eSearchCountService;

	@Autowired
	private PmidProvenanceService pmidProvenanceService;

	public enum IdentityNameType {
		ORIGINAL,
		DERIVED
	}
	
	private class AsyncRetrievalEngine extends Thread {

		private final Identity identity;
		private final Date startDate;
		private final Date endDate;
		private final RetrievalRefreshFlag refreshFlag;
		
		public AsyncRetrievalEngine(Identity identity, Date startDate, Date endDate, RetrievalRefreshFlag refreshFlag) {
			this.identity = identity;
			this.startDate = startDate;
			this.endDate = endDate;
			this.refreshFlag = refreshFlag;
		}
		
		@Override
		public void run() {
			try {
				// If the eSearchResult collection doesn't contain any information regarding this person,
				// then we'd want to perform a full retrieval because this will be first time that ReCiter
				// retrieve PubMed and Scopus articles for this person.
				slf4jLogger.info("this.refreshFlag in Alias run" + this.refreshFlag);
				if(this.refreshFlag == RetrievalRefreshFlag.ALL_PUBLICATIONS) {
					slf4jLogger.info("Starting full retrieval for uid=[" + identity.getUid() + "].");
					retrieveData(identity, this.refreshFlag);
				} else if(this.refreshFlag == RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS) {
					slf4jLogger.info("Starting date range retrieval for uid=[" + identity.getUid() + "] startDate=["
						+ startDate + "] endDate=[" + endDate + "].");
					retrieveDataByDateRange(identity, startDate, endDate, this.refreshFlag);
				}
			} catch (IOException e) {
				slf4jLogger.error("Unabled to retrieve. " + identity.getUid(), e);
			}
		}
	}

	@Override
	public boolean retrieveArticlesByDateRange(List<Identity> identities, Date startDate, Date endDate, RetrievalRefreshFlag refreshFlag) throws IOException {
		ExecutorService executorService = Executors.newWorkStealingPool(15);//Executors.newFixedThreadPool(10);
		for (Identity identity : identities) {
			executorService.execute(new AsyncRetrievalEngine(identity, startDate, endDate, refreshFlag));
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			slf4jLogger.error("Thread interrupted while waiting for retrieval to finish.");
			return false;
		}
		return true;
	}
	
	private Set<Long> retrieveData(Identity identity, RetrievalRefreshFlag refreshFlag) throws IOException {
		slf4jLogger.info("Coming into retrieveData section without date range****");
		Set<Long> uniquePmids = new HashSet<>();
		
		QueryType queryType = null;
		
		//eSearchResultService.delete();
		
		String uid = identity.getUid();

		// Phase 1 provenance tracking state
		Set<Long> nonGsStrategyPmids = new HashSet<>();
		Map<Long, String> newPmidStrategy = new LinkedHashMap<>();
		Set<Long> backfillPmids = new HashSet<>(pmidProvenanceService.findPmidsByUidAndStrategy(uid, "BACKFILL_FROM_ESEARCHRESULT"));
		// Build set of already-known PMIDs from existing ESearchResult
		Set<Long> existingPmids = new HashSet<>();
		reciter.database.dynamodb.model.ESearchResult existingESearch = eSearchResultService.findByUid(uid);
		if (existingESearch != null && existingESearch.getESearchPmids() != null) {
			for (reciter.database.dynamodb.model.ESearchPmid esp : existingESearch.getESearchPmids()) {
				if (esp.getPmids() != null) {
					existingPmids.addAll(esp.getPmids());
				}
			}
		}

		Map<IdentityNameType, Set<AuthorName>> identityNames = new LinkedHashMap<IdentityNameType, Set<AuthorName>>();
		identityAuthorNames(identity, identityNames);
		boolean useStrictQueryOnly = identityNames.entrySet().stream().anyMatch(entry -> entry.getKey() == IdentityNameType.DERIVED && entry.getValue().size() > 0);

		if(useStrictQueryOnly) {
			queryType = QueryType.STRICT_COMPOUND_NAME_LOOKUP;
		}

		//Retreive by GoldStandard
		Map<Long, PubMedArticle> pubMedArticles = null;
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(identity.getUid().trim());
		//if(goldStandard != null && goldStandard.getKnownPmids() != null && !goldStandard.getKnownPmids().isEmpty()) {
			RetrievalResult goldStandardRetrievalResult = goldStandardRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
			pubMedArticles = goldStandardRetrievalResult.getPubMedArticles();
			savePubMedArticles(pubMedArticles.values(), uid, goldStandardRetrievalStrategy.getRetrievalStrategyName(), goldStandardRetrievalResult.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(pubMedArticles.keySet());
			trackNewPmids(pubMedArticles, goldStandardRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		//}

		// Retrieve by ORCID (asserted from Identity, or inferred from accepted articles).
		String orcidForRetrieval = null;
		if (identity.getOrcid() != null && !identity.getOrcid().isEmpty()
				&& !"NOT SET".equalsIgnoreCase(identity.getOrcid().trim())) {
			orcidForRetrieval = identity.getOrcid().trim();
			slf4jLogger.info("Using asserted ORCID [{}] for uid=[{}]", orcidForRetrieval, uid);
		}
		if (orcidForRetrieval == null) {
			orcidForRetrieval = inferOrcidFromAcceptedArticles(pubMedArticles, goldStandard, identity);
			if (orcidForRetrieval != null) {
				slf4jLogger.info("Inferred ORCID [{}] from accepted articles for uid=[{}]",
						orcidForRetrieval, uid);
				// Set in-memory so OrcidRetrievalStrategy.constructOrcidQuery() can read it.
				// Do NOT persist to DynamoDB — Identity.orcid is for human-asserted ORCIDs only.
				identity.setOrcid(orcidForRetrieval);
				slf4jLogger.info("Using inferred ORCID [{}] (in-memory only) for retrieval, uid=[{}]",
						orcidForRetrieval, uid);
			}
		}
		if (orcidForRetrieval != null) {
			// Strategy reads identity.getOrcid() directly (thread-safe).
			RetrievalResult orcidResult = orcidRetrievalStrategy.retrievePubMedArticles(
					identity, identityNames, useStrictQueryOnly);
			pubMedArticles.putAll(orcidResult.getPubMedArticles());
			savePubMedArticles(orcidResult.getPubMedArticles().values(), uid,
					orcidRetrievalStrategy.getRetrievalStrategyName(),
					orcidResult.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(orcidResult.getPubMedArticles().keySet());
			nonGsStrategyPmids.addAll(orcidResult.getPubMedArticles().keySet());
			trackNewPmids(orcidResult.getPubMedArticles(), orcidRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		}

		// Retrieve by email.
		RetrievalResult retrievalResult = emailRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
		pubMedArticles = retrievalResult.getPubMedArticles();
		slf4jLogger.info("pubMedArticles in retrieveData section without date range****"+pubMedArticles.size());
		/*if (pubMedArticles.size() > 0) {
			Map<Long, AuthorName> aliasSet = AuthorNameUtils.calculatePotentialAlias(identity, pubMedArticles.values());

			slf4jLogger.info("Found " + aliasSet.size() + " new alias for uid=[" + uid + "]");

			// Update alias.
			List<PubMedAlias> pubMedAliases = new ArrayList<>();
			for (Map.Entry<Long, AuthorName> entry : aliasSet.entrySet()) {
				PubMedAlias pubMedAlias = new PubMedAlias();
				pubMedAlias.setAuthorName(entry.getValue());
				pubMedAlias.setPmid(entry.getKey());
				slf4jLogger.info("new alias for uid=[" + identity.getUid() + "], alias=[" + entry.getValue() + "] from pmid=[" + entry.getKey() + "]");
				pubMedAliases.add(pubMedAlias);
			}

			identity.setPubMedAlias(pubMedAliases);
			Date date = new Date();
			identity.setDateInitialRun(date);
			identity.setDateLastRun(date);
			identityService.save(identity);

			uniquePmids.addAll(pubMedArticles.keySet());
		}*/

		// TODO parallelize by putting save in a separate thread.
		savePubMedArticles(pubMedArticles.values(), uid, emailRetrievalStrategy.getRetrievalStrategyName(), retrievalResult.getPubMedQueryResults(), queryType, refreshFlag);
		uniquePmids.addAll(pubMedArticles.keySet());
		nonGsStrategyPmids.addAll(pubMedArticles.keySet());
		trackNewPmids(pubMedArticles, emailRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		
		RetrievalResult r1;
		if(useStrictQueryOnly) {
			r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, false);
		} else {
			r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
		}
		//if (r1.getPubMedArticles().size() > 0) {
		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() < searchStrategyLenientThreshold) {
			if(queryType == null) {
				queryType = QueryType.LENIENT_LOOKUP;
			}
			pubMedArticles.putAll(r1.getPubMedArticles());
			savePubMedArticles(r1.getPubMedArticles().values(), uid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
			nonGsStrategyPmids.addAll(r1.getPubMedArticles().keySet());
			trackNewPmids(r1.getPubMedArticles(), firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		}
		//toggle useStrictQUery as true if results from Last Name First Initial Strategy is larger than lenientStrategy
		if(r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLenientThreshold) {
			useStrictQueryOnly = true;
			queryType = QueryType.STRICT_EXCEEDS_THRESHOLD_LOOKUP;

			// Store the true eSearch count so ArticleSizeStrategy can use log(count) for scoring.
			// This avoids a separate live eSearch call during the scoring phase.
			int trueCount = r1.getPubMedQueryResults().get(0).getNumResult();
			eSearchCountService.save(new ESearchCount(uid, trueCount));
			slf4jLogger.info("Stored eSearchCount={} for uid={}", trueCount, uid);
		}

		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLenientThreshold
				||
				useStrictQueryOnly) {
			//Check to see if there is an actual need to do query for all steps
			if(identity.getInstitutions() != null && !identity.getInstitutions().isEmpty()) {
				RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r2.getPubMedArticles());
				savePubMedArticles(r2.getPubMedArticles().values(), uid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r2.getPubMedArticles().keySet());
				nonGsStrategyPmids.addAll(r2.getPubMedArticles().keySet());
				trackNewPmids(r2.getPubMedArticles(), affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			} else {
				slf4jLogger.info("Skipping " + affiliationInDbRetrievalStrategy.getRetrievalStrategyName() + " since no affiliation for " + identity.getUid());
			}

			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
			pubMedArticles.putAll(r3.getPubMedArticles());
			savePubMedArticles(r3.getPubMedArticles().values(), uid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			nonGsStrategyPmids.addAll(r3.getPubMedArticles().keySet());
			trackNewPmids(r3.getPubMedArticles(), affiliationRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			if(identity.getOrganizationalUnits() != null && !identity.getOrganizationalUnits().isEmpty()) {
				RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r4.getPubMedArticles());
				savePubMedArticles(r4.getPubMedArticles().values(), uid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r4.getPubMedArticles().keySet());
				nonGsStrategyPmids.addAll(r4.getPubMedArticles().keySet());
				trackNewPmids(r4.getPubMedArticles(), departmentRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			} else {
				slf4jLogger.info("Skipping " + departmentRetrievalStrategy.getRetrievalStrategyName() + " since no departments for " + identity.getUid());
			}

			if(identity.getGrants() != null && !identity.getGrants().isEmpty()) {
				RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r5.getPubMedArticles());
				savePubMedArticles(r5.getPubMedArticles().values(), uid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r5.getPubMedArticles().keySet());
				nonGsStrategyPmids.addAll(r5.getPubMedArticles().keySet());
				trackNewPmids(r5.getPubMedArticles(), grantRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			} else {
				slf4jLogger.info("Skipping " + grantRetrievalStrategy.getRetrievalStrategyName() + " since no grants for " + identity.getUid());
			}

			RetrievalResult r6 = fullNameRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
			pubMedArticles.putAll(r6.getPubMedArticles());
			savePubMedArticles(r6.getPubMedArticles().values(), uid, fullNameRetrievalStrategy.getRetrievalStrategyName(), r6.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r6.getPubMedArticles().keySet());
			nonGsStrategyPmids.addAll(r6.getPubMedArticles().keySet());
			trackNewPmids(r6.getPubMedArticles(), fullNameRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			if(identity.getKnownRelationships() != null && !identity.getKnownRelationships().isEmpty()) {
				RetrievalResult r7 = knownRelationshipRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r7.getPubMedArticles());
				savePubMedArticles(r7.getPubMedArticles().values(), uid, knownRelationshipRetrievalStrategy.getRetrievalStrategyName(), r7.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r7.getPubMedArticles().keySet());
				nonGsStrategyPmids.addAll(r7.getPubMedArticles().keySet());
				trackNewPmids(r7.getPubMedArticles(), knownRelationshipRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			} else {
				slf4jLogger.info("Skipping " + knownRelationshipRetrievalStrategy.getRetrievalStrategyName() + " since no Known Relationships for " + identity.getUid());
			}

			RetrievalResult r8 = secondIntialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
			pubMedArticles.putAll(r8.getPubMedArticles());
			savePubMedArticles(r8.getPubMedArticles().values(), uid, secondIntialRetrievalStrategy.getRetrievalStrategyName(), r8.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r8.getPubMedArticles().keySet());
			nonGsStrategyPmids.addAll(r8.getPubMedArticles().keySet());
			trackNewPmids(r8.getPubMedArticles(), secondIntialRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

		}

		// Phase 1: Save ESearchCount for users who didn't already get a threshold-path
		// count (line 275). For threshold-exceeding users, the raw PubMed count is
		// the correct value for ArticleSizeStrategy's log(count) scoring formula.
		// This guard will be removed in Phase 2 when ArticleSizeStrategy is updated
		// to use the retrieved-PMID semantic consistently.
		if (queryType != QueryType.STRICT_EXCEEDS_THRESHOLD_LOOKUP) {
			eSearchCountService.save(new ESearchCount(uid, nonGsStrategyPmids.size()));
			slf4jLogger.info("Stored final eSearchCount={} for uid={}", nonGsStrategyPmids.size(), uid);
		}

		// Phase 1: Write provenance records for newly discovered PMIDs
		if (!newPmidStrategy.isEmpty()) {
			Date now = new Date();
			List<PmidProvenance> provenanceRecords = new ArrayList<>();
			for (Map.Entry<Long, String> entry : newPmidStrategy.entrySet()) {
				provenanceRecords.add(new PmidProvenance(uid, entry.getKey(), now, entry.getValue()));
			}
			pmidProvenanceService.saveAllIfNotExists(provenanceRecords);
			slf4jLogger.info("Wrote {} provenance records for uid={}", provenanceRecords.size(), uid);
		}

		if (useScopusArticles) {
			List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);

			scopusService.save(scopusArticles);

			// Look up the remaining Scopus articles by DOI.
			List<Long> notFoundPmids = new ArrayList<>();
			Set<Long> foundPmids = new HashSet<>();
			for (ScopusArticle scopusArticle : scopusArticles) {
				foundPmids.add(scopusArticle.getPubmedId());
			}
			// Find the pmids that were not found by using pmid query to Scopus.
			for (long pmid : uniquePmids) {
				if (!foundPmids.contains(pmid)) {
					notFoundPmids.add(pmid);
				}
			}

			slf4jLogger.info("Scopus PMID lookup for uid=[{}]: queried={}, matched={}, notFound={}",
					uid, uniquePmids.size(), foundPmids.size(), notFoundPmids.size());

			List<String> dois = new ArrayList<>();
			Map<String, Long> doiToPmid = new HashMap<>();
			int noDoisCount = 0;
			for (long pmid : notFoundPmids) {
				PubMedArticle pubMedArticle = pubMedArticles.get(pmid);

				if (pubMedArticle != null &&
						pubMedArticle.getMedlinecitation() != null &&
						pubMedArticle.getMedlinecitation().getArticle() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid() != null) {
					String doi = pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid().toLowerCase();
					dois.add(doi);
					doiToPmid.put(doi, pmid);
				} else {
					noDoisCount++;
				}
			}

			slf4jLogger.info("Scopus DOI fallback for uid=[{}]: notFoundPmids={}, withDoi={}, withoutDoi={}",
					uid, notFoundPmids.size(), dois.size(), noDoisCount);

			List<ScopusArticle> scopusArticlesByDoi = emailRetrievalStrategy.retrieveScopusDoi(dois);
			List<Long> pmidsByDoi = new ArrayList<>();
			int doiMatchSuccess = 0;
			int doiMatchFailed = 0;
			for (ScopusArticle scopusArticle : scopusArticlesByDoi) {
				// manually insert PMID information.
				if (scopusArticle.getDoi() != null && !scopusArticle.getDoi().isEmpty()) {
					if(doiToPmid.get(scopusArticle.getDoi().toLowerCase()) != null) {
						scopusArticle.setPubmedId(doiToPmid.get(scopusArticle.getDoi().toLowerCase()));
						doiMatchSuccess++;
					} else {
						slf4jLogger.warn("Scopus DOI fallback: DOI mismatch for uid=[{}] — Scopus returned doi=[{}] which has no reverse PMID mapping",
								uid, scopusArticle.getDoi());
						doiMatchFailed++;
					}
				} else {
					doiMatchFailed++;
				}
				pmidsByDoi.add(scopusArticle.getPubmedId());
			}

			slf4jLogger.info("Scopus DOI fallback results for uid=[{}]: doisQueried={}, scopusReturned={}, pmidInjected={}, pmidFailed={}, stillUnmatched={}",
					uid, dois.size(), scopusArticlesByDoi.size(), doiMatchSuccess, doiMatchFailed,
					notFoundPmids.size() - doiMatchSuccess);

			scopusService.save(scopusArticlesByDoi);
		}
		slf4jLogger.info("Finished retrieval for uid=[{}], uniquePmids={}", identity.getUid(), uniquePmids.size());
		return uniquePmids;
	}
	
	public void retrieveDataByDateRange(Identity identity, Date startDate, Date endDate, RetrievalRefreshFlag refreshFlag) throws IOException {
		slf4jLogger.info("Coming in retrieveData section with date range****");
		Set<Long> uniquePmids = new HashSet<>();
		QueryType queryType = null;
		String uid = identity.getUid();

		// Phase 1 provenance tracking state (no always-save ESearchCount for date-range runs)
		Map<Long, String> newPmidStrategy = new LinkedHashMap<>();
		Set<Long> backfillPmids = new HashSet<>(pmidProvenanceService.findPmidsByUidAndStrategy(uid, "BACKFILL_FROM_ESEARCHRESULT"));
		Set<Long> existingPmids = new HashSet<>();
		reciter.database.dynamodb.model.ESearchResult existingESearch = eSearchResultService.findByUid(uid);
		if (existingESearch != null && existingESearch.getESearchPmids() != null) {
			for (reciter.database.dynamodb.model.ESearchPmid esp : existingESearch.getESearchPmids()) {
				if (esp.getPmids() != null) {
					existingPmids.addAll(esp.getPmids());
				}
			}
		}

		Map<IdentityNameType, Set<AuthorName>> identityNames = new LinkedHashMap<IdentityNameType, Set<AuthorName>>();
		identityAuthorNames(identity, identityNames);

		boolean useStrictQueryOnly = identityNames.entrySet().stream().anyMatch(entry -> entry.getKey() == IdentityNameType.DERIVED && entry.getValue().size() > 0);

		if(useStrictQueryOnly) {
			queryType = QueryType.STRICT_COMPOUND_NAME_LOOKUP;
		}

		Map<Long, PubMedArticle> pubMedArticles = null;
		//Retreive by GoldStandard
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(identity.getUid().trim());
		//if(goldStandard != null && goldStandard.getKnownPmids() != null && !goldStandard.getKnownPmids().isEmpty()) {
			RetrievalResult goldStandardRetrievalResult = goldStandardRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles = goldStandardRetrievalResult.getPubMedArticles();
			savePubMedArticles(pubMedArticles.values(), uid, goldStandardRetrievalStrategy.getRetrievalStrategyName(), goldStandardRetrievalResult.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(pubMedArticles.keySet());
			trackNewPmids(pubMedArticles, goldStandardRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		//}

		// Retrieve by ORCID (asserted from Identity, or inferred from accepted articles).
		String orcidForRetrieval = null;
		if (identity.getOrcid() != null && !identity.getOrcid().isEmpty()
				&& !"NOT SET".equalsIgnoreCase(identity.getOrcid().trim())) {
			orcidForRetrieval = identity.getOrcid().trim();
			slf4jLogger.info("Using asserted ORCID [{}] for uid=[{}]", orcidForRetrieval, uid);
		}
		if (orcidForRetrieval == null) {
			orcidForRetrieval = inferOrcidFromAcceptedArticles(pubMedArticles, goldStandard, identity);
			if (orcidForRetrieval != null) {
				slf4jLogger.info("Inferred ORCID [{}] from accepted articles for uid=[{}]",
						orcidForRetrieval, uid);
				// Set in-memory so OrcidRetrievalStrategy.constructOrcidQuery() can read it.
				// Do NOT persist to DynamoDB — Identity.orcid is for human-asserted ORCIDs only.
				identity.setOrcid(orcidForRetrieval);
				slf4jLogger.info("Using inferred ORCID [{}] (in-memory only) for retrieval, uid=[{}]",
						orcidForRetrieval, uid);
			}
		}
		if (orcidForRetrieval != null) {
			// Strategy reads identity.getOrcid() directly (thread-safe).
			RetrievalResult orcidResult = orcidRetrievalStrategy.retrievePubMedArticles(
					identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles.putAll(orcidResult.getPubMedArticles());
			savePubMedArticles(orcidResult.getPubMedArticles().values(), uid,
					orcidRetrievalStrategy.getRetrievalStrategyName(),
					orcidResult.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(orcidResult.getPubMedArticles().keySet());
			trackNewPmids(orcidResult.getPubMedArticles(), orcidRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		}

		// Retrieve by email.
		RetrievalResult retrievalResult = emailRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
		//Map<Long, PubMedArticle> emailPubMedArticles = retrievalResult.getPubMedArticles();
		pubMedArticles = retrievalResult.getPubMedArticles();
		slf4jLogger.info("pubMedArticles in retrieveData section with date range****"+pubMedArticles.size());
		/*if (pubMedArticles.size() > 0) {
			Map<Long, AuthorName> aliasSet = AuthorNameUtils.calculatePotentialAlias(identity, pubMedArticles.values());

			slf4jLogger.info("Found " + aliasSet.size() + " new alias for uid=[" + uid + "]");

			// Update alias.
			List<PubMedAlias> pubMedAliases = new ArrayList<PubMedAlias>();
			for (Map.Entry<Long, AuthorName> entry : aliasSet.entrySet()) {
				PubMedAlias pubMedAlias = new PubMedAlias();
				pubMedAlias.setAuthorName(entry.getValue());
				pubMedAlias.setPmid(entry.getKey());
				slf4jLogger.info("new alias for uid=[" + identity.getUid() + "], alias=[" + entry.getValue() + "] from pmid=[" + entry.getKey() + "]");
				pubMedAliases.add(pubMedAlias);
			}

			identity.setPubMedAlias(pubMedAliases);
			// TODO convert to localdate
			Date now = new Date();
			identity.setDateInitialRun(now);
			identity.setDateLastRun(now);
			identityService.save(identity);

			uniquePmids.addAll(pubMedArticles.keySet());
		}*/

		// TODO parallelize by putting save in a separate thread.
		savePubMedArticles(pubMedArticles.values(), uid, emailRetrievalStrategy.getRetrievalStrategyName(), retrievalResult.getPubMedQueryResults(), queryType, refreshFlag);
		uniquePmids.addAll(pubMedArticles.keySet());
		trackNewPmids(pubMedArticles, emailRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

		RetrievalResult r1;
		if(useStrictQueryOnly) {
			r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, false);
		} else {
			r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
		}
		//if (r1.getPubMedArticles().size() > 0) {
		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() < searchStrategyLenientThreshold) {
			if(queryType == null) {
				queryType = QueryType.LENIENT_LOOKUP;
			}
			pubMedArticles.putAll(r1.getPubMedArticles());
			savePubMedArticles(r1.getPubMedArticles().values(), uid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
			trackNewPmids(r1.getPubMedArticles(), firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		}

		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLenientThreshold) {
			queryType = QueryType.STRICT_EXCEEDS_THRESHOLD_LOOKUP;

			// Store the true eSearch count for scoring.
			int trueCount = r1.getPubMedQueryResults().get(0).getNumResult();
			eSearchCountService.save(new ESearchCount(uid, trueCount));
			slf4jLogger.info("Stored eSearchCount={} for uid={}", trueCount, uid);
		}

		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLenientThreshold
				||
				useStrictQueryOnly) {

			if(identity.getInstitutions() != null && !identity.getInstitutions().isEmpty()) {
				RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r2.getPubMedArticles());
				savePubMedArticles(r2.getPubMedArticles().values(), uid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r2.getPubMedArticles().keySet());
				trackNewPmids(r2.getPubMedArticles(), affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
			} else {
				slf4jLogger.info("Skipping " + affiliationInDbRetrievalStrategy.getRetrievalStrategyName() + " since no affiliation for " + identity.getUid());
			}

			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles.putAll(r3.getPubMedArticles());
			savePubMedArticles(r3.getPubMedArticles().values(), uid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			trackNewPmids(r3.getPubMedArticles(), affiliationRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			if(identity.getOrganizationalUnits() != null && !identity.getOrganizationalUnits().isEmpty()) {
				RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r4.getPubMedArticles());
				savePubMedArticles(r4.getPubMedArticles().values(), uid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r4.getPubMedArticles().keySet());
				trackNewPmids(r4.getPubMedArticles(), departmentRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			} else {
				slf4jLogger.info("Skipping " + departmentRetrievalStrategy.getRetrievalStrategyName() + " since no departments for " + identity.getUid());
			}

			if(identity.getGrants() != null && !identity.getGrants().isEmpty()) {
				RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r5.getPubMedArticles());
				savePubMedArticles(r5.getPubMedArticles().values(), uid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r5.getPubMedArticles().keySet());
				trackNewPmids(r5.getPubMedArticles(), grantRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
			} else {
				slf4jLogger.info("Skipping " + grantRetrievalStrategy.getRetrievalStrategyName() + " since no grants for " + identity.getUid());
			}

			RetrievalResult r6 = fullNameRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles.putAll(r6.getPubMedArticles());
			savePubMedArticles(r6.getPubMedArticles().values(), uid, fullNameRetrievalStrategy.getRetrievalStrategyName(), r6.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r6.getPubMedArticles().keySet());
			trackNewPmids(r6.getPubMedArticles(), fullNameRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);

			if(identity.getKnownRelationships() != null && !identity.getKnownRelationships().isEmpty()) {
				RetrievalResult r7 = knownRelationshipRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r7.getPubMedArticles());
				savePubMedArticles(r7.getPubMedArticles().values(), uid, knownRelationshipRetrievalStrategy.getRetrievalStrategyName(), r7.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r7.getPubMedArticles().keySet());
				trackNewPmids(r7.getPubMedArticles(), knownRelationshipRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
			} else {
				slf4jLogger.info("Skipping " + knownRelationshipRetrievalStrategy.getRetrievalStrategyName() + " since no Known Relationships for " + identity.getUid());
			}
			RetrievalResult r8 = secondIntialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles.putAll(r8.getPubMedArticles());
			savePubMedArticles(r8.getPubMedArticles().values(), uid, secondIntialRetrievalStrategy.getRetrievalStrategyName(), r8.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r8.getPubMedArticles().keySet());
			trackNewPmids(r8.getPubMedArticles(), secondIntialRetrievalStrategy.getRetrievalStrategyName(), uid, existingPmids, newPmidStrategy, backfillPmids);
		}

		// Phase 1: Write provenance records for newly discovered PMIDs (no ESearchCount always-save for date-range runs)
		if (!newPmidStrategy.isEmpty()) {
			Date now = new Date();
			List<PmidProvenance> provenanceRecords = new ArrayList<>();
			for (Map.Entry<Long, String> entry : newPmidStrategy.entrySet()) {
				provenanceRecords.add(new PmidProvenance(uid, entry.getKey(), now, entry.getValue()));
			}
			pmidProvenanceService.saveAllIfNotExists(provenanceRecords);
			slf4jLogger.info("Wrote {} provenance records (date-range) for uid={}", provenanceRecords.size(), uid);
		}

		slf4jLogger.info("uniquePmids in retrieveData section with date range****"+uniquePmids.size());
		//List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
		//scopusService.save(scopusArticles);
		if (useScopusArticles) {
			List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);

			scopusService.save(scopusArticles);

			// Look up the remaining Scopus articles by DOI.
			List<Long> notFoundPmids = new ArrayList<>();
			Set<Long> foundPmids = new HashSet<>();
			for (ScopusArticle scopusArticle : scopusArticles) {
				foundPmids.add(scopusArticle.getPubmedId());
			}
			// Find the pmids that were not found by using pmid query to Scopus.
			for (long pmid : uniquePmids) {
				if (!foundPmids.contains(pmid)) {
					notFoundPmids.add(pmid);
				}
			}

			slf4jLogger.info("Scopus PMID lookup for uid=[{}]: queried={}, matched={}, notFound={}",
					uid, uniquePmids.size(), foundPmids.size(), notFoundPmids.size());

			List<String> dois = new ArrayList<>();
			Map<String, Long> doiToPmid = new HashMap<>();
			int noDoisCount = 0;
			for (long pmid : notFoundPmids) {
				PubMedArticle pubMedArticle = pubMedArticles.get(pmid);

				if (pubMedArticle != null &&
						pubMedArticle.getMedlinecitation() != null &&
						pubMedArticle.getMedlinecitation().getArticle() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid() != null) {
					String doi = pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid().toLowerCase();
					dois.add(doi);
					doiToPmid.put(doi, pmid);
				} else {
					noDoisCount++;
				}
			}

			slf4jLogger.info("Scopus DOI fallback for uid=[{}]: notFoundPmids={}, withDoi={}, withoutDoi={}",
					uid, notFoundPmids.size(), dois.size(), noDoisCount);

			List<ScopusArticle> scopusArticlesByDoi = emailRetrievalStrategy.retrieveScopusDoi(dois);
			List<Long> pmidsByDoi = new ArrayList<>();
			int doiMatchSuccess = 0;
			int doiMatchFailed = 0;
			for (ScopusArticle scopusArticle : scopusArticlesByDoi) {
				// manually insert PMID information.
				if (scopusArticle.getDoi() != null && !scopusArticle.getDoi().isEmpty()) {
					if(doiToPmid.get(scopusArticle.getDoi().toLowerCase()) != null) {
						scopusArticle.setPubmedId(doiToPmid.get(scopusArticle.getDoi().toLowerCase()));
						doiMatchSuccess++;
					} else {
						slf4jLogger.warn("Scopus DOI fallback: DOI mismatch for uid=[{}] — Scopus returned doi=[{}] which has no reverse PMID mapping",
								uid, scopusArticle.getDoi());
						doiMatchFailed++;
					}
				} else {
					doiMatchFailed++;
				}
				pmidsByDoi.add(scopusArticle.getPubmedId());
			}

			slf4jLogger.info("Scopus DOI fallback results for uid=[{}]: doisQueried={}, scopusReturned={}, pmidInjected={}, pmidFailed={}, stillUnmatched={}",
					uid, dois.size(), scopusArticlesByDoi.size(), doiMatchSuccess, doiMatchFailed,
					notFoundPmids.size() - doiMatchSuccess);

			scopusService.save(scopusArticlesByDoi);
		}
		slf4jLogger.info("Finished retrieval for uid=[{}], uniquePmids={}", identity.getUid(), uniquePmids.size());
		
	}
	
	

	@Override
	public void retrieveByPmids(String uid, List<Long> pmids) throws IOException {
		//Not being used
		/*if (!pmids.isEmpty()) {
			RetrievalResult result = goldStandardRetrievalStrategy.retrievePubMedArticles(pmids);
			if (result.getPubMedArticles().size() > 0) {
				savePubMedArticles(result.getPubMedArticles().values(), uid, 
						goldStandardRetrievalStrategy.getRetrievalStrategyName(), result.getPubMedQueryResults(), null);
			}
			List<ScopusArticle> scopusArticles = goldStandardRetrievalStrategy.retrieveScopus(pmids);
			scopusService.save(scopusArticles);
		}*/
	}
	
	/**
	 * Infer the target author's ORCID by scanning accepted (known) PubMed articles.
	 * Uses the full TargetAuthorSelection pipeline (19-step name matching cascade)
	 * to identify the target author on each accepted article, then extracts
	 * the ORCID from whichever author is identified. Returns the most common
	 * matching ORCID, or null if none found.
	 */
	private String inferOrcidFromAcceptedArticles(Map<Long, PubMedArticle> pubMedArticles,
			GoldStandard goldStandard, Identity identity) {
		if (goldStandard == null || goldStandard.getKnownPmids() == null
				|| goldStandard.getKnownPmids().isEmpty() || pubMedArticles == null) {
			return null;
		}

		// Sanitize identity names (same as the scoring pipeline does)
		AuthorNameSanitizationUtils sanitizationUtils = new AuthorNameSanitizationUtils(strategyParameters);
		identity.setSanitizedNames(sanitizationUtils.sanitizeIdentityAuthorNames(identity));

		// Translate accepted PubMed articles into ReCiterArticles and run target author selection
		List<ReCiterArticle> acceptedReCiterArticles = new ArrayList<>();
		for (Long knownPmid : goldStandard.getKnownPmids()) {
			PubMedArticle pubMedArticle = pubMedArticles.get(knownPmid);
			if (pubMedArticle == null) {
				continue;
			}
			try {
				ReCiterArticle reCiterArticle = ArticleTranslator.translate(
						pubMedArticle, null,
						strategyParameters.getNameIgnoredCoAuthors(),
						strategyParameters);
				// Sanitize article author names
				reCiterArticle.getArticleCoAuthors().setSanitizedAuthorMap(
						sanitizationUtils.sanitizeArticleAuthorNames(reCiterArticle));
				acceptedReCiterArticles.add(reCiterArticle);
			} catch (Exception e) {
				slf4jLogger.warn("Could not translate PMID {} for ORCID inference: {}",
						knownPmid, e.getMessage());
			}
		}

		if (acceptedReCiterArticles.isEmpty()) {
			return null;
		}

		// Run the full target author identification (19-step cascade)
		TargetAuthorSelection targetAuthorSelection = new TargetAuthorSelection();
		targetAuthorSelection.identifyTargetAuthor(acceptedReCiterArticles, identity);

		// Collect ORCIDs from identified target authors
		Map<String, Integer> orcidCounts = new HashMap<>();
		for (ReCiterArticle article : acceptedReCiterArticles) {
			if (article.getArticleCoAuthors() == null
					|| article.getArticleCoAuthors().getAuthors() == null) {
				continue;
			}
			for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
				if (author.isTargetAuthor()
						&& author.getOrcid() != null
						&& !author.getOrcid().isEmpty()) {
					orcidCounts.merge(author.getOrcid(), 1, Integer::sum);
				}
			}
		}

		if (orcidCounts.isEmpty()) {
			return null;
		}

		// Return the most common ORCID
		return orcidCounts.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.orElse(null);
	}

	/**
	 * Track newly discovered PMIDs for provenance recording.
	 * For each PMID in the articles map, if it's not already known (from existingPmids or newPmidStrategy),
	 * record which strategy first discovered it. Also heal any backfill provenance records.
	 */
	private void trackNewPmids(Map<Long, ?> articles, String strategyName,
			String uid, Set<Long> existingPmids, Map<Long, String> newPmidStrategy,
			Set<Long> backfillPmids) {
		for (Long pmid : articles.keySet()) {
			if (!existingPmids.contains(pmid) && !newPmidStrategy.containsKey(pmid)) {
				newPmidStrategy.put(pmid, strategyName);
			}
			if (backfillPmids.contains(pmid)) {
				pmidProvenanceService.updateStrategyIfBackfill(uid, pmid, strategyName);
				backfillPmids.remove(pmid);
			}
		}
	}

	/**
	 * This function get all authorNames and derive additional names as well.
	 * @see <a href ="https://github.com/wcmc-its/ReCiter/issues/259">All Identity Name Sec 3.</a>
	 * @param identity
	 * @return
	 */
	private void identityAuthorNames(Identity identity, Map<IdentityNameType, Set<AuthorName>> identityNames) {
		Set<AuthorName> identityAuthorNames  = new HashSet<AuthorName>();
		Set<AuthorName> identityDerivedNames = new HashSet<AuthorName>();
		AuthorName identityPrimaryName = identity.getPrimaryName();
		if(identityPrimaryName!=null && identityPrimaryName.getFirstName()!=null && identityPrimaryName.getFirstName()!="")
			identityPrimaryName.setFirstName(ReCiterStringUtil.deAccent(identityPrimaryName.getFirstName().replaceAll("[\"()]", "")));
		if(identityPrimaryName!=null  && identityPrimaryName.getLastName()!=null && identityPrimaryName.getLastName()!="")
			identityPrimaryName.setLastName(ReCiterStringUtil.deAccent(identityPrimaryName.getLastName().replaceAll("(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", "")));
		if(identityPrimaryName!=null && identityPrimaryName.getMiddleName()!=null && identityPrimaryName.getMiddleName() != "") {
			identityPrimaryName.setMiddleName(ReCiterStringUtil.deAccent(identityPrimaryName.getMiddleName()));
		}
		
		//For any name in primaryName or alternateNames, does targetAuthor have a surname, which satisfies these conditions: 
		//contains a space or dash; if you break up the name at the first space or dash, there would be two strings of four characters or greater
		if(identityPrimaryName!=null &&(identityPrimaryName.getLastName().contains(" ") || identityPrimaryName.getLastName().contains("-")
				||
				(identityPrimaryName.getFirstName().contains(" ") || identityPrimaryName.getFirstName().contains("."))//Cases for W. Clay[firstName] Bracken[lastName]- W.[firstName] Clay[middleName] Bracken[lastName] - W Clay[firstName] Bracken[lastName]
				||
				(identityPrimaryName.getFirstName().length() ==1 && identityPrimaryName.getMiddleName() != null)
				)) {
			identityDerivedNames.addAll(deriveAdditionalName(identityPrimaryName));
		}
		
		identityAuthorNames.add(identityPrimaryName);
		
		if(identity.getAlternateNames() != null) {
			for(AuthorName authorName: identity.getAlternateNames()) {
				authorName.setFirstName(ReCiterStringUtil.deAccent(authorName.getFirstName().replaceAll("[\"()]", "")));
				authorName.setLastName(ReCiterStringUtil.deAccent(authorName.getLastName().replaceAll("(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", "")));
				if(authorName.getMiddleName() != null) {
					authorName.setMiddleName(ReCiterStringUtil.deAccent(authorName.getMiddleName()));
				}
				if(authorName.getLastName().contains(" ") || authorName.getLastName().contains("-")
						||
						(authorName.getFirstName().contains(" ") || authorName.getFirstName().contains("."))
						||
						(authorName.getFirstName().length() ==1 && authorName.getMiddleName() != null)) {
					identityDerivedNames.addAll(deriveAdditionalName(authorName));
				}
				
				identityAuthorNames.add(authorName);
			}
		}
		identityNames.put(IdentityNameType.ORIGINAL, identityAuthorNames);
		identityNames.put(IdentityNameType.DERIVED, identityDerivedNames);
	}
	
	/**
	 * This function derive additional names, if possible.
	 * @see <a href ="https://github.com/wcmc-its/ReCiter/issues/259">Additional Name Sec 4.</a>
	 * @param identityName
	 * @return
	 */
	private Set<AuthorName> deriveAdditionalName(AuthorName identityName) {
		
		Set<AuthorName> derivedAuthorNames = new HashSet<AuthorName>();
		if(identityName.getLastName().contains(" ") || identityName.getLastName().contains(".")) {
			String[] possibleLastName = identityName.getLastName().split("\\s+|-", 2);
			if(possibleLastName[0].length() >=4 
					&&
					possibleLastName[1].length() >=4) {
				
				String middleName = null;
				if(identityName.getMiddleName() != null) {
					middleName = identityName.getMiddleName();
				}
				AuthorName authorName1 = new AuthorName(identityName.getFirstName(), middleName, possibleLastName[0].trim());
				AuthorName authorName2 = new AuthorName(identityName.getFirstName(), middleName, possibleLastName[1].trim());
				derivedAuthorNames.add(authorName1);
				derivedAuthorNames.add(authorName2);
			}
		}
		if(identityName.getFirstName().contains(" ") || identityName.getFirstName().contains(".")) {
			String middleName = null;
			if(identityName.getMiddleName() != null) {
				middleName = identityName.getMiddleName();
			}
			if(identityName.getFirstName().length() ==2 && identityName.getFirstName().trim().endsWith(".") && middleName != null) {
				AuthorName authorName1 = new AuthorName(middleName, null, identityName.getLastName());//W.[firstName] Clay[middleName] Bracken[lastName]
				derivedAuthorNames.add(authorName1);
			}
			if(identityName.getFirstName().length() >=3 && Character.isWhitespace(identityName.getFirstName().charAt(1))) {
				//String[] possibleFirstName = identityName.getFirstName().split("\\s+", 2);
				AuthorName authorName1 = new AuthorName(Character.toString(identityName.getFirstName().charAt(2)), middleName, identityName.getLastName());//W Clay[firstName] Bracken[lastName]
				derivedAuthorNames.add(authorName1);
			}	
			if(identityName.getFirstName().length() >=4 && Character.isWhitespace(identityName.getFirstName().charAt(1)) && identityName.getFirstName().charAt(2) == '.') {
				//String[] possibleFirstName = identityName.getFirstName().split(".\\s+", 2);
				AuthorName authorName1 = new AuthorName(Character.toString(identityName.getFirstName().charAt(3)), middleName, identityName.getLastName());//W. Clay[firstName] Bracken[lastName]
				derivedAuthorNames.add(authorName1);
			}
		}
		if(identityName.getFirstName().length() ==1 && identityName.getMiddleName() != null) {//Case for W[firstName] Clay[middleName] Bracken[lastName]
			AuthorName authorName1 = new AuthorName(identityName.getMiddleName(), null, identityName.getLastName());
			derivedAuthorNames.add(authorName1);
		}
		
		return derivedAuthorNames;
	}
}
