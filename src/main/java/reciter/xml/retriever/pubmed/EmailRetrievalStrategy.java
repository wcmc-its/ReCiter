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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.xml.retriever.engine.AliasReCiterRetrievalEngine.IdentityNameType;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

@Component("emailRetrievalStrategy")
public class EmailRetrievalStrategy extends AbstractRetrievalStrategy {

    private static final String retrievalStrategyName = "EmailRetrievalStrategy";

    @Override
    public String getRetrievalStrategyName() {
        return retrievalStrategyName;
    }

    /**
     * Concatenates email strings with " OR " and encloses each in double quotes.
     * Also replaces any commas with periods.
     * 
     * @param identity The identity object containing email addresses.
     * @return A string query built from the email addresses.
     * @throws IllegalArgumentException if the identity is null or if no valid email is found.
     */
    private String constructEmailQuery(Identity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity is null.");
        }
        if (identity.getEmails() != null && !identity.getEmails().isEmpty()) {
            Iterator<String> iterator = identity.getEmails().iterator();
            String firstEmail = iterator.next();
            if (firstEmail == null || firstEmail.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be null or empty.");
            }
            // Enclose the first email in double quotes and replace commas with dots
            final String first = "\"" + firstEmail.replace(',', '.') + "\"";
            if (!iterator.hasNext()) {
                return first;
            }
            final StringBuilder buf = new StringBuilder(30); // approximate length for two emails.
            buf.append(first);
            while (iterator.hasNext()) {
                String email = iterator.next();
                if (email == null || email.trim().isEmpty()) {
                    continue; // skip invalid email entries
                }
                buf.append(" OR ");
                buf.append("\"").append(email.replace(',', '.').trim()).append("\"");
            }
            return buf.toString();
        } else {
            return null;
        }
    }

    @Override
    protected List<PubMedQueryType> buildQuery(Identity identity, Map<IdentityNameType, Set<AuthorName>> identityNames) {
        List<PubMedQueryType> pubMedQueries = new ArrayList<>();

        String emailQueryString = constructEmailQuery(identity);
        if (emailQueryString == null || emailQueryString.trim().isEmpty()) {
            throw new IllegalArgumentException("No valid email query could be constructed from the identity.");
        }
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(emailQueryString);
        PubMedQuery emailQuery = pubMedQueryBuilder.build();

        PubMedQueryType pubMedQueryType = new PubMedQueryType();
        pubMedQueryType.setLenientQuery(new PubMedQueryResult(emailQuery));
        pubMedQueryType.setStrictQuery(new PubMedQueryResult(emailQuery));
        pubMedQueryType.setLenientCountQuery(new PubMedQueryResult(emailQuery));
        pubMedQueryType.setStrictCountQuery(new PubMedQueryResult(emailQuery));
        pubMedQueries.add(pubMedQueryType);

        return pubMedQueries;
    }

    @Override
    protected List<PubMedQueryType> buildQuery(Identity identity, Map<IdentityNameType, Set<AuthorName>> identityNames,
            Date startDate, Date endDate) {
        List<PubMedQueryType> pubMedQueries = new ArrayList<>();

        String emailQueryString = constructEmailQuery(identity);
        if (emailQueryString == null || emailQueryString.trim().isEmpty()) {
            throw new IllegalArgumentException("No valid email query could be constructed from the identity.");
        }
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(emailQueryString)
                .dateRange(true, startDate, endDate);
        PubMedQuery emailQuery = pubMedQueryBuilder.build();

        PubMedQueryBuilder pubMedQueryBuilderCount = new PubMedQueryBuilder(emailQueryString);
        PubMedQuery emailQueryCount = pubMedQueryBuilderCount.build();

        PubMedQueryType pubMedQueryType = new PubMedQueryType();
        pubMedQueryType.setLenientQuery(new PubMedQueryResult(emailQuery));
        pubMedQueryType.setStrictQuery(new PubMedQueryResult(emailQuery));
        pubMedQueryType.setLenientCountQuery(new PubMedQueryResult(emailQueryCount));
        pubMedQueryType.setStrictCountQuery(new PubMedQueryResult(emailQueryCount));
        pubMedQueries.add(pubMedQueryType);

        return pubMedQueries;
    }

    @Override
    public RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException {
        throw new UnsupportedOperationException("Does not support retrieval by pmids.");
    }
}
