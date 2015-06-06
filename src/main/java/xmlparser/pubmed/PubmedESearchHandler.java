package xmlparser.pubmed;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX handler for parsing the ESearch query from PubMed.
 * @author Jie
 *
 */
public class PubmedESearchHandler extends DefaultHandler {
	
	private String webEnv;
	private int count;
	private boolean bWebEnv;
	private boolean bCount;
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			SAXParserFactory.newInstance()
							.newSAXParser()
							.parse(inputStream, webEnvHandler);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return webEnvHandler;
	}
	
	public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("WebEnv")) {
			bWebEnv = true;
		}
		if (qName.equalsIgnoreCase("Count")) {
			bCount = true;
		}
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {
		if (bWebEnv) {
			webEnv = new String(ch, start, length);
			bWebEnv = false;
		}
		if (bCount) {
			count = Integer.parseInt(new String(ch, start, length));
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
