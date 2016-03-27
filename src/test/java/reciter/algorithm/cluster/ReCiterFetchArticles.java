package reciter.algorithm.cluster;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import reciter.engine.ReCiterEngineProperty;
import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.xml.parser.pubmed.PubmedXmlFetcher;

public class ReCiterFetchArticles {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterFetchArticles.class);
	
	public static void main(String[] args) {
		ReCiterEngineProperty.loadProperty();
		slf4jLogger.info("Email folder=[" + ReCiterEngineProperty.emailXmlFolder + "].");
		
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		String cwid = "yiwang";
		TargetAuthorService targetAuthorService = new TargetAuthorServiceImpl();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(cwid);
		
		try {
			pubmedXmlFetcher.retrieveByEmail(targetAuthor);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
