package reciter.xml.retriever.pubmed;

import reciter.model.author.TargetAuthor;

public class EmailRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	private static final String retrievalStrategyName = "EmailRetrievalStrategy";
	
	public EmailRetrievalStrategy(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	protected String constructInitialQuery(TargetAuthor targetAuthor) {
		return targetAuthor.getEmail();
	}

	@Override
	protected String constructStrictQuery(TargetAuthor targetAuthor) {
		return targetAuthor.getEmail();
	}

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
}
