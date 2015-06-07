package xmlparser.scopus;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import xmlparser.AbstractXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * This class handles fetching the XML from Scopus and saving it to disk.
 * @author jil3004
 *
 */
public class ScopusXmlFetcher extends AbstractXmlFetcher {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlFetcher.class);
	private static final String DEFAULT_LOCATION = "src/main/java/resources/data/scopus";

	public ScopusXmlFetcher() {
		super(DEFAULT_LOCATION);
	}
	
	public ScopusXmlFetcher(String directory) {
		super(directory);
	}
		
	/**
	 * Fetch a single Scopus XML file based on the PMID and save XML file into disk.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 * @param pmid
	 */
	public void fetchSingleScopus(String cwid, String pmid) {
		ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder(pmid).build();
		saveXml(scopusXmlQuery.getQueryUrl(), cwid, pmid);
	}
	
	/**
	 * Fetch a single Scopus XML file based on the PMID.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 * @param pmid
	 */
	public ScopusArticle getScopusXml(String cwid, String pmid) {
		File scopusXmlFile = new File("data/scopus/" + cwid + "/" + pmid + ".xml");
		if (!scopusXmlFile.exists()) {
			return null;
		} else {
			ScopusXmlHandler handler = new ScopusXmlHandler();
			SAXParser saxParser;
			try {
				saxParser = SAXParserFactory.newInstance().newSAXParser();
				saxParser.parse(scopusXmlFile, handler);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				slf4jLogger.info(e.getMessage());
			}
			return handler.getScopusArticle();
		}
	}

	@Override
	public void fetch(String lastName, String firstName, String cwid) {
		// TODO Auto-generated method stub
		
	}
}
