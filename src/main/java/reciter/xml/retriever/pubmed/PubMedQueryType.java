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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class PubMedQueryType {
	
	// Query that tends to return more results than the strict query.
	private PubMedQueryResult lenientQuery;
	
	// QUery that tends to return less results than the lenient query.
	private PubMedQueryResult strictQuery;

	public PubMedQueryResult getLenientQuery() {
		return lenientQuery;
	}

	public void setLenientQuery(PubMedQueryResult lenientQuery) {
		this.lenientQuery = lenientQuery;
	}

	public PubMedQueryResult getStrictQuery() {
		return strictQuery;
	}

	public void setStrictQuery(PubMedQueryResult strictQuery) {
		this.strictQuery = strictQuery;
	}
	
	@Override
	public String toString() {
		return "PubMedQueryType [lenientQuery=" + lenientQuery + ", strictQuery=" + strictQuery + "]";
	}

	/**
	 * PubMed Query builder class for constructing a PubMed query.
	 */
	public static class PubMedQueryBuilder {
		private String lastName;
		private String firstName;
		private boolean isAuthorRequired;
		
		private LocalDate startDate;
		private LocalDate endDate;
		private boolean isDateRangeRequired;
		
		private String strategyQuery;
		
		private List<Long> pmids;
		private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		public PubMedQueryBuilder() {}
		
		public PubMedQueryBuilder(String strategyQuery) {
			this.strategyQuery = strategyQuery;
		}
		
		public PubMedQueryBuilder(List<Long> pmids) {
			this.pmids = pmids;
		}
		
		public PubMedQueryBuilder author(boolean isAuthorRequired, String lastName, String firstName) {
			this.isAuthorRequired = isAuthorRequired;
			this.lastName = lastName;
			this.firstName = firstName;
			return this;
		}
		
		public PubMedQueryBuilder dateRange(boolean isDateRangeRequired, LocalDate startDate, LocalDate endDate) {
			this.isDateRangeRequired = isDateRangeRequired;
			this.startDate = startDate;
			this.endDate = endDate;
			return this;
		}
		
		public String build() {
			List<String> parts = new ArrayList<String>();
			if (isAuthorRequired) {
				parts.add(lastName + " " + firstName + " [au]");
			}
			if (isDateRangeRequired) {
				parts.add(startDate.format(formatter) + ":" + endDate.format(formatter) + "[DP]");
			}
			if (strategyQuery != null && !strategyQuery.isEmpty()) {
				parts.add(strategyQuery);
			}
			
			return StringUtils.join(parts, " AND ");
		}
		
		private String buildPmid(List<Long> pmids) {
			List<String> pmidsUid = new ArrayList<>();
			for (long pmid : pmids) {
				pmidsUid.add(pmid + "[uid]");
			}
			return StringUtils.join(pmidsUid, " OR ");
		}
		
		private static final int THRESHOLD = 25;
		
		public Map<String, Integer> buildPmids() {
			if (pmids.size() == 1) {
				Map<String, Integer> map = new HashMap<>();
				map.put(pmids.get(0) + "[uid]", 1);
				return map;
			}
			Map<String, Integer> map = new HashMap<>();
			List<Long> partPmids = new ArrayList<>();
			int i = 1;
			Iterator<Long> itr = pmids.iterator();
			while (itr.hasNext()) {
				long pmid = itr.next();
				partPmids.add(pmid);
				if (i % THRESHOLD == 0) {
					map.put(buildPmid(partPmids), THRESHOLD);
					partPmids.clear();
				}
				i++;
			}
			if (!partPmids.isEmpty()) {
				map.put(buildPmid(partPmids), partPmids.size());
			}
			return map;
		}
	}
}
