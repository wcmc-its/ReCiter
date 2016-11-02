package reciter.xml.retriever.pubmed;

import org.springframework.stereotype.Component;

import reciter.database.mongo.model.Identity;

@Component("departmentRetrievalStrategy")
public class DepartmentRetrievalStrategy extends AbstractRetrievalStrategy {

	private static final String retrievalStrategyName = "DepartmentRetrievalStrategy";
	
	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}

	@Override
	protected String getStrategySpecificQuerySuffix(Identity identity) {
		if (identity.getDepartments() != null && !identity.getDepartments().isEmpty()) {
			return identity.getDepartments().get(0);
		} else {
			return null;
		}
	}
}
