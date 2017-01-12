package reciter.xml.retriever.pubmed;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import reciter.model.identity.Identity;
import reciter.xml.retriever.pubmed.PubMedQuery.PubMedQueryBuilder;

@Component("affiliationInDbRetrievalStrategy")
public class AffiliationInDbRetrievalStrategy extends AbstractNameRetrievalStrategy {

	private static final String retrievalStrategyName = "AffiliationInDbRetrievalStrategy";

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	protected String getStrategySpecificKeyword(Identity identity) {
		if (identity.getInstitutions() != null && !identity.getInstitutions().isEmpty()) {
			return identity.getInstitutions().get(0);
		} else {
			return null;
		}
	}
	
	@Override
	protected String buildNameQuery(String lastName, String firstName, Identity identity) {
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder(getStrategySpecificKeyword(identity))
					.author(true, lastName, firstName);
		
		return pubMedQueryBuilder.build();
	}

	@Override
	protected String buildNameQuery(String lastName, String firstName, Identity identity, LocalDate startDate,
			LocalDate endDate) {
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder(getStrategySpecificKeyword(identity))
					.author(true, lastName, firstName)
					.dateRange(true, startDate, endDate);
		
		return pubMedQueryBuilder.build();
	}
}
