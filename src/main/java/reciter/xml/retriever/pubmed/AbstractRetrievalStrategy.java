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
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.pubmed.retriever.PubMedArticleRetriever;
import reciter.pubmed.xmlparser.PubmedESearchHandler;
import reciter.scopus.retriever.ScopusArticleRetriever;

@Configurable
public abstract class AbstractRetrievalStrategy implements RetrievalStrategy {

	private static final String nodeUrlBegin = "https://reciter-pubmed-retrieval-";
	private static final String nodeUrlEnd = ".herokuapp.com/reciter/retrieve/pubmed/by/query?";
	private static final int nodeSize = 7;

	private static final String scopusNodeUrlBegin = "https://reciter-scopus-retrieval-";
	private static final String scopusNodeUrlEnd = ".herokuapp.com/reciter/retrieve/scopus/by/pmids/";
	private static final int scopusNodeSize = 3;

	public static class RetrievalResult {
		private final Map<Long, PubMedArticle> pubMedArticles;
		private final List<PubMedQueryResult> pubMedQueryResults;
		public RetrievalResult(Map<Long, PubMedArticle> pubMedArticles, List<PubMedQueryResult> pubMedQueryResults) {
			this.pubMedArticles = pubMedArticles;
			this.pubMedQueryResults = pubMedQueryResults;
		}
		public Map<Long, PubMedArticle> getPubMedArticles() {
			return pubMedArticles;
		}
		public List<PubMedQueryResult> getPubMedQueryResults() {
			return pubMedQueryResults;
		}
	}

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractRetrievalStrategy.class);

	/**
	 * Retrieval threshold.
	 */
	protected static final int DEFAULT_THRESHOLD = 2000;

	/**
	 * Scopus retrieval threshold.
	 */
	protected static final int SCOPUS_DEFAULT_THRESHOLD = 24;

	/**
	 * Scopus retrieval max threshold.
	 */
	protected static final int SCOPUS_MAX_THRESHOLD = 25;

	/**
	 * Should retrieved if threshold exceeds.
	 */
	protected boolean isRetrieveExceedThreshold;

	public void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}

	protected abstract List<PubMedQuery> buildQuery(Identity identity);
	protected abstract List<PubMedQuery> buildQuery(Identity identity, LocalDate startDate, LocalDate endDate);

	@Override
	public RetrievalResult retrievePubMedArticles(Identity identity, LocalDate startDate, LocalDate endDate) throws IOException {
		List<PubMedQuery> pubMedQueries = buildQuery(identity, startDate, endDate);
		return retrievePubMedArticles(identity, pubMedQueries);
	}

	@Override
	public RetrievalResult retrievePubMedArticles(Identity identity) throws IOException {
		List<PubMedQuery> pubMedQueries = buildQuery(identity);
		for (PubMedQuery pubMedQuery : pubMedQueries) {
			slf4jLogger.info(pubMedQuery.toString());
		}
		return retrievePubMedArticles(identity, pubMedQueries);
	}

	private RetrievalResult retrievePubMedArticles(Identity identity, List<PubMedQuery> pubMedQueries) throws IOException {

		Map<Long, PubMedArticle> pubMedArticles = new HashMap<Long, PubMedArticle>();

		slf4jLogger.info("Query size: " + pubMedQueries.size());
		List<PubMedQueryResult> pubMedQueryResults = new ArrayList<PubMedQueryResult>();

		for (PubMedQuery pubMedQuery : pubMedQueries) {

			String encodedInitialQuery = URLEncoder.encode(pubMedQuery.getLenientQuery().getQuery(), "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(encodedInitialQuery);

			// check number of PubMed results returned by initial query.
			// If it's greater than the threshold, query using the strict query.
			pubMedQuery.getLenientQuery().setNumResult(handler.getCount());

			if (handler.getCount() > DEFAULT_THRESHOLD) {
				String constructedStrictQuery = pubMedQuery.getStrictQuery().getQuery();
				String strictQuery = URLEncoder.encode(constructedStrictQuery, "UTF-8");
				PubmedESearchHandler strictSearchHandler = getPubmedESearchHandler(strictQuery);

				pubMedQuery.getStrictQuery().setNumResult(strictSearchHandler.getCount());

				// only retrieve articles if number is less than threshold, otherwise the article download
				// may take too long
				if (strictSearchHandler.getCount() <= DEFAULT_THRESHOLD) {
					List<PubMedArticle> result = retrievePubMed(constructedStrictQuery, strictSearchHandler.getCount());
					for (PubMedArticle pubMedArticle : result) {
						long pmid = pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid();
						if (!pubMedArticles.containsKey(pmid)) {
							pubMedArticles.put(pmid, pubMedArticle);
						}
					}
					pubMedQuery.getStrictQuery().setUsed(true);
				}
			} else {
				List<PubMedArticle> result = retrievePubMed(encodedInitialQuery, handler.getCount());
				for (PubMedArticle pubMedArticle : result) {
					long pmid = pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid();
					if (!pubMedArticles.containsKey(pmid)) {
						pubMedArticles.put(pmid, pubMedArticle);
					}
				}
				pubMedQuery.getLenientQuery().setUsed(true);
			}

			pubMedQueryResults.add(pubMedQuery.getLenientQuery());
			pubMedQueryResults.add(pubMedQuery.getStrictQuery());
		}
		slf4jLogger.info("Found " + pubMedArticles.size() + " PubMed articles for " + identity.getUid() 
		+ " using retrieval strategy [" + getRetrievalStrategyName() + "]");

		return new RetrievalResult(pubMedArticles, pubMedQueryResults);
	}

	/**
	 * Randomly select a node.
	 * @return
	 */
	private String loadBalance() {
		int nodeSelected = (int) (Math.random() * nodeSize + 1);
		return nodeUrlBegin + nodeSelected + nodeUrlEnd;
	}

	private String loadBalanceScopus() {
		int nodeSelected = (int) (Math.random() * scopusNodeSize + 1);
		return scopusNodeUrlBegin + nodeSelected + scopusNodeUrlEnd;
	}

	private class Response<T> {
		private final boolean shouldRetrieve;
		private final T response;

		public Response(boolean shouldRetrieve, T response) {
			this.shouldRetrieve = shouldRetrieve;
			this.response = response;
		}
		public boolean isShouldRetrieve() {
			return shouldRetrieve;
		}
		public T getResponse() {
			return response;
		}
	}

	public List<PubMedArticle> retrievePubMed(String pubMedQuery, int numberOfPubmedArticles)  {
		PubMedArticleRetriever pubMedArticleRetriever = new PubMedArticleRetriever();
		return pubMedArticleRetriever.retrievePubMed(pubMedQuery, numberOfPubmedArticles);
	}

	@Override
	public List<ScopusArticle> retrieveScopus(Collection<Long> pmids) {
		ScopusArticleRetriever<Long> scopusArticleRetriever = new ScopusArticleRetriever<Long>();
		return scopusArticleRetriever.retrieveScopus(ScopusArticleRetriever.PMID_MODIFIER, new ArrayList<Long>(pmids));
	}
	
	@Override
	public List<ScopusArticle> retrieveScopusDoi(Collection<String> dois) {
		ScopusArticleRetriever<String> scopusArticleRetriever = new ScopusArticleRetriever<String>();
		return scopusArticleRetriever.retrieveScopus(ScopusArticleRetriever.DOI_MODIFIER, new ArrayList<String>(dois));
	}

	protected PubmedESearchHandler getPubmedESearchHandler(String query) throws IOException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery(query);
		String fullUrl = pubmedXmlQuery.buildESearchQuery(); // build eSearch query.
		PubmedESearchHandler pubmedESearchHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(fullUrl).openStream();

		try {
			SAXParserFactory.newInstance().newSAXParser().parse(esearchStream, pubmedESearchHandler);
		} catch (SAXException | ParserConfigurationException e) {
			slf4jLogger.error("Error parsing XML file for query=[" + query + "], full url=[" + fullUrl + "]", e);
		}
		return pubmedESearchHandler;
	}

	private List<PubMedArticle> retrievePubMedViaRest(String pubMedQuery) {
		String nodeUrl = loadBalance();
		RestTemplate restTemplate = new RestTemplate();
		slf4jLogger.info("Sending web request: " + nodeUrl);
		ResponseEntity<PubMedArticle[]> responseEntity = null;
		try {
			responseEntity = restTemplate.getForEntity(nodeUrl, PubMedArticle[].class);
		} catch (Exception e) {
			slf4jLogger.error("Unable to retrieve via external REST api=[" + nodeUrl + "]", e);
		}
		PubMedArticle[] pubMedArticles = responseEntity.getBody();
		return Arrays.asList(pubMedArticles);
	}

	private Response<List<ScopusArticle>> retrieveScopusViaRest(Collection<Long> pmids) {
		if (pmids.isEmpty()) {
			return new Response<List<ScopusArticle>>(false, null);
		}
		String nodeUrl = loadBalanceScopus() + "?pmids=";
		StringBuffer sb = new StringBuffer();
		List<String> pmidQueries = new ArrayList<String>();
		int i = 0;
		Iterator<Long> itr = pmids.iterator();
		while (itr.hasNext()) {
			long pmid = itr.next();
			if (i == 0 || (i % SCOPUS_DEFAULT_THRESHOLD != 0 && i != pmids.size() - 1)) {
				sb.append(pmid);
				sb.append(",");
			} else {
				sb.append(pmid);
			}
			if (i != 0 && i % SCOPUS_DEFAULT_THRESHOLD == 0) {
				pmidQueries.add(sb.toString());
				sb = new StringBuffer();
			}
			i++;
		}
		// add the remaining pmids
		String remaining = sb.toString();
		if (!remaining.isEmpty()) {
			pmidQueries.add(remaining);
		}
		List<ScopusArticle> scopusArticlesResult = new ArrayList<ScopusArticle>();
		for (String pmidQuery : pmidQueries) {
			RestTemplate restTemplate = new RestTemplate();
			try {
				slf4jLogger.info("Sending web request: " + nodeUrl + pmidQuery);
				ResponseEntity<ScopusArticle[]> responseEntity = restTemplate.getForEntity(nodeUrl + pmidQuery, ScopusArticle[].class);
				ScopusArticle[] scopusArticles = responseEntity.getBody();
				scopusArticlesResult.addAll(Arrays.asList(scopusArticles));
				slf4jLogger.info("Retrieved scopusArticles size=[" + scopusArticles.length + "]");
			} catch (Exception e) {
				slf4jLogger.error("Unable to retrieve via external REST api=[" + nodeUrl + pmidQuery + "]", e);
				return new Response<List<ScopusArticle>>(true, null);
			}
		}
		return new Response<List<ScopusArticle>>(false, scopusArticlesResult);
	}
}
