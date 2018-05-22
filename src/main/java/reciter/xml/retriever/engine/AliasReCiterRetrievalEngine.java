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

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.PubMedAlias;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ESearchResultService;
import reciter.utils.AuthorNameUtils;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;

@Component("aliasReCiterRetrievalEngine")
public class AliasReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AliasReCiterRetrievalEngine.class);

	@Value("${use.scopus.articles}")
	private boolean useScopusArticles;
	
	@Autowired
	private ESearchResultService eSearchResultService;
	
	private class AsyncRetrievalEngine extends Thread {

		private final Identity identity;
		private final Date startDate;
		private final Date endDate;
		
		public AsyncRetrievalEngine(Identity identity, Date startDate, Date endDate) {
			this.identity = identity;
			this.startDate = startDate;
			this.endDate = endDate;
		}
		
		@Override
		public void run() {
			try {
				// If the eSearchResult collection doesn't contain any information regarding this person,
				// then we'd want to perform a full retrieval because this will be first time that ReCiter
				// retrieve PubMed and Scopus articles for this person.
//				List<ESearchResult> results = eSearchResultService.findByUid(identity.getUid());
//				if (results.isEmpty()) {
					slf4jLogger.info("Starting full retrieval for uid=[" + identity.getUid() + "].");
					retrieveData(identity);
//				}
//				} else {
//					slf4jLogger.info("Starting date range retrieval for uid=[" + identity.getUid() + "] startDate=["
//							+ startDate + " endDate=[" + endDate + "].");
//					retrieveDataByDateRange(identity, startDate, endDate);
//				}
			} catch (IOException e) {
				slf4jLogger.error("Unabled to retrieve. " + identity.getUid(), e);
			}
		}
	}

	@Override
	public boolean retrieveArticlesByDateRange(List<Identity> identities, Date startDate, Date endDate) throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (Identity identity : identities) {
			executorService.execute(new AsyncRetrievalEngine(identity, startDate, endDate));
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
	
	private Set<Long> retrieveData(Identity identity) throws IOException {
		Set<Long> uniquePmids = new HashSet<>();
		
		//eSearchResultService.delete();
		
		String uid = identity.getUid();
		
		// Retrieve by email.
		RetrievalResult retrievalResult = emailRetrievalStrategy.retrievePubMedArticles(identity);
		Map<Long, PubMedArticle> pubMedArticles = retrievalResult.getPubMedArticles();
		
		if (pubMedArticles.size() > 0) {
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
		}
		
		// TODO parallelize by putting save in a separate thread.
		savePubMedArticles(pubMedArticles.values(), uid, emailRetrievalStrategy.getRetrievalStrategyName(), retrievalResult.getPubMedQueryResults());
		
		RetrievalResult r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity);
		if (r1.getPubMedArticles().size() > 0) {
			pubMedArticles.putAll(r1.getPubMedArticles());
			savePubMedArticles(r1.getPubMedArticles().values(), uid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults());
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
		} else {
			RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity);
			pubMedArticles.putAll(r2.getPubMedArticles());
			savePubMedArticles(r2.getPubMedArticles().values(), uid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults());
			uniquePmids.addAll(r2.getPubMedArticles().keySet());
			
			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity);
			pubMedArticles.putAll(r3.getPubMedArticles());
			savePubMedArticles(r3.getPubMedArticles().values(), uid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults());
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			
			RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity);
			pubMedArticles.putAll(r4.getPubMedArticles());
			savePubMedArticles(r4.getPubMedArticles().values(), uid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults());
			uniquePmids.addAll(r4.getPubMedArticles().keySet());
			
			RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity);
			pubMedArticles.putAll(r5.getPubMedArticles());
			savePubMedArticles(r5.getPubMedArticles().values(), uid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults());
			uniquePmids.addAll(r5.getPubMedArticles().keySet());
		}
		
		//Retreive by GoldStandard
		RetrievalResult goldStandardRetrievalResult = goldStandardRetrievalStrategy.retrievePubMedArticlesUsingGoldStandard(identity, uniquePmids);
		pubMedArticles = goldStandardRetrievalResult.getPubMedArticles();
		savePubMedArticles(pubMedArticles.values(), uid, goldStandardRetrievalStrategy.getRetrievalStrategyName(), goldStandardRetrievalResult.getPubMedQueryResults());
		
		
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
		return uniquePmids;
	}
	
	public void retrieveDataByDateRange(Identity identity, Date startDate, Date endDate) throws IOException {
		Set<Long> uniquePmids = new HashSet<>();
		
		String uid = identity.getUid();
		
		// Retrieve by email.
		RetrievalResult retrievalResult = emailRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
		Map<Long, PubMedArticle> emailPubMedArticles = retrievalResult.getPubMedArticles();
		
		if (emailPubMedArticles.size() > 0) {
			Map<Long, AuthorName> aliasSet = AuthorNameUtils.calculatePotentialAlias(identity, emailPubMedArticles.values());

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
			
			uniquePmids.addAll(emailPubMedArticles.keySet());
		}
		
		// TODO parallelize by putting save in a separate thread.
		savePubMedArticles(emailPubMedArticles.values(), uid, emailRetrievalStrategy.getRetrievalStrategyName(), retrievalResult.getPubMedQueryResults());
		
		RetrievalResult r1 = firstNameInitialRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
		if (r1.getPubMedArticles().size() > 0) {
			savePubMedArticles(r1.getPubMedArticles().values(), uid, firstNameInitialRetrievalStrategy.getRetrievalStrategyName(), r1.getPubMedQueryResults());
			uniquePmids.addAll(r1.getPubMedArticles().keySet());
		} else {
			RetrievalResult r2 = affiliationInDbRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r2.getPubMedArticles().values(), uid, affiliationInDbRetrievalStrategy.getRetrievalStrategyName(), r2.getPubMedQueryResults());
			uniquePmids.addAll(r2.getPubMedArticles().keySet());
			
			RetrievalResult r3 = affiliationRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r3.getPubMedArticles().values(), uid, affiliationRetrievalStrategy.getRetrievalStrategyName(), r3.getPubMedQueryResults());
			uniquePmids.addAll(r3.getPubMedArticles().keySet());
			
			RetrievalResult r4 = departmentRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r4.getPubMedArticles().values(), uid, departmentRetrievalStrategy.getRetrievalStrategyName(), r4.getPubMedQueryResults());
			uniquePmids.addAll(r4.getPubMedArticles().keySet());
			
			RetrievalResult r5 = grantRetrievalStrategy.retrievePubMedArticles(identity, startDate, endDate);
			savePubMedArticles(r5.getPubMedArticles().values(), uid, grantRetrievalStrategy.getRetrievalStrategyName(), r5.getPubMedQueryResults());
			uniquePmids.addAll(r5.getPubMedArticles().keySet());
		}
		
		List<ScopusArticle> scopusArticles = emailRetrievalStrategy.retrieveScopus(uniquePmids);
		scopusService.save(scopusArticles);
		slf4jLogger.info("Finished retrieval for uid: " + identity.getUid());
	}
	
	

	@Override
	public void retrieveByPmids(String uid, List<Long> pmids) throws IOException {
		if (!pmids.isEmpty()) {
			RetrievalResult result = goldStandardRetrievalStrategy.retrievePubMedArticles(pmids);
			if (result.getPubMedArticles().size() > 0) {
				savePubMedArticles(result.getPubMedArticles().values(), uid, 
						goldStandardRetrievalStrategy.getRetrievalStrategyName(), result.getPubMedQueryResults());
			}
			List<ScopusArticle> scopusArticles = goldStandardRetrievalStrategy.retrieveScopus(pmids);
			scopusService.save(scopusArticles);
		}
	}
}
