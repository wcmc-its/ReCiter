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
package reciter.pubmed.retriever;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.pubmed.PubMedArticle;
import reciter.pubmed.callable.PubMedUriParserCallable;
import reciter.pubmed.querybuilder.PubmedXmlQuery;
import reciter.pubmed.xmlparser.PubmedEFetchHandler;
import reciter.pubmed.xmlparser.PubmedESearchHandler;

@Slf4j
public class PubMedArticleRetriever {

	/**
	 * Initializes and starts threads that handles the retrieval process. Partition the number of articles
	 * into manageable pieces and ask each thread to handle one partition.
	 */
	public List<PubMedArticle> retrievePubMed(String pubMedQuery, int numberOfPubmedArticles)  {

		int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numAvailableProcessors);

		// Get the count (number of publications for this query).
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(pubMedQuery);

		// The number of articles will be less than 10,000. Set retMax equal to minimum of number of articles needed to be
		// retrieved divided by the number of available processors and 10,000.
		// If number of articles is less than 4, use number of articles as retmax.
		pubmedXmlQuery.setRetMax(Math.min(Math.max(numberOfPubmedArticles / Math.max(numAvailableProcessors, 1), numberOfPubmedArticles)
				, PubmedXmlQuery.DEFAULT_RETMAX));
		log.info("numAvailableProcessors=[" + numAvailableProcessors + "] retMax=["
				+ pubmedXmlQuery.getRetMax() + "], pubMedQuery=[" + pubMedQuery + "], "
				+ "numberOfPubmedArticles=[" + numberOfPubmedArticles + "].");

		// Retrieve the publications retMax records at one time and store to disk.
		int currentRetStart = 0;

		// Number of partitions that we need to finish retrieving all XML.
		int numSteps = (int) Math.ceil((double)numberOfPubmedArticles / pubmedXmlQuery.getRetMax()); 

		List<Callable<List<PubMedArticle>>> callables = new ArrayList<>();

		// Use the retstart value to iteratively fetch all XMLs.
		for (int i = 0; i < numSteps; i++) {
			// Get webenv value.
			pubmedXmlQuery.setRetStart(currentRetStart);
			String eSearchUrl = pubmedXmlQuery.buildESearchQuery();

			pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

			// Use the webenv value to retrieve xml.
			String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();
			log.info("eFetchUrl=[" + eFetchUrl + "].");
			PubMedUriParserCallable pubMedUriParserCallable = new PubMedUriParserCallable(new PubmedEFetchHandler(), eFetchUrl);
			callables.add(pubMedUriParserCallable);
 
			// Update the retstart value.
			currentRetStart += pubmedXmlQuery.getRetMax();
			pubmedXmlQuery.setRetStart(currentRetStart);
		}

		List<List<PubMedArticle>> list = new ArrayList<>();

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
			log.error("Unable to invoke callable.", e);
		}

		List<PubMedArticle> results = new ArrayList<>();
		list.forEach(results::addAll);
		return results;
	}
}
