package reciter.xml.retriever.pubmed;

import java.io.IOException;

import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.xml.retriever.engine.DefaultReCiterRetrievalEngine;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

public class ReCiterRetriever {

	public static void main(String[] args) {
		ReCiterRetrievalEngine engine = new DefaultReCiterRetrievalEngine();
		TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor("yiwang");
		try {
			engine.retrieve(targetAuthor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
