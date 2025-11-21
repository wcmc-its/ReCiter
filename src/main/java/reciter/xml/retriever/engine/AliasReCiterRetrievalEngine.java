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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import reciter.algorithm.util.ReCiterStringUtil;
import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.model.QueryType;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ESearchResultService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;

@Component("aliasReCiterRetrievalEngine")
public class AliasReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AliasReCiterRetrievalEngine.class);

	@Value("${use.scopus.articles}")
	private boolean useScopusArticles;
	
	@Value("${searchStrategy-leninent-threshold}")
	private double searchStrategyLeninentThreshold;
	
	@Autowired
	private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
	
	@Autowired
	private ESearchResultService eSearchResultService;
	
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
		//}
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
				r1.getPubMedQueryResults().get(0).getNumResult() < searchStrategyLeninentThreshold) {
			if(queryType == null) {
				queryType = QueryType.LENIENT_LOOKUP;
			}
			pubMedArticles.putAll(r1.getPubMedArticles());
			savePubMedArticles(r1.getPubMedArticles().values(), uid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
		} 
		//toggle useStrictQUery as true if results from Last Name First Initial Strategy is larger than lenientStrategy
		if(r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLeninentThreshold) {
			useStrictQueryOnly = true;
			queryType = QueryType.STRICT_EXCEEDS_THRESHOLD_LOOKUP;
		}
		
		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLeninentThreshold
				||
				useStrictQueryOnly) {
			//Check to see if there is an actual need to do query for all steps
			if(identity.getInstitutions() != null && !identity.getInstitutions().isEmpty()) {
				RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r2.getPubMedArticles());
				savePubMedArticles(r2.getPubMedArticles().values(), uid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r2.getPubMedArticles().keySet());
				
			} else {
				slf4jLogger.info("Skipping " + affiliationInDbRetrievalStrategy.getRetrievalStrategyName() + " since no affiliation for " + identity.getUid());
			}
			
			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
			pubMedArticles.putAll(r3.getPubMedArticles());
			savePubMedArticles(r3.getPubMedArticles().values(), uid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			
			if(identity.getOrganizationalUnits() != null && !identity.getOrganizationalUnits().isEmpty()) {
				RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r4.getPubMedArticles());
				savePubMedArticles(r4.getPubMedArticles().values(), uid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r4.getPubMedArticles().keySet());
				
			} else {
				slf4jLogger.info("Skipping " + departmentRetrievalStrategy.getRetrievalStrategyName() + " since no departments for " + identity.getUid());
			}
			
			if(identity.getGrants() != null && !identity.getGrants().isEmpty()) {
				RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r5.getPubMedArticles());
				savePubMedArticles(r5.getPubMedArticles().values(), uid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r5.getPubMedArticles().keySet());
				
			} else {
				slf4jLogger.info("Skipping " + grantRetrievalStrategy.getRetrievalStrategyName() + " since no grants for " + identity.getUid());
			}
			
			RetrievalResult r6 = fullNameRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
			pubMedArticles.putAll(r6.getPubMedArticles());
			savePubMedArticles(r6.getPubMedArticles().values(), uid, fullNameRetrievalStrategy.getRetrievalStrategyName(), r6.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r6.getPubMedArticles().keySet());
			
			if(identity.getKnownRelationships() != null && !identity.getKnownRelationships().isEmpty()) {
				RetrievalResult r7 = knownRelationshipRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
				pubMedArticles.putAll(r7.getPubMedArticles());
				savePubMedArticles(r7.getPubMedArticles().values(), uid, knownRelationshipRetrievalStrategy.getRetrievalStrategyName(), r7.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r7.getPubMedArticles().keySet());
				
			} else {
				slf4jLogger.info("Skipping " + knownRelationshipRetrievalStrategy.getRetrievalStrategyName() + " since no Known Relationships for " + identity.getUid());
			}
			
			RetrievalResult r8 = secondIntialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, useStrictQueryOnly);
			pubMedArticles.putAll(r8.getPubMedArticles());
			savePubMedArticles(r8.getPubMedArticles().values(), uid, secondIntialRetrievalStrategy.getRetrievalStrategyName(), r8.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r8.getPubMedArticles().keySet());
			
		}
		
		
		if (useScopusArticles) {
			List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
      
			//Delete the table first if required
			//scopusService.delete();

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
			List<String> dois = new ArrayList<>();
			Map<String, Long> doiToPmid = new HashMap<>();
			for (long pmid : notFoundPmids) {
				PubMedArticle pubMedArticle = pubMedArticles.get(pmid);

				if (pubMedArticle != null && 
						pubMedArticle.getMedlinecitation() != null && 
						pubMedArticle.getMedlinecitation().getArticle() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid() != null) {
					String doi = pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid().toLowerCase(); // Need to lowercase doi here because of null pointer exception. (see below comment)
					dois.add(doi);
					doiToPmid.put(doi, pmid); // store a map of doi to pmid so that when Scopus doesn't return pmid, use this mapping to manually insert pmid.
				}
			}
			List<ScopusArticle> scopusArticlesByDoi = emailRetrievalStrategy.retrieveScopusDoi(dois);;
			List<Long> pmidsByDoi = new ArrayList<>();
			for (ScopusArticle scopusArticle : scopusArticlesByDoi) {
				// manually insert PMID information.
				if (scopusArticle.getDoi() != null && !scopusArticle.getDoi().isEmpty()) {
					// Need to lowercase doi here because of null pointer exception.
					// PMID: 28221372
					// PubMed article may provide DOI as "10.1038/NPLANTS.2016.112", and Scopus article may provide DOI as 10.1038/nplants.2016.112
					//Sometimes scopus doi retrieval wont match with the DOI found in Pubmed
					if(doiToPmid.get(scopusArticle.getDoi().toLowerCase()) != null)
						scopusArticle.setPubmedId(doiToPmid.get(scopusArticle.getDoi().toLowerCase()));
				}
				pmidsByDoi.add(scopusArticle.getPubmedId());
			}
			slf4jLogger.info("retrieved size=[" + pmidsByDoi.size() + "] pmidsByDoi=" + pmidsByDoi + " via DOI for uid=[" + uid + "]");
			scopusService.save(scopusArticlesByDoi);
		}
		slf4jLogger.info("uniquePmids in retrieveData section without date range****"+uniquePmids.size());
		slf4jLogger.info("Finished retrieval for uid: " + identity.getUid());
		return uniquePmids;
	}
	
	public void retrieveDataByDateRange(Identity identity, Date startDate, Date endDate, RetrievalRefreshFlag refreshFlag) throws IOException {
		slf4jLogger.info("Coming in retrieveData section with date range****");
		Set<Long> uniquePmids = new HashSet<>();
		QueryType queryType = null; 
		String uid = identity.getUid();
		
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
		//}
		
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
				r1.getPubMedQueryResults().get(0).getNumResult() < searchStrategyLeninentThreshold) {
			if(queryType == null) {
				queryType = QueryType.LENIENT_LOOKUP;
			}
			pubMedArticles.putAll(r1.getPubMedArticles());
			savePubMedArticles(r1.getPubMedArticles().values(), uid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
		}
		
		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLeninentThreshold) {
			queryType = QueryType.STRICT_EXCEEDS_THRESHOLD_LOOKUP;
		}
		
		if(r1.getPubMedQueryResults() != null
				&&
				r1.getPubMedQueryResults().size() > 0
				&&
				r1.getPubMedQueryResults().get(0).getNumResult() > searchStrategyLeninentThreshold
				||
				useStrictQueryOnly) {
			
			if(identity.getInstitutions() != null && !identity.getInstitutions().isEmpty()) {
				RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r2.getPubMedArticles());
				savePubMedArticles(r2.getPubMedArticles().values(), uid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r2.getPubMedArticles().keySet());
			} else {
				slf4jLogger.info("Skipping " + affiliationInDbRetrievalStrategy.getRetrievalStrategyName() + " since no affiliation for " + identity.getUid());
			}

			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles.putAll(r3.getPubMedArticles());
			savePubMedArticles(r3.getPubMedArticles().values(), uid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			
			if(identity.getOrganizationalUnits() != null && !identity.getOrganizationalUnits().isEmpty()) {
				RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r4.getPubMedArticles());
				savePubMedArticles(r4.getPubMedArticles().values(), uid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r4.getPubMedArticles().keySet());
				
			} else {
				slf4jLogger.info("Skipping " + departmentRetrievalStrategy.getRetrievalStrategyName() + " since no departments for " + identity.getUid());
			}
			
			if(identity.getGrants() != null && !identity.getGrants().isEmpty()) {
				RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r5.getPubMedArticles());
				savePubMedArticles(r5.getPubMedArticles().values(), uid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r5.getPubMedArticles().keySet());
			} else {
				slf4jLogger.info("Skipping " + grantRetrievalStrategy.getRetrievalStrategyName() + " since no grants for " + identity.getUid());
			}
			
			RetrievalResult r6 = fullNameRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles.putAll(r6.getPubMedArticles());
			savePubMedArticles(r6.getPubMedArticles().values(), uid, fullNameRetrievalStrategy.getRetrievalStrategyName(), r6.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r6.getPubMedArticles().keySet());

			if(identity.getKnownRelationships() != null && !identity.getKnownRelationships().isEmpty()) {
				RetrievalResult r7 = knownRelationshipRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
				pubMedArticles.putAll(r7.getPubMedArticles());
				savePubMedArticles(r7.getPubMedArticles().values(), uid, knownRelationshipRetrievalStrategy.getRetrievalStrategyName(), r7.getPubMedQueryResults(), queryType, refreshFlag);
				uniquePmids.addAll(r7.getPubMedArticles().keySet());
			} else {
				slf4jLogger.info("Skipping " + knownRelationshipRetrievalStrategy.getRetrievalStrategyName() + " since no Known Relationships for " + identity.getUid());
			}
			RetrievalResult r8 = secondIntialRetrievalStrategy.retrievePubMedArticles(identity, identityNames, startDate, endDate, useStrictQueryOnly);
			pubMedArticles.putAll(r8.getPubMedArticles());
			savePubMedArticles(r8.getPubMedArticles().values(), uid, secondIntialRetrievalStrategy.getRetrievalStrategyName(), r8.getPubMedQueryResults(), queryType, refreshFlag);
			uniquePmids.addAll(r8.getPubMedArticles().keySet());
		}
		slf4jLogger.info("uniquePmids in retrieveData section with date range****"+uniquePmids.size());
		//List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
		//scopusService.save(scopusArticles);
		if (useScopusArticles) {
			List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
      
			//Delete the table first if required
			//scopusService.delete();

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
			List<String> dois = new ArrayList<>();
			Map<String, Long> doiToPmid = new HashMap<>();
			for (long pmid : notFoundPmids) {
				PubMedArticle pubMedArticle = pubMedArticles.get(pmid);

				if (pubMedArticle != null && 
						pubMedArticle.getMedlinecitation() != null && 
						pubMedArticle.getMedlinecitation().getArticle() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid() != null &&
						pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid() != null) {
					String doi = pubMedArticle.getMedlinecitation().getArticle().getElocationid().getElocationid().toLowerCase(); // Need to lowercase doi here because of null pointer exception. (see below comment)
					dois.add(doi);
					doiToPmid.put(doi, pmid); // store a map of doi to pmid so that when Scopus doesn't return pmid, use this mapping to manually insert pmid.
				}
			}
			List<ScopusArticle> scopusArticlesByDoi = emailRetrievalStrategy.retrieveScopusDoi(dois);;
			List<Long> pmidsByDoi = new ArrayList<>();
			for (ScopusArticle scopusArticle : scopusArticlesByDoi) {
				// manually insert PMID information.
				if (scopusArticle.getDoi() != null && !scopusArticle.getDoi().isEmpty()) {
					// Need to lowercase doi here because of null pointer exception.
					// PMID: 28221372
					// PubMed article may provide DOI as "10.1038/NPLANTS.2016.112", and Scopus article may provide DOI as 10.1038/nplants.2016.112
					//Sometimes scopus doi retrieval wont match with the DOI found in Pubmed
					if(doiToPmid.get(scopusArticle.getDoi().toLowerCase()) != null)
						scopusArticle.setPubmedId(doiToPmid.get(scopusArticle.getDoi().toLowerCase()));
				}
				pmidsByDoi.add(scopusArticle.getPubmedId());
			}
			slf4jLogger.info("retrieved size=[" + pmidsByDoi.size() + "] pmidsByDoi=" + pmidsByDoi + " via DOI for uid=[" + uid + "]");
			scopusService.save(scopusArticlesByDoi);
		}
		slf4jLogger.info("Finished retrieval for uid: " + identity.getUid());
		
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
