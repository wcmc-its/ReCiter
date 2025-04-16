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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reciter.database.dynamodb.model.GoldStandard;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
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
		List<PubMedQueryType> pubMedQueries = new ArrayList<>();

		// Defensive check for UID
		String uid = identity.getUid();
		if (uid == null || uid.trim().isEmpty()) {
			throw new IllegalArgumentException("Identity UID is missing or blank.");
		}
		uid = uid.trim();

		// Retrieve the gold standard record safely
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
		List<Long> goldStandardPmids = new ArrayList<>();
		if (goldStandard != null && goldStandard.getKnownPmids() != null) {
			goldStandardPmids.addAll(goldStandard.getKnownPmids());
		}
		
		// Remove PMIDs that are already processed
		goldStandardPmids.removeAll(unqiuePmids);

		PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder();
		PubMedQuery goldStandardQuery = pubMedQueryBuilder.buildPmids(goldStandardPmids);

		PubMedQueryType pubMedQueryType = new PubMedQueryType();
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(goldStandardQuery));
		pubMedQueries.add(pubMedQueryType);

		return pubMedQueries;
	}
	

	@Override
	protected List<PubMedQueryType> buildQuery(Identity identity, Map<IdentityNameType, Set<AuthorName>> identityNames) {
		List<PubMedQueryType> pubMedQueries = new ArrayList<>();

		// Defensive check for UID
		String uid = identity.getUid();
		if (uid == null || uid.trim().isEmpty()) {
			throw new IllegalArgumentException("Identity UID is missing or blank.");
		}
		uid = uid.trim();

		List<Long> goldStandardPmids = new ArrayList<>();
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
		if (goldStandard != null && goldStandard.getKnownPmids() != null) {
			goldStandardPmids.addAll(goldStandard.getKnownPmids());
		}
		if (goldStandard != null && goldStandard.getRejectedPmids() != null) {
			goldStandardPmids.addAll(goldStandard.getRejectedPmids());
		}
		
		PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder();
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
		List<PubMedQueryType> pubMedQueries = new ArrayList<>();
		
		// Defensive check for UID
		String uid = identity.getUid();
		if (uid == null || uid.trim().isEmpty()) {
			throw new IllegalArgumentException("Identity UID is missing or blank.");
		}
		uid = uid.trim();

		List<Long> goldStandardPmids = new ArrayList<>();
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
		if (goldStandard != null && goldStandard.getKnownPmids() != null) {
			goldStandardPmids.addAll(goldStandard.getKnownPmids());
		}
		if (goldStandard != null && goldStandard.getRejectedPmids() != null) {
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
