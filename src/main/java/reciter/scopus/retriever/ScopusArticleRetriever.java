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
package reciter.scopus.retriever;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.scopus.ScopusArticle;
import reciter.scopus.callable.ScopusUriParserCallable;
import reciter.scopus.querybuilder.ScopusXmlQuery;
import reciter.scopus.xmlparser.ScopusXmlHandler;

public class ScopusArticleRetriever<T> {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusArticleRetriever.class);

	/**
	 * Scopus retrieval threshold.
	 */
	protected static final int SCOPUS_DEFAULT_THRESHOLD = 24;

	/**
	 * Scopus retrieval max threshold.
	 */
	protected static final int SCOPUS_MAX_THRESHOLD = 25;
	
	/**
	 * Scopus pmid modifier
	 */
	public static final String PMID_MODIFIER = "pmid";
	
	/**
	 * Scopus doi modifier
	 */
	public static final String DOI_MODIFIER = "doi";

	/**
	 * Modifier options: "pmid" or "doi".
	 * 
	 * @param queryModifier
	 * @param queryParams
	 * @return
	 */
	public List<ScopusArticle> retrieveScopus(String queryModifier, List<T> queryParams) {

		List<String> pmidQueries = new ArrayList<String>();
		if (queryParams.size() == 1) {
			pmidQueries.add("pmid(" + queryParams.get(0) + ")");
		} else {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			Iterator<T> itr = queryParams.iterator();
			while (itr.hasNext()) {
				T param = itr.next();
				if (i == 0 || (i % SCOPUS_DEFAULT_THRESHOLD != 0 && i != queryParams.size() - 1)) {
					sb.append(queryModifier + "(");
					sb.append(param);
					sb.append(")+OR+");
				} else {
					sb.append(queryModifier + "(");
					sb.append(param);
					sb.append(")");
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
		}

		List<Callable<List<ScopusArticle>>> callables = new ArrayList<Callable<List<ScopusArticle>>>();

		for (String query : pmidQueries) {
			ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder(query, SCOPUS_MAX_THRESHOLD).build();
			String scopusUrl = scopusXmlQuery.getQueryUrl();
			ScopusUriParserCallable scopusUriParserCallable = new ScopusUriParserCallable(new ScopusXmlHandler(), scopusUrl);
			callables.add(scopusUriParserCallable);
		}

		List<List<ScopusArticle>> list = new ArrayList<List<ScopusArticle>>();

		int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numAvailableProcessors);

		try {
			executor.invokeAll(callables)
			.stream()
			.map(future -> {
				try {
					return future.get();
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}).forEach(list::add);
		} catch (InterruptedException e) {
			slf4jLogger.error("Unable to invoke callable.", e);
		}

		List<ScopusArticle> results = new ArrayList<ScopusArticle>();
		list.forEach(results::addAll);
		return results;
	}
}
