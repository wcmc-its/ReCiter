package main.xml.pubmed;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import main.xml.AbstractXmlFetcher;
import main.xml.pubmed.model.PubmedArticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Fetches XML articles from PubMed and writes to a file.
 * @author jil3004
 *
 */
public class PubmedXmlFetcher extends AbstractXmlFetcher {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractXmlFetcher.class);	
	private boolean performRetrievePublication;

	public PubmedXmlFetcher() {
		super("data/xml/");
	}

	public PubmedXmlFetcher(String directory) {
		super(directory);
	}

	/**
	 * Retrieves all the XML files from the directory {@code "data/xml/cwid."}
	 * @param cwid cwid of the author.
	 * @return a list of PubmedArticle from the disk.
	 * 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public List<PubmedArticle> getPubmedArticle(String lastName, String firstInitial, String cwid) {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();

		// The articles are retrieved when this directory exists. Fetch the articles if they do not exist.
		//		File dir = new File(getDirectory() + cwid);
		//		if (!dir.exists()) {
		//			fetch(lastName, firstInitial, cwid);
		//		}

		// 03/21/2015 (Jie): Read the config.properties file to determine whether to retrieve the publications or not.
		if (performRetrievePublication) {
			slf4jLogger.info("Fetching publications for " + cwid);
			fetch(lastName, firstInitial, cwid);
		}

		// Parse the xml files to return a list of PubmedArticles.
		System.out.println(getDirectory() + cwid);
		File[] xmlFiles = new File(getDirectory() + cwid).listFiles();
		for (File xmlFile : xmlFiles) {
			PubmedEFetchHandler pubmedEFetchHandler = new PubmedEFetchHandler();
			try {
				SAXParserFactory.newInstance()
				.newSAXParser()
				.parse(xmlFile.getPath(), pubmedEFetchHandler);
			} catch (SAXException | IOException | ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pubmedArticleList.addAll(pubmedEFetchHandler.getPubmedArticles());
		}
		return pubmedArticleList;
	}

	/**
	 * Fetch all the publications for this query "lastname firstInitial[au]" in PubMed and store it on disk.
	 * @param lastName last name of the author.
	 * @param firstInitial first initial of the author.
	 * @param cwid cwid of the author.
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void fetch(String lastName, String firstName, String cwid) {

		File dir = new File(getDirectory() + cwid);
		
		// Fetch only if directory doesn't exist.
		if (!dir.exists()) {
//			String firstInitial = firstName.substring(0, 1);
			
			/**
			 * Get the count (number of publications for this query).
			 */
			PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
//			pubmedXmlQuery.setTerm(lastName + "%20" + firstInitial + "[au]");
			lastName = lastName.replaceAll(" ", "%20");
			firstName = firstName.replaceAll(" ", "%20");
			pubmedXmlQuery.setTerm(lastName + "%20" + firstName + "[au]");
			pubmedXmlQuery.setRetMax(1); // set retmax = 1 so that query can be executed fast.
			String eSearchUrl = pubmedXmlQuery.buildESearchQuery();
			PubmedESearchHandler xmlHandler = PubmedESearchHandler.executeESearchQuery(eSearchUrl);
			int count = xmlHandler.getCount();

			/**
			 * Perform retrieval and storage of xml.
			 */
			int retMax = 10000;
			pubmedXmlQuery.setRetMax(retMax);
			int currentRetStart = 0;

			int numSteps = (int) Math.ceil((double)count/retMax); // number of partitions that we need to finish retrieving all XML.

			// Use the retstart value to iteratively fetch all XMLs.
			for (int i = 0; i < numSteps; i++) {
				// Get webenv value.
				pubmedXmlQuery.setRetStart(currentRetStart);
				eSearchUrl = pubmedXmlQuery.buildESearchQuery();
				pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

				// Use the webenv value to retrieve xml.
				String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();

				// Save the xml file to directory data/xml/cwid
				int result = saveXml(eFetchUrl, cwid, cwid + "_" + i);

				// Unsucessful query. ie: XML contains: <ERROR>Unable to obtain query #1</ERROR>. Retry with the same query.
				while (result == 1) {
					slf4jLogger.info("Query returned: <ERROR>Unable to obtain query #1</ERROR>. Retrying ...");
					eSearchUrl = pubmedXmlQuery.buildESearchQuery();
					System.out.println(eSearchUrl);
					pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

					// Use the webenv value to retrieve xml.
					eFetchUrl = pubmedXmlQuery.buildEFetchQuery();
					System.out.println(eFetchUrl);
					result = saveXml(eFetchUrl, cwid, cwid + "_" + i);
				}

				// Update the retstart value.
				currentRetStart += pubmedXmlQuery.getRetMax();
				pubmedXmlQuery.setRetStart(currentRetStart);
			}
		}
	}

	public boolean isPerformRetrievePublication() {
		return performRetrievePublication;
	}

	public void setPerformRetrievePublication(boolean performRetrievePublication) {
		this.performRetrievePublication = performRetrievePublication;
	}
}
