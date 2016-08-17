package reciter.xml.retriever.pubmed;

import reciter.database.mongo.model.Identity;
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
	protected String constructInitialQuery(Identity identity) {
		if (identity.getEmails() != null && !identity.getEmails().isEmpty()) {
			return identity.getEmails().get(0);
		} else {
			return null;
		}
	}

	@Override
	protected String constructStrictQuery(Identity identity) {
		if (identity.getEmails() != null && !identity.getEmails().isEmpty()) {
			return identity.getEmails().get(0);
		} else {
			return null;
		}
	}

	@Override
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
}
