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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.util.ReCiterStringUtil;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.PubMedAlias;
import reciter.model.scopus.Author;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryMode;

public abstract class AbstractNameRetrievalStrategy extends AbstractRetrievalStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractNameRetrievalStrategy.class);

	//protected abstract PubMedQuery buildNameQuery(String lastName, String firstName, Identity identity);
	protected abstract PubMedQuery buildNameQuery(List<AuthorName> identitynames, Identity identity);
	//protected abstract PubMedQuery buildNameQuery(String lastName, String firstName, Identity identity, Date startDate, Date endDate);
	protected abstract PubMedQuery buildNameQuery(List<AuthorName> identitynames, Identity identity, Date startDate, Date endDate);
	
	protected abstract String getStrategySpecificKeyword(Identity identity);
	
	@Override
	protected List<PubMedQueryType> buildQuery(Identity identity) {
		List<PubMedQueryType> pubMedQueries = new ArrayList<PubMedQueryType>();

		String lastName = identity.getPrimaryName().getLastName();
		String firstName = identity.getPrimaryName().getFirstName();
		String firstInitial = identity.getPrimaryName().getFirstInitial();
		List<AuthorName> derivedNames = new ArrayList<AuthorName>();
		List<AuthorName> identityNames = identityAuthorNames(identity, false, derivedNames);
		List<AuthorName> identityNamesWithDerived = identityAuthorNames(identity, true, derivedNames);

		PubMedQueryType pubMedQueryType = new PubMedQueryType();
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(identityNames, identity)));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(identityNamesWithDerived, identity)));
		pubMedQueryType.setLenientCountQuery(new PubMedQueryResult(buildNameQuery(identityNames, identity)));
		pubMedQueryType.setStrictCountQuery(new PubMedQueryResult(buildNameQuery(identityNamesWithDerived, identity)));
		pubMedQueryType.setDerivedQuery(new PubMedQueryResult(buildNameQuery(derivedNames, identity)));

		pubMedQueries.add(pubMedQueryType);
		slf4jLogger.info("Constructed lenient query: {}" + pubMedQueryType.getLenientQuery().getQuery());
		slf4jLogger.info("Constructed strict query: {}" + pubMedQueryType.getStrictQuery().getQuery());
		/*for (PubMedAlias pubMedAlias : identity.getPubMedAlias()) {

			AuthorName alias = pubMedAlias.getAuthorName();
			String aliasLastName = alias.getLastName();
			String aliasFirstInitial = alias.getFirstInitial();
			String aliasFirstName = alias.getFirstName();

			PubMedQueryType aliasPubMedQueryType = new PubMedQueryType();
			aliasPubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity)));
			aliasPubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity)));
			aliasPubMedQueryType.setLenientCountQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity)));
			aliasPubMedQueryType.setStrictCountQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity)));
			pubMedQueries.add(aliasPubMedQueryType);
		}*/
		
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
		List<AuthorName> derivedNames = new ArrayList<AuthorName>();
		List<AuthorName> identityNames = identityAuthorNames(identity, false, derivedNames);
		
 		List<AuthorName> identityNamesWithDerived = identityAuthorNames(identity, true, derivedNames);
		pubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(identityNames, identity, startDate, endDate)));
		pubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(identityNamesWithDerived, identity, startDate, endDate)));
		pubMedQueryType.setLenientCountQuery(new PubMedQueryResult(buildNameQuery(identityNames, identity)));
		pubMedQueryType.setStrictCountQuery(new PubMedQueryResult(buildNameQuery(identityNamesWithDerived, identity)));

		pubMedQueries.add(pubMedQueryType);
		
		/*for (PubMedAlias pubMedAlias : identity.getPubMedAlias()) {

			AuthorName alias = pubMedAlias.getAuthorName();
			String aliasLastName = alias.getLastName();
			String aliasFirstInitial = alias.getFirstInitial();
			String aliasFirstName = alias.getFirstName();

			PubMedQueryType aliasPubMedQueryType = new PubMedQueryType();
			aliasPubMedQueryType.setLenientQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity, startDate, endDate)));
			aliasPubMedQueryType.setStrictQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity, startDate, endDate)));
			aliasPubMedQueryType.setLenientCountQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstInitial, identity)));
			aliasPubMedQueryType.setStrictCountQuery(new PubMedQueryResult(buildNameQuery(aliasLastName, aliasFirstName, identity)));
			pubMedQueries.add(aliasPubMedQueryType);
		}*/

		return pubMedQueries;
	}
	
	@Override
	public RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException {
		throw new UnsupportedOperationException("Does not support retrieval by pmids.");
	}
	
	/**
	 * This function get all authorNames and derive additional names as well.
	 * @see <a href ="https://github.com/wcmc-its/ReCiter/issues/259">All Identity Name Sec 3.</a>
	 * @param identity
	 * @return
	 */
	private List<AuthorName> identityAuthorNames(Identity identity, boolean derivedNameFlag, List<AuthorName> derivedNames) {
		Set<AuthorName> identityAuthorNames  = new HashSet<AuthorName>();
		AuthorName identityPrimaryName = identity.getPrimaryName();
		identityPrimaryName.setFirstName(ReCiterStringUtil.deAccent(identityPrimaryName.getFirstName()));
		identityPrimaryName.setLastName(ReCiterStringUtil.deAccent(identityPrimaryName.getLastName().replaceAll("(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", "")));
		if(identityPrimaryName.getMiddleName() != null) {
			identityPrimaryName.setMiddleName(ReCiterStringUtil.deAccent(identityPrimaryName.getMiddleName()));
		}
		
		//For any name in primaryName or alternateNames, does targetAuthor have a surname, which satisfies these conditions: 
		//contains a space or dash; if you break up the name at the first space or dash, there would be two strings of four characters or greater
		if(derivedNameFlag 
				&& 
				(identityPrimaryName.getLastName().contains(" ") || identityPrimaryName.getLastName().contains("-"))) {
			//identityAuthorNames.addAll(deriveAdditionalName(identityPrimaryName));
			derivedNames.addAll(deriveAdditionalName(identityPrimaryName));
		}
		
		identityAuthorNames.add(identityPrimaryName);
		
		for(AuthorName authorName: identity.getAlternateNames()) {
			authorName.setFirstName(ReCiterStringUtil.deAccent(authorName.getFirstName()));
			authorName.setLastName(ReCiterStringUtil.deAccent(authorName.getLastName().replaceAll("(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", "")));
			if(authorName.getMiddleName() != null) {
				authorName.setMiddleName(ReCiterStringUtil.deAccent(authorName.getMiddleName()));
			}
			if(derivedNameFlag
					&& 
					(authorName.getLastName().contains(" ") || authorName.getLastName().contains("-"))) {
				//identityAuthorNames.addAll(deriveAdditionalName(authorName));
				derivedNames.addAll(deriveAdditionalName(identityPrimaryName));
			}
			
			identityAuthorNames.add(identityPrimaryName);
		}
		return new ArrayList<>(identityAuthorNames);
	}
	
	/**
	 * This function derive additional names, if possible.
	 * @see <a href ="https://github.com/wcmc-its/ReCiter/issues/259">Additional Name Sec 4.</a>
	 * @param identityName
	 * @return
	 */
	private Set<AuthorName> deriveAdditionalName(AuthorName identityName) {
		String[] possibleLastName = identityName.getLastName().split("\\s+|-", 2);
		if(possibleLastName[0].length() >=4 
				&&
				possibleLastName[1].length() >=4) {
			Set<AuthorName> derivedAuthorNames = new HashSet<AuthorName>();
			String middleName = null;
			if(identityName.getMiddleName() != null) {
				middleName = identityName.getMiddleName();
			}
			AuthorName authorName1 = new AuthorName(identityName.getFirstName(), middleName, possibleLastName[0]);
			AuthorName authorName2 = new AuthorName(identityName.getFirstName(), middleName, possibleLastName[1]);
			derivedAuthorNames.add(authorName1);
			derivedAuthorNames.add(authorName2);
			return derivedAuthorNames;
		}
		return null;
	}
}
