package main.xml.scopus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ScopusXmlQuery {

	private static final String SEARCH_URL_PMID = "http://api.elsevier.com/content/search/index:SCOPUS?query=";
	private static final String SEARCH_URL_END = "&count=100&field=pubmed-id,affiliation&start=0&view=COMPLETE&field=affilname&apikey=e0fa610418a4859d24f2457e021aea60&httpAccept=application/xml";
	private static final String SEARCH_URL_AFFIL_AUTHOR = "&count=1&field=pubmed-id,affiliation,author,afid&start=0&view=COMPLETE&apikey=e0fa610418a4859d24f2457e021aea60&httpAccept=application/xml";
	
	/**
	 * Construct a search query for a given PMID in Scopus that retrieves affiliation id and author information.
	 * @param pmid PMID of a PubMed article.
	 * @return A search query in the format <code>SEARCH_URL_PMID</code> + "pmid(pmid)" + <code>SEARCH_URL_AFFIL_AUTHOR</code>
	 */
	public String buildSearchQuery(String pmid) {
		StringBuilder sb = new StringBuilder();
		sb.append(SEARCH_URL_PMID);
		sb.append("pmid(" + pmid + ")");
		sb.append(SEARCH_URL_AFFIL_AUTHOR);
		return sb.toString();
	}
		
	/**
	 * Build an affiliation string from the list of pmids.
	 * @param pmidList
	 * @return
	 */
	public String buildAffiliationQuery(List<String> pmidList) {
		StringBuilder sb = new StringBuilder();
		sb.append(SEARCH_URL_PMID);
		for (int i = 0; i < pmidList.size(); i++) {
			sb.append("pmid(");
			sb.append(pmidList.get(i));
			sb.append(")");
			// append OR logic between pmids except for the last pmid.
			if (i != pmidList.size() - 1) {
				sb.append("%20OR%20");
			}
		}
		sb.append(SEARCH_URL_END);
		return sb.toString();
	}
	
	
	
	/**
	 * Returns the xml handler that searches the list of pmids.
	 * @param pmidList list of pmids to be searched. List must be less than or equal to 100.
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public ScopusAffiliationHandler executeQuery(List<String> pmidList) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		ScopusAffiliationHandler scopusXmlPmidHandler = new ScopusAffiliationHandler();
		InputSource inputSource = new InputSource(new URL(buildAffiliationQuery(pmidList)).openStream());
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(inputSource, scopusXmlPmidHandler);
		return scopusXmlPmidHandler;
	}
}
