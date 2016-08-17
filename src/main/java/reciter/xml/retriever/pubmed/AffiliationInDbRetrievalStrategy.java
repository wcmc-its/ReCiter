package reciter.xml.retriever.pubmed;

import reciter.database.mongo.model.Identity;

public class AffiliationInDbRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	private static final String retrievalStrategyName = "AffiliationInDbRetrievalStrategy";
	
	public AffiliationInDbRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}
	
	@Override
	protected String constructInitialQuery(Identity identity) {
		if (identity.getAffiliations() != null && !identity.getAffiliations().isEmpty()) {
			String affiliation = identity.getAffiliations().get(0);
			String lastName = identity.getAuthorName().getLastName();
			String firstInitial = identity.getAuthorName().getFirstInitial();
			return lastName + " " + firstInitial + " AND " + affiliation;
		} else {
			return null;
		}
	}

	@Override
	protected String constructStrictQuery(Identity identity) {
		if (identity.getAffiliations() != null && !identity.getAffiliations().isEmpty()) {
			String affiliation = identity.getAffiliations().get(0);
			String lastName = identity.getAuthorName().getLastName();
			String firstName = identity.getAuthorName().getFirstName();
			return lastName + " " + firstName + " AND " + affiliation;
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
