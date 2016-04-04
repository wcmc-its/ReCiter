package reciter.xml.parser.pubmed.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler for parsing the ESearch query from PubMed.
 * @author Jie
 *
 */
public class PubmedESearchHandler extends DefaultHandler {
	
	private final static Logger slf4jLogger = LoggerFactory.getLogger(PubmedESearchHandler.class);
	
	private String webEnv;
	private int count;
	private boolean bWebEnv;
	private boolean bCount;
	private int numCountEncounteredSoFar = 0;
	
	private StringBuilder chars = new StringBuilder();
	
	/**
	 * Sends a query to the NCBI web site to retrieve the webEnv.
	 * 
	 * @param query example query: http://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=1&usehistory=y&term=Kukafka%20R[au].
	 * @return WebEnvHandler that contains the WebEnv data.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static PubmedESearchHandler executeESearchQuery(String eSearchUrl) {
		PubmedESearchHandler webEnvHandler = new PubmedESearchHandler();
		InputStream inputStream = null;
		try {
			inputStream = new URL(eSearchUrl).openStream();
		} catch (IOException e) {
			slf4jLogger.error("Error in executeESearchQuery", e);
		}
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(inputStream, webEnvHandler);
		} catch (Exception e) {
			slf4jLogger.error("Error in executeESearchQuery", e);
		}
		return webEnvHandler;
	}
	
	@Override
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		
		chars.setLength(0);
		
		if (qName.equalsIgnoreCase("WebEnv")) {
			bWebEnv = true;
		}
		if (qName.equalsIgnoreCase("Count") && numCountEncounteredSoFar == 0) {
			numCountEncounteredSoFar++;
			bCount = true;
		}
	}
	
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (bWebEnv) {
			chars.append(ch, start, length);
		}
		if (bCount) {
			chars.append(ch, start, length);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// WebEnv
		if (bWebEnv) {
			webEnv = chars.toString();
			bWebEnv = false;
		}
		
		// Count.
		if (bCount) {
			count = Integer.parseInt(chars.toString());
			bCount = false;
		}
	}
	
	public String getWebEnv() {
		return webEnv;
	}

	public int getCount() {
		return count;
	}
}
