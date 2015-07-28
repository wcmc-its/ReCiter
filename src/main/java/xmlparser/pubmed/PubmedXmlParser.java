package xmlparser.pubmed;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import xmlparser.pubmed.model.PubmedArticle;

/**
 * This class handles PubMed XML parsing.
 * 
 * @author jil3004
 *
 */
public class PubmedXmlParser implements Parser {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(PubmedXmlParser.class);
	
	private File xmlFile;
	private final PubmedEFetchHandler xmlHandler;
	
	/**
	 * Parse the PubMed XML and return a list of PubmedArticles.
	 */
	@Override
	public List<PubmedArticle> parse() {
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(xmlFile, xmlHandler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			slf4jLogger.error(e.getMessage());
		}
		return xmlHandler.getPubmedArticles();
	}
	
	public PubmedXmlParser(PubmedEFetchHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}
	
	public void setXmlInputSource(File xmlFile) {
		this.xmlFile = xmlFile;
	}
}
