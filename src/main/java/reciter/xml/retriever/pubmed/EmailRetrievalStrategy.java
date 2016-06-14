package reciter.xml.retriever.pubmed;

import reciter.model.author.TargetAuthor;

public class EmailRetrievalStrategy extends AbstractRetrievalStrategy {

	private int threshold = DEFAULT_THRESHOLD;
	
	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	protected String constructInitialQuery(TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String constructStrictQuery(TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return null;
	}

}
