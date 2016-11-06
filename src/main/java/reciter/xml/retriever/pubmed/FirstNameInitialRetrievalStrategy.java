package reciter.xml.retriever.pubmed;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import reciter.database.mongo.model.Identity;
import reciter.xml.retriever.pubmed.PubMedQuery.PubMedQueryBuilder;

@Component("firstNameInitialRetrievalStrategy")
public class FirstNameInitialRetrievalStrategy extends AbstractNameRetrievalStrategy {

	private static final String retrievalStrategyName = "FirstNameInitialRetrievalStrategy";

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	@Override
	protected String getStrategySpecificKeyword(Identity identity) {
		return null;
	}

	@Override
	protected String buildNameQuery(String lastName, String firstName, Identity identity) {
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder()
					.author(true, lastName, firstName);
		
		return pubMedQueryBuilder.build();
	}
	
	@Override
	protected String buildNameQuery(String lastName, String firstName, Identity identity, LocalDate startDate,
			LocalDate endDate) {
		PubMedQueryBuilder pubMedQueryBuilder = 
				new PubMedQueryBuilder()
					.author(true, lastName, firstName)
					.dateRange(true, startDate, endDate);
		
		return pubMedQueryBuilder.build();
	}
}
