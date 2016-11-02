package reciter.xml.retriever.pubmed;

import org.springframework.stereotype.Component;

import reciter.database.mongo.model.Identity;

@Component("affiliationInDbRetrievalStrategy")
public class AffiliationInDbRetrievalStrategy extends AbstractRetrievalStrategy {

	private static final String retrievalStrategyName = "AffiliationInDbRetrievalStrategy";

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	@Override
	protected String getStrategySpecificQuerySuffix(Identity identity) {
		if (identity.getAffiliations() != null && !identity.getAffiliations().isEmpty()) {
			return identity.getAffiliations().get(0);
		} else {
			return null;
		}
	}
}
