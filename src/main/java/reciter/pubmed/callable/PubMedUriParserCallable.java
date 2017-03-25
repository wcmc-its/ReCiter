package reciter.pubmed.callable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import reciter.model.pubmed.PubMedArticle;
import reciter.pubmed.xmlparser.PubmedEFetchHandler;

public class PubMedUriParserCallable implements Callable<List<PubMedArticle>> {

	private final PubmedEFetchHandler xmlHandler;
	private final String uri;
	
	public PubMedUriParserCallable(PubmedEFetchHandler xmlHandler, String uri) {
		this.xmlHandler = xmlHandler;
		this.uri = uri;
	}

	public List<PubMedArticle> parse(String uri) throws ParserConfigurationException, SAXException, IOException {
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(uri, xmlHandler);
		return xmlHandler.getPubmedArticles();
	}

	public List<PubMedArticle> call() throws Exception {
		return parse(uri);
	}
}
