package reciter.xml.retriever.pubmed;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
		
		private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		public PubMedQueryBuilder() {}
		
		public PubMedQueryBuilder(String strategyQuery) {
			this.strategyQuery = strategyQuery;
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
			
			return StringUtils.join(parts, " AND");
		}
	}
}
