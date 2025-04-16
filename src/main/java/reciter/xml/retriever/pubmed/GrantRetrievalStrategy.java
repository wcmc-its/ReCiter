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

import java.util.Date;
import java.util.Set;

import org.springframework.stereotype.Component;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

@Component("grantRetrievalStrategy")
public class GrantRetrievalStrategy extends AbstractNameRetrievalStrategy {

    private static final String retrievalStrategyName = "GrantRetrievalStrategy";

    @Override
    public String getRetrievalStrategyName() {
        return retrievalStrategyName;
    }

    /**
     * Constructs a grant query string by concatenating valid grant numbers.
     * Each valid grant is appended with "[Grant Number]" and separated by " OR ".
     * 
     * @param identity the identity object containing grants
     * @return a query string or null if no valid grant information exists
     * @throws IllegalArgumentException if the identity is null
     */
    @Override
    protected String getStrategySpecificKeyword(Identity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity is null.");
        }
        if (identity.getGrants() == null || identity.getGrants().isEmpty()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        int count = 0;
        int validCount = 0;
        for (String grant : identity.getGrants()) {
            count++;
            if (grant == null || grant.trim().isEmpty()) {
                continue;
            }
            // Append each valid grant with the "[Grant Number]" suffix.
            sb.append(grant.trim()).append("[Grant Number]");
            validCount++;
            // Append " OR " if there are more grants to process
            if (count < identity.getGrants().size()) {
                sb.append(" OR ");
            }
        }
        String keyword = sb.toString();
        // If no valid grant strings were found, return null.
        if (keyword.trim().isEmpty() || validCount == 0) {
            return null;
        }
        return "(" + keyword + ")";
    }

    @Override
    protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity) {
        String keyword = getStrategySpecificKeyword(identity);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("No valid grant keyword could be constructed from the identity grants.");
        }
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(keyword)
                .author(true, false, identitynames);
        return pubMedQueryBuilder.build();
    }

    @Override
    protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity, Date startDate,
            Date endDate) {
        String keyword = getStrategySpecificKeyword(identity);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("No valid grant keyword could be constructed from the identity grants.");
        }
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(keyword)
                .author(true, false, identitynames)
                .dateRange(true, startDate, endDate);
        return pubMedQueryBuilder.build();
    }
}
