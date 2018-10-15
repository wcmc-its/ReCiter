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

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.dynamodb.model.GoldStandard;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.pubmed.retriever.PubMedArticleRetriever;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.xml.retriever.engine.AliasReCiterRetrievalEngine.IdentityNameType;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

@Component("goldStandardRetrievalStrategy")
public class GoldStandardRetrievalStrategy extends AbstractRetrievalStrategy {
	
	private static final String retrievalStrategyName = "GoldStandardRetrievalStrategy";
	
	@Autowired
	private IDynamoDbGoldStandardService dynamoDbGoldStandardService;

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
	
	protected List<PubMedQueryType> buildQueryGoldStandard(Identity identity, Set<Long> unqiuePmids) {
		List<PubMedQueryType> pubMedQueries = new ArrayList<PubMedQueryType>();
		System.out.println(identity.getUid());
		List<Long> goldStandardPmids = dynamoDbGoldStandardService.findByUid(identity.getUid().trim()).getKnownPmids();
		PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder();
		goldStandardPmids.removeAll(unqiuePmids);
		PubMedQuery goldStandardQuery = pubMedQueryBuilder.buildPmids(goldStandardPmids);

		PubMedQueryType pubMedQueryType = new PubMedQueryType();
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueries.add(pubMedQueryType);

		return pubMedQueries;
	}
	

	@Override
	protected List<PubMedQueryType> buildQuery(Identity identity, Map<IdentityNameType, Set<AuthorName>> identityNames) {
		List<PubMedQueryType> pubMedQueries = new ArrayList<PubMedQueryType>();

		PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder();
		List<Long> goldStandardPmids = new ArrayList<Long>();
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(identity.getUid().trim());
		if(goldStandard != null 
				&& 
				goldStandard.getKnownPmids() != null 
				&& 
				goldStandard.getKnownPmids().size() > 0) {
			goldStandardPmids = goldStandard.getKnownPmids();
		}
		
		if(goldStandard != null 
				&&
				goldStandard.getRejectedPmids() != null
				&&
				goldStandard.getRejectedPmids().size() > 0) {
			goldStandardPmids.addAll(goldStandard.getRejectedPmids());
		}
		
		PubMedQuery goldStandardQuery = pubMedQueryBuilder.buildPmids(goldStandardPmids);

		PubMedQueryType pubMedQueryType = new PubMedQueryType();
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueryType.setLenientCountQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueryType.setStrictCountQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueries.add(pubMedQueryType);

		return pubMedQueries;
	}

	@Override
	protected List<PubMedQueryType> buildQuery(Identity identity, Map<IdentityNameType, Set<AuthorName>> identityNames, Date startDate, Date endDate) {
		List<PubMedQueryType> pubMedQueries = new ArrayList<PubMedQueryType>();
		
		List<Long> goldStandardPmids = new ArrayList<Long>();
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(identity.getUid().trim());
		if(goldStandard != null 
				&& 
				goldStandard.getKnownPmids() != null 
				&& 
				goldStandard.getKnownPmids().size() > 0) {
			goldStandardPmids = goldStandard.getKnownPmids();
		}
		
		if(goldStandard != null 
				&&
				goldStandard.getRejectedPmids() != null
				&&
				goldStandard.getRejectedPmids().size() > 0) {
			goldStandardPmids.addAll(goldStandard.getRejectedPmids());
		}

		PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder()
				.dateRange(true, startDate, endDate);
		PubMedQuery goldStandardQuery = pubMedQueryBuilder.buildPmids(goldStandardPmids);
		
		PubMedQueryBuilder pubMedQueryBuilderCount = new PubMedQueryBuilder();
		PubMedQuery goldStandardCountQuery = pubMedQueryBuilderCount.buildPmids(goldStandardPmids);

		PubMedQueryType pubMedQueryType = new PubMedQueryType();
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueryType.setStrictCountQuery(new PubMedQueryResult(goldStandardCountQuery));
		pubMedQueryType.setLenientCountQuery(new PubMedQueryResult(goldStandardCountQuery));
		pubMedQueries.add(pubMedQueryType);

		return pubMedQueries;
	}

	@Override
	public RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException {
		throw new UnsupportedOperationException("Does not support retrieval by pmids.");
	}
}
