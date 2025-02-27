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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

@Component("fullNameRetrievalStrategy")
public class FullNameRetrievalStrategy extends AbstractNameRetrievalStrategy {

    private static final String retrievalStrategyName = "FullNameRetrievalStrategy";
    private final static Logger slf4jLogger = LoggerFactory.getLogger(FullNameRetrievalStrategy.class);
    
    @Override
    public String getRetrievalStrategyName() {
        return retrievalStrategyName;
    }
    
    @Override
    protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity) {
        // Defensive checks
        if (identity == null) {
            throw new IllegalArgumentException("Identity is null.");
        }
        if (identitynames == null || identitynames.isEmpty()) {
            throw new IllegalArgumentException("Identity names set is null or empty.");
        }
        
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder()
                    .author(true, true, identitynames);
        
        PubMedQuery query = pubMedQueryBuilder.build();
        slf4jLogger.info(retrievalStrategyName + " produced query=[" + query + "]");
        return query;
    }

    @Override
    protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity, Date startDate,
            Date endDate) {
        // Defensive checks
        if (identity == null) {
            throw new IllegalArgumentException("Identity is null.");
        }
        if (identitynames == null || identitynames.isEmpty()) {
            throw new IllegalArgumentException("Identity names set is null or empty.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date or end date is null.");
        }
        
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder()
                    .author(true, true, identitynames)
                    .dateRange(true, startDate, endDate);
        
        return pubMedQueryBuilder.build();
    }
    
    @Override
    protected String getStrategySpecificKeyword(Identity identity) {
        // FullNameRetrievalStrategy does not require a strategy-specific keyword.
        return null;
    }
}
