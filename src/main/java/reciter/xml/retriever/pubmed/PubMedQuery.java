package reciter.xml.retriever.pubmed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class PubMedQuery {
	
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
		return "PubMedQuery [lenientQuery=" + lenientQuery + ", strictQuery=" + strictQuery + "]";
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
		
		public List<String> buildPmids() {
			if (pmids.size() == 1) {
				List<String> list = new ArrayList<>(1);
				list.add(pmids.get(0) + "[uid]");
				return list;
			}
			List<Long> partPmids = new ArrayList<>();
			List<String> queries = new ArrayList<>();
			int i = 1;
			Iterator<Long> itr = pmids.iterator();
			while (itr.hasNext()) {
				long pmid = itr.next();
				partPmids.add(pmid);
				if (i % THRESHOLD == 0) {
					queries.add(buildPmid(partPmids));
					partPmids.clear();
				}
				i++;
			}
			if (!partPmids.isEmpty()) {
				queries.add(buildPmid(partPmids));
			}
			System.out.println(queries.size());
			System.out.println(queries);
			return queries;
		}
	}
}
