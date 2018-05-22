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
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import reciter.pubmed.retriever.PubMedQuery;

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

		// TODO update to LocalDate
		private Date startDate;
		private Date endDate;
		private boolean isDateRangeRequired;
		
		private String strategyQuery;
		
		private List<Long> pmids;
		
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
		
		public PubMedQueryBuilder dateRange(boolean isDateRangeRequired, Date startDate, Date endDate) {
			this.isDateRangeRequired = isDateRangeRequired;
			this.startDate = startDate;
			this.endDate = endDate;
			return this;
		}
		
		public PubMedQuery build() {
			PubMedQuery p = PubMedQuery.builder().build();
			if (isAuthorRequired) {
				p.setAuthor(lastName + " " + firstName);
			}
			if (isDateRangeRequired) {
				p.setStart(startDate);
				p.setEnd(endDate);
			}
			if (strategyQuery != null && !strategyQuery.isEmpty()) {
				p.setStrategyQuery(strategyQuery);
			}
			return p;
		}
		
		public PubMedQuery buildPmids(List<Long> pmids) {
			List<String> pmidsUid = new ArrayList<>();
			for (long pmid : pmids) {
				//Commented using of OR with [uid] since it was causing issue with GoldStandardRetreivalStrategy if number of pmid used was bigger than 100
				//pmidsUid.add(pmid + "[uid]");
				pmidsUid.add( Long.toString(pmid));
			}
			//PubMedQuery p =  PubMedQuery.builder().strategyQuery(StringUtils.join(pmidsUid, " OR ")).build();
			PubMedQuery p =  PubMedQuery.builder().strategyQuery(pmidsUid.toString()).build();
			return p;
		}
		
//		private static final int THRESHOLD = 25;
//
//		public Map<String, Integer> buildPmids() {
//			if (pmids.size() == 1) {
//				Map<String, Integer> map = new HashMap<>();
//				map.put(pmids.get(0) + "[uid]", 1);
//				return map;
//			}
//			Map<String, Integer> map = new HashMap<>();
//			List<Long> partPmids = new ArrayList<>();
//			int i = 1;
//			Iterator<Long> itr = pmids.iterator();
//			while (itr.hasNext()) {
//				long pmid = itr.next();
//				partPmids.add(pmid);
//				if (i % THRESHOLD == 0) {
//					map.put(buildPmid(partPmids), THRESHOLD);
//					partPmids.clear();
//				}
//				i++;
//			}
//			if (!partPmids.isEmpty()) {
//				map.put(buildPmid(partPmids), partPmids.size());
//			}
//			return map;
//		}
	}
}
