package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import reciter.service.PubMedService;
import reciter.service.impl.PubMedServiceImpl;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.model.PubMedArticle;

public class PubMedXmlParseWorker implements Runnable {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(PubMedXmlParseWorker.class);
	private final PubmedEFetchHandler xmlHandler;
	private final String uri;
	
	public PubMedXmlParseWorker(PubmedEFetchHandler xmlHandler, String uri) {
		this.xmlHandler = xmlHandler;
		this.uri = uri;
	}
	
	@Override
	public void run() {
		try {
			List<PubMedArticle> pubMedArticles = parse(uri);
			PubMedService pubMedService = new PubMedServiceImpl();
			for (PubMedArticle pubMedArticle : pubMedArticles) {
				pubMedService.upsertPubMedArticle(pubMedArticle);
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			slf4jLogger.error("Error parsing PubMed XML input stream.", e);
		}
	}

	public List<PubMedArticle> parse(String uri) throws ParserConfigurationException, SAXException, IOException {
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(uri, xmlHandler);
		return xmlHandler.getPubmedArticles();
	}
}
