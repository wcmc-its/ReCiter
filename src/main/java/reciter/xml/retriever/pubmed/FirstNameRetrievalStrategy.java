package reciter.xml.retriever.pubmed;

import reciter.model.author.TargetAuthor;

public class FirstNameRetrievalStrategy extends AbstractRetrievalStrategy {

	@Override
	public String constructInitialQuery(TargetAuthor targetAuthor) {
		return targetAuthor.getAuthorName().getLastName() + " " + targetAuthor.getAuthorName().getFirstInitial();
	}

	@Override
	protected String constructStrictQuery(TargetAuthor targetAuthor) {
		return targetAuthor.getAuthorName().getLastName() + " " + targetAuthor.getAuthorName().getFirstName();
	}
}
