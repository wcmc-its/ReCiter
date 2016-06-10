package reciter.xml.retriever.engine;

import java.io.IOException;
import java.util.List;

import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {
	
	@Override
	public List<PubMedArticle> retrieve(TargetAuthor targetAuthor) throws IOException {

		// First retrieve using first name initial and last name. i.e., Kukafka r[au].
		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy();
		firstNameInitialRetrievalStrategy.setRetrieveExceedThreshold(true);
		return firstNameInitialRetrievalStrategy.retrieve(targetAuthor);

//		if (!isRetrieved) {
//			RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy();
//			emailRetrievalStrategy.retrieve(targetAuthor);
//		}
	}
}
