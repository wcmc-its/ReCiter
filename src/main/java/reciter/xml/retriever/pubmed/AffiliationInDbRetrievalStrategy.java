package reciter.xml.retriever.pubmed;

import reciter.model.author.TargetAuthor;

public class AffiliationInDbRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	private static final String retrievalStrategyName = "AffiliationInDbRetrievalStrategy";
	
	public AffiliationInDbRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}
	
	@Override
	protected String constructInitialQuery(TargetAuthor targetAuthor) {
		String affiliation = targetAuthor.getAffiliation().getAffiliationName();
		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		return lastName + " " + firstInitial + " AND " + affiliation;
	}

	@Override
	protected String constructStrictQuery(TargetAuthor targetAuthor) {
		String affiliation = targetAuthor.getAffiliation().getAffiliationName();
		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstName = targetAuthor.getAuthorName().getFirstName();
		return lastName + " " + firstName + " AND " + affiliation;
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
