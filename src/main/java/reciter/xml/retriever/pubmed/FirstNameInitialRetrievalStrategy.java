package reciter.xml.retriever.pubmed;

import java.util.List;

import org.springframework.stereotype.Component;

import reciter.database.mongo.model.Identity;

@Component("firstNameInitialRetrievalStrategy")
public class FirstNameInitialRetrievalStrategy extends AbstractRetrievalStrategy {

	private static final String retrievalStrategyName = "FirstNameInitialRetrievalStrategy";

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	@Override
	protected String getStrategySpecificQuerySuffix(Identity identity) {
		return "[au]";
	}
	
	@Override
	protected List<PubMedQuery> constructPubMedQueryList(Identity identity) {
		return constructPubMedQueryList(identity, true, "");
	}
}
