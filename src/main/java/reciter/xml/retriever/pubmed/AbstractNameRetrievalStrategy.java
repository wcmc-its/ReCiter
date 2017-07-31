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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.PubMedAlias;
import reciter.pubmed.retriever.PubMedQuery;

public abstract class AbstractNameRetrievalStrategy extends AbstractRetrievalStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractNameRetrievalStrategy.class);

	protected abstract PubMedQuery buildNameQuery(String lastName, String firstName, Identity identity);
	protected abstract PubMedQuery buildNameQuery(String lastName, String firstName, Identity identity, Date startDate, Date endDate);
	protected abstract String getStrategySpecificKeyword(Identity identity);
	
	@Override
	protected List<PubMedQueryType> buildQuery(Identity identity) {
		List<PubMedQueryType> pubMedQueries = new ArrayList<PubMedQueryType>();

		String lastName = identity.getPrimaryName().getLastName();
		String firstName = identity.getPrimaryName().getFirstName();
		String firstInitial = identity.getPrimaryName().getFirstInitial();

		PubMedQueryType pubMedQueryType = new PubMedQueryType();
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(lastName, firstInitial, identity)));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(lastName, firstName, identity)));

		pubMedQueries.add(pubMedQueryType);
		slf4jLogger.info("Constructed lenient query: {}" + pubMedQueryType.getLenientQuery().getQuery());
		slf4jLogger.info("Constructed strict query: {}" + pubMedQueryType.getStrictQuery().getQuery());
		for (PubMedAlias pubMedAlias : identity.getPubMedAlias()) {

			AuthorName alias = pubMedAlias.getAuthorName();
			String aliasLastName = alias.getLastName();
			String aliasFirstInitial = alias.getFirstInitial();
			String aliasFirstName = alias.getFirstName();

			PubMedQueryType aliasPubMedQueryType = new PubMedQueryType();
			aliasPubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity)));
			aliasPubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity)));
			pubMedQueries.add(aliasPubMedQueryType);
		}
		
		for (PubMedQueryType query : pubMedQueries) {
			slf4jLogger.info(query.toString());
		}
		return pubMedQueries;
	}

	@Override
	protected List<PubMedQueryType> buildQuery(Identity identity, Date startDate, Date endDate) {
		List<PubMedQueryType> pubMedQueries = new ArrayList<PubMedQueryType>();

		String lastName = identity.getPrimaryName().getLastName();
		String firstName = identity.getPrimaryName().getFirstName();
		String firstInitial = identity.getPrimaryName().getFirstInitial();

		PubMedQueryType pubMedQueryType = new PubMedQueryType();
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(lastName, firstInitial, identity, startDate, endDate)));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(lastName, firstName, identity, startDate, endDate)));

		pubMedQueries.add(pubMedQueryType);
		
		for (PubMedAlias pubMedAlias : identity.getPubMedAlias()) {

			AuthorName alias = pubMedAlias.getAuthorName();
			String aliasLastName = alias.getLastName();
			String aliasFirstInitial = alias.getFirstInitial();
			String aliasFirstName = alias.getFirstName();

			PubMedQueryType aliasPubMedQueryType = new PubMedQueryType();
			aliasPubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity, startDate, endDate)));
			aliasPubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity, startDate, endDate)));
			pubMedQueries.add(aliasPubMedQueryType);
		}

		return pubMedQueries;
	}
	
	@Override
	public RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException {
		throw new UnsupportedOperationException("Does not support retrieval by pmids.");
	}
}
