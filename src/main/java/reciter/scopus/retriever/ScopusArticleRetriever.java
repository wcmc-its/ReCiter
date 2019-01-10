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

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import reciter.model.scopus.ScopusArticle;
import reciter.model.scopus.ScopusQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ScopusArticleRetriever<T> {

    /**
     * Scopus pmid modifier
     */
    public static final String PMID_MODIFIER = "pmid";

    /**
     * Scopus doi modifier
     */
    public static final String DOI_MODIFIER = "doi";

    private static final String SCOPUS_SERVICE = System.getenv("SCOPUS_SERVICE");

    /**
     * Modifier options: "pmid" or "doi".
     *
     * @param queryModifier
     * @param queryParams
     * @return
     */
    public List<ScopusArticle> retrieveScopus(String queryModifier, List<T> queryParams) {
        if (queryParams.isEmpty()) {
            return Collections.emptyList();
        }
        String nodeUrl = SCOPUS_SERVICE + "/scopus/query/";
        RestTemplate restTemplate = new RestTemplate();
        log.info("Sending web request for query " + queryParams + " modifier:" + queryModifier + ":" + nodeUrl);
        List<Object> pmidList = new ArrayList<>();
        for (T t : queryParams) {
            pmidList.add(t);
        }
        ScopusQuery scopusQuery = null;
        ResponseEntity<List<ScopusArticle>> responseEntity = null;
        if (PMID_MODIFIER.equals(queryModifier)) {
        	scopusQuery = new ScopusQuery(pmidList, "pmid");
        } else if (DOI_MODIFIER.equals(queryModifier)) {
        	scopusQuery = new ScopusQuery(pmidList, "doi");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(scopusQuery, headers);
            responseEntity =
                    restTemplate.exchange(nodeUrl, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<ScopusArticle>>() {
                    });
        } catch (Exception e) {
            log.error("Unable to retrieve via external REST api=[" + nodeUrl + "]", e);
        }
        if (responseEntity == null) {
            return Collections.emptyList();
        }
        return responseEntity.getBody();
    }
}
