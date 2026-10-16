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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import reciter.model.pubmed.PubMedArticle;
import reciter.utils.EnvUtils;

public class PubMedArticleRetriever {

	private static final Logger log = LoggerFactory.getLogger(PubMedArticleRetriever.class);
	
    private static final String PUBMED_SERVICE = System.getenv("PUBMED_SERVICE");

    // RestTemplate is thread-safe; share one instance instead of rebuilding its converters on every call.
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * Initializes and starts threads that handles the retrieval process. Partition the number of articles
     * into manageable pieces and ask each thread to handle one partition.
     */
    public List<PubMedArticle> retrievePubMed(PubMedQuery pubMedQuery, int numberOfPubmedArticles) {
        if (numberOfPubmedArticles == 0) {
            return Collections.emptyList();
        }
        String pubmedService = EnvUtils.require("PUBMED_SERVICE", PUBMED_SERVICE);
        String nodeUrl = pubmedService.endsWith("/pubmed")?pubmedService + "/query-complex/" :pubmedService + "/pubmed/query-complex/";
        RestTemplate restTemplate = REST_TEMPLATE;
        log.info("Sending web request: for query: " + pubMedQuery + ":" + nodeUrl);
        ResponseEntity<PubMedArticle[]> responseEntity = null;
        try {
            responseEntity = restTemplate.postForEntity(nodeUrl, pubMedQuery, PubMedArticle[].class);
            PubMedArticle[] pubMedArticles = (responseEntity == null) ? null : responseEntity.getBody();
            if (pubMedArticles == null) {
                // Fetch failed (tool 500 / NCBI 429) or returned an empty body. Flag the run so the
                // engine rolls the retrievalDate watermark back instead of treating this as "no articles". #689
                reciter.xml.retriever.pubmed.RetrievalErrorTracker.markError();
                return Collections.emptyList();
            }
            return Arrays.asList(pubMedArticles);
        } catch (Exception e) {
            log.error("Unable to retrieve via external REST api=[" + nodeUrl + "]", e);
            reciter.xml.retriever.pubmed.RetrievalErrorTracker.markError();
            return Collections.emptyList();
        }
    }
}
