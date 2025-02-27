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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

/**
 * This class gets all the homeInstitutions defined in an application property and
 * forms a search string for PubMed.
 */
@Component("affiliationRetrievalStrategy")
public class AffiliationRetrievalStrategy extends AbstractNameRetrievalStrategy {

    private static final String retrievalStrategyName = "AffiliationRetrievalStrategy";
    
    @Value("${strategy.authorAffiliationScoringStrategy.homeInstitution-keywords}")
    private String homeInstitutionsKeywords;
    
    @Override
    public String getRetrievalStrategyName() {
        return retrievalStrategyName;
    }
    
    @Override
    protected String getStrategySpecificKeyword(Identity identity) {
        // Generate the affiliation keyword based solely on the configured home institutions.
        String keyword = getHomeInstitutionsForPubmed();
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Home institution keywords could not be generated. Check configuration.");
        }
        return keyword;
    }
    
    @Override
    protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity) {
        String keyword = getStrategySpecificKeyword(identity);
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(keyword)
                    .author(true, false, identitynames);
        
        return pubMedQueryBuilder.build();
    }
    
    @Override
    protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity, Date startDate,
            Date endDate) {
        String keyword = getStrategySpecificKeyword(identity);
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(keyword)
                    .author(true, false, identitynames)
                    .dateRange(true, startDate, endDate);
        
        return pubMedQueryBuilder.build();
    }
    
    /**
     * Constructs and returns the PubMed affiliation search string based on the 
     * home institution keywords defined in the configuration.
     * 
     * @return a string search query for PubMed
     */
    public String getHomeInstitutionsForPubmed() {
        if (homeInstitutionsKeywords == null || homeInstitutionsKeywords.trim().isEmpty()) {
            throw new IllegalArgumentException("Home institution keywords property is missing or empty.");
        }
        // Split the property into individual keywords separated by commas.
        List<String> homeInstKeywords = Arrays.asList(homeInstitutionsKeywords.trim().split("\\s*,\\s*"));
        StringBuilder affiliationQueryString = new StringBuilder();
        affiliationQueryString.append("(");
        
        for (int i = 0; i < homeInstKeywords.size(); i++) {
            String keywords = homeInstKeywords.get(i);
            if (keywords == null || keywords.trim().isEmpty()) {
                continue; // Skip any empty keyword sets.
            }
            // Split multiple terms separated by a vertical bar ("|")
            List<String> keywordParts = Arrays.asList(keywords.trim().split("\\|"));
            if (keywordParts.size() == 1) {
                affiliationQueryString.append(keywordParts.get(0).trim()).append("[affiliation]");
            } else {
                affiliationQueryString.append("(");
                for (int j = 0; j < keywordParts.size(); j++) {
                    String word = keywordParts.get(j);
                    if (word == null || word.trim().isEmpty()) {
                        continue;
                    }
                    affiliationQueryString.append(word.trim()).append("[affiliation]");
                    if (j < keywordParts.size() - 1) {
                        affiliationQueryString.append(" AND ");
                    }
                }
                affiliationQueryString.append(")");
            }
            if (i < homeInstKeywords.size() - 1) {
                affiliationQueryString.append(" OR ");
            }
        }
        affiliationQueryString.append(")");
        
        return affiliationQueryString.toString();
    }
}
