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
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.model.QueryType;
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
import reciter.xml.retriever.pubmed.PubMedQueryResult;
import reciter.xml.retriever.pubmed.SecondInitialRetrievalStrategy;
@Slf4j
@Component("abstractReCiterRetrievalEngine")
public abstract class AbstractReCiterRetrievalEngine implements ReCiterRetrievalEngine {

	@Autowired
	protected PubMedService pubMedService;

	@Autowired
	protected ESearchResultService eSearchResultService;

	@Autowired
	protected ScopusService scopusService;

	@Autowired
	protected IdentityService identityService;
	
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
	protected GrantRetrievalStrategy grantRetrievalStrategy;
	
	/**
	 * Save the PubMed articles and the ESearch results.
	 * @param pubMedArticles
	 * @param uid
	 */
	protected void savePubMedArticles(Collection<PubMedArticle> pubMedArticles, String uid, String retrievalStrategyName, List<PubMedQueryResult> pubMedQueryResults, QueryType queryType, RetrievalRefreshFlag refreshFlag) {
		// Save the articles.
		List<PubMedArticle> pubMedArticleList = new ArrayList<>(pubMedArticles);
		log.info("pubMedArticleList size********************"+pubMedArticleList.size());
		pubMedService.save(pubMedArticleList);

		// Save the search result.
		List<Long> pmids = new ArrayList<>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			pmids.add(pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid());
		}
		log.info("PMID's are********************"+pmids);
		log.info("PMID's size********************"+pmids.size());
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
			log.info("eSearchPmidRefreshFlag********************"+eSearchPmidRefreshFlag);
			eSearchPmid = new ESearchPmid(pmids, retrievalStrategyName,Instant.now(), eSearchPmidRefreshFlag);
			log.info("eSearchPmid********************"+eSearchPmid);
		}
		ESearchResult eSearchResultDb = eSearchResultService.findByUid(uid);
		if (eSearchResultDb == null) {
			List<ESearchPmid> eSearchPmids = new ArrayList<>();
			if(eSearchPmid != null) {
				eSearchPmids.add(eSearchPmid);
			}
			if(!eSearchPmids.isEmpty()) {
				eSearchResultService.save(new ESearchResult(uid, Instant.now(), eSearchPmids, queryType));
			}
			log.info("eSearchResultDb in case of null********************"+eSearchPmids.size());	
		} else {
			List<ESearchPmid> eSearchPmids = eSearchResultDb.getESearchPmids();
			if(eSearchPmid != null) {
				eSearchPmids.add(eSearchPmid);
			}
			if(!eSearchPmids.isEmpty()) {
				eSearchResultService.save(new ESearchResult(uid, Instant.now(), eSearchPmids, queryType));
			} else {
				eSearchResultDb.setRetrievalDate(Instant.now());
				eSearchResultDb.setQueryType(queryType);
				eSearchResultService.save(eSearchResultDb);
			}
			log.info("eSearchResultDb in else********************"+eSearchPmids.size());
		}
	}
}
