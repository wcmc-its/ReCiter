package reciter.xml.retriever.pubmed;

import reciter.database.mongo.model.Identity;

public class DepartmentRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	private static final String retrievalStrategyName = "DepartmentRetrievalStrategy";
	
	public DepartmentRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}
	
	@Override
	protected String constructInitialQuery(Identity identity) {
		if (identity.getDepartments() != null && !identity.getDepartments().isEmpty()) {
			String department = identity.getDepartments().get(0);
			String lastName = identity.getAuthorName().getLastName();
			String firstInitial = identity.getAuthorName().getFirstInitial();
			return lastName + " " + firstInitial + " AND " + department;
		} else {
			return null;
		}
	}

	@Override
	protected String constructStrictQuery(Identity identity) {
		if (identity.getDepartments() != null && !identity.getDepartments().isEmpty()) {
			String department = identity.getDepartments().get(0);
			String lastName = identity.getAuthorName().getLastName();
			String firstName = identity.getAuthorName().getFirstName();
			return lastName + " " + firstName + " AND " + department;
		} else {
			return null;
		}
	}

	@Override
	public int getThreshold() {
		return threshold;
	}

	@Override
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
}
