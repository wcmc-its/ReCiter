package reciter.xml.retriever.engine;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reciter.model.author.TargetAuthor;
import reciter.xml.retriever.pubmed.EmailRetrievalStrategy;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	public void retrieve(TargetAuthor targetAuthor) throws IOException, SAXException, ParserConfigurationException {

		// First retrieve using first name initial and last name. i.e., Kukafka r[au].
		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy();
		firstNameInitialRetrievalStrategy.setRetrieveExceedThreshold(false);
		boolean isRetrieved = firstNameInitialRetrievalStrategy.retrieve(targetAuthor);

		if (!isRetrieved) {
			RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy();
			emailRetrievalStrategy.retrieve(targetAuthor);
		}

	}
}
