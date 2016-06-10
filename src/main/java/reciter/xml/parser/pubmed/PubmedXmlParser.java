package reciter.xml.parser.pubmed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import reciter.model.pubmed.PubMedArticle;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;

/**
 * This class handles PubMed XML parsing.
 * 
 * @author jil3004
 *
 */
public class PubmedXmlParser {

	private final PubmedEFetchHandler xmlHandler;

	public PubmedXmlParser(PubmedEFetchHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}

	/**
	 * Parse the PubMed XML and return a list of PubmedArticles.
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public List<PubMedArticle> parse(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(xmlFile, xmlHandler);
		return xmlHandler.getPubmedArticles();
	}
	
	public List<PubMedArticle> parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(inputStream, xmlHandler);
		return xmlHandler.getPubmedArticles();
	}
}
