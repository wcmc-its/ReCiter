package reciter.xml.retriever.engine;

import java.io.IOException;

import reciter.model.author.TargetAuthor;
import reciter.service.PubMedService;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {
	
	@Override
	public void retrieve(TargetAuthor targetAuthor) throws IOException {

		// First retrieve using first name initial and last name. i.e., Kukafka r[au].
		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy();
		firstNameInitialRetrievalStrategy.setRetrieveExceedThreshold(true);
		boolean isRetrieved = firstNameInitialRetrievalStrategy.retrieve(targetAuthor);

//		if (!isRetrieved) {
//			RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy();
//			emailRetrievalStrategy.retrieve(targetAuthor);
//		}
	}
}
