package reciter.xml.retriever.pubmed;

import reciter.database.mongo.model.Identity;

public class FirstNameInitialRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	private static final String retrievalStrategyName = "FirstNameInitialRetrievalStrategy";
	
	public FirstNameInitialRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}

	@Override
	public String constructInitialQuery(Identity identity) {
		return identity.getAuthorName().getLastName() + " " + identity.getAuthorName().getFirstInitial() + "[au]";
	}

	@Override
	protected String constructStrictQuery(Identity identity) {
		return identity.getAuthorName().getLastName() + " " + identity.getAuthorName().getFirstName() + "[au]";
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
