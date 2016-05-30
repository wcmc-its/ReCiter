package reciter.xml.retriever.engine;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import reciter.model.author.TargetAuthor;
import reciter.service.PubMedService;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.xml.retriever.pubmed.FirstNameInitialRetrievalStrategy;
import reciter.xml.retriever.pubmed.RetrievalStrategy;

public class DefaultReCiterRetrievalEngine extends AbstractReCiterRetrievalEngine {

	public static void main(String[] args) {
		// open/read the application context file
	    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:application-context.xml");
	    PubMedService pubMedService = (PubMedService) ctx.getBean("pubMedServiceImpl");
	    System.out.println("Is that null=" + pubMedService == null);
	    ReCiterRetrievalEngine engine = new DefaultReCiterRetrievalEngine();
		TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor("aas2004");
		try {
			engine.retrieve(targetAuthor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void retrieve(TargetAuthor targetAuthor) throws IOException {

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
