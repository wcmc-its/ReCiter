package reciter.xml.retriever.engine;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	public static void main(String[] args) {
		DefaultReCiterRetrievalEngine engine = new DefaultReCiterRetrievalEngine();
		TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor("aas2004");
		try {
			engine.retrieve(targetAuthor);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void retrieve(TargetAuthor targetAuthor) throws IOException, SAXException, ParserConfigurationException {

		// First retrieve using first name initial and last name. i.e., Kukafka r[au].
		RetrievalStrategy firstNameInitialRetrievalStrategy = new FirstNameInitialRetrievalStrategy();
		firstNameInitialRetrievalStrategy.setRetrieveExceedThreshold(false);
		boolean isRetrieved = firstNameInitialRetrievalStrategy.retrieve(targetAuthor);

//		if (!isRetrieved) {
//			RetrievalStrategy emailRetrievalStrategy = new EmailRetrievalStrategy();
//			emailRetrievalStrategy.retrieve(targetAuthor);
//		}
	}
}
