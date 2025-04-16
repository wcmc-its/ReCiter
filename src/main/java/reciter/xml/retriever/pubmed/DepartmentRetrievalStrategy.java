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

import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.springframework.stereotype.Component;

import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;
import reciter.pubmed.retriever.PubMedQuery;
import reciter.xml.retriever.pubmed.PubMedQueryType.PubMedQueryBuilder;

@Component("departmentRetrievalStrategy")
public class DepartmentRetrievalStrategy extends AbstractNameRetrievalStrategy {

    private static final String retrievalStrategyName = "DepartmentRetrievalStrategy";
    
    @Override
    public String getRetrievalStrategyName() {
        return retrievalStrategyName;
    }

    @Override
    protected String getStrategySpecificKeyword(Identity identity) {
        if (identity == null) {
            throw new IllegalArgumentException("Identity is null.");
        }
        if (identity.getOrganizationalUnits() == null || identity.getOrganizationalUnits().isEmpty()) {
            return null;
        }
        
        Iterator<OrganizationalUnit> iter = identity.getOrganizationalUnits().iterator();
        OrganizationalUnit firstDepartment = iter.next();
        if (firstDepartment == null || firstDepartment.getOrganizationalUnitLabel() == null ||
            firstDepartment.getOrganizationalUnitLabel().trim().isEmpty()) {
            throw new IllegalArgumentException("First organizational unit or its label is missing.");
        }
        
        final String first = firstDepartment.getOrganizationalUnitLabel().trim() + "[affiliation]";
        // If only one organizational unit exists, return its keyword.
        if (!iter.hasNext()) {
            return first;
        }
        
        // For more than one organizational unit, build a combined query.
        final StringBuilder buf = new StringBuilder();
        buf.append("(").append(first);
        while (iter.hasNext()) {
            OrganizationalUnit orgUnit = iter.next();
            if (orgUnit == null || orgUnit.getOrganizationalUnitLabel() == null ||
                orgUnit.getOrganizationalUnitLabel().trim().isEmpty()) {
                // Skip any invalid organizational unit entries.
                continue;
            }
            buf.append(" OR ");
            buf.append(orgUnit.getOrganizationalUnitLabel().trim()).append("[affiliation]");
        }
        buf.append(")");
        return buf.toString();
    }

    @Override
    protected PubMedQuery buildNameQuery(Set<AuthorName> identitynames, Identity identity) {
        String keyword = getStrategySpecificKeyword(identity);
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("No valid department keyword could be generated from the identity organizational units.");
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
            throw new IllegalArgumentException("No valid department keyword could be generated from the identity organizational units.");
        }
        PubMedQueryBuilder pubMedQueryBuilder = new PubMedQueryBuilder(keyword)
                    .author(true, false, identitynames)
                    .dateRange(true, startDate, endDate);
        
        return pubMedQueryBuilder.build();
    }
}
