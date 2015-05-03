package main.xml.pubmed;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
			fetch(lastName, firstInitial, cwid);
		}

		// Parse the xml files to return a list of PubmedArticles.
		File[] xmlFiles = new File(getDirectory() + cwid).listFiles();
		for (File xmlFile : xmlFiles) {
			PubmedEFetchHandler pubmedEFetchHandler = new PubmedEFetchHandler();
			try {
				SAXParserFactory.newInstance()
				.newSAXParser()
				.parse(xmlFile.getPath(), pubmedEFetchHandler);
			} catch (Exception e) {
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
		int numPubMedArticles = -1;
		// Fetch only if directory doesn't exist.
		if (!dir.exists()) {
			String firstInitial = firstName.substring(0, 1);

			// Get the count (number of publications for this query).
			PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
			lastName = lastName.replaceAll(" ", "%20");
			pubmedXmlQuery.setTerm(lastName + "%20" + firstInitial + "[au]");

			// set retmax = 1 so that query can be executed fast.
			pubmedXmlQuery.setRetMax(1);

			String eSearchUrl = pubmedXmlQuery.buildESearchQuery();
			PubmedESearchHandler xmlHandler = PubmedESearchHandler.executeESearchQuery(eSearchUrl);
			numPubMedArticles = xmlHandler.getCount();

			slf4jLogger.info("Number of articles need to be retrieved for : " + cwid + " is "+ numPubMedArticles);

			// Retrieve the publications 10,000 records at one time and store to disk.
			int retMax = 10000;
			pubmedXmlQuery.setRetMax(retMax);
			int currentRetStart = 0;

			// Number of partitions that we need to finish retrieving all XML.
			int numSteps = (int) Math.ceil((double)numPubMedArticles / retMax); 

			// Use the retstart value to iteratively fetch all XMLs.
			for (int i = 0; i < numSteps; i++) {
				// Get webenv value.
				pubmedXmlQuery.setRetStart(currentRetStart);
				eSearchUrl = pubmedXmlQuery.buildESearchQuery();
				pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

				// Use the webenv value to retrieve xml.
				String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();

				// Save the xml file to directory data/xml/cwid
				saveXml(eFetchUrl, cwid, cwid + "_" + i);

				// Unsucessful query. ie: XML contains: <ERROR>Unable to obtain query #1</ERROR>. Retry with the same query.
				//				while (result == 1) {
				//					slf4jLogger.info("Query returned: <ERROR>Unable to obtain query #1</ERROR>. Retrying ...");
				//					eSearchUrl = pubmedXmlQuery.buildESearchQuery();
				//					slf4jLogger.info(eSearchUrl);
				//					pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());
				//
				//					// Use the webenv value to retrieve xml.
				//					eFetchUrl = pubmedXmlQuery.buildEFetchQuery();
				//					slf4jLogger.info(eFetchUrl);
				//					result = saveXml(eFetchUrl, cwid, cwid + "_" + i);
				//				}

				// Update the retstart value.
				currentRetStart += pubmedXmlQuery.getRetMax();
				pubmedXmlQuery.setRetStart(currentRetStart);
			}
			
			// Check the file for 'Unable to obtain query #1' error.
			int n = 3;
			String lineN = null;
			try {
				lineN = Files.lines(Paths.get(getDirectory() + cwid + "/" + cwid + "_0.xml"))
				                    .skip(n)
				                    .findFirst()
				                    .get()
				                    .trim();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ("<ERROR>Unable to obtain query #1</ERROR>".equals(lineN)) {
				slf4jLogger.warn("cwid: " + cwid + " received 'Unable to obtain query#1' error.");
			}
			
			// Check number of articles match.
			List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();
			// Parse the xml files to return a list of PubmedArticles.
			File[] xmlFiles = new File(getDirectory() + cwid).listFiles();
			for (File xmlFile : xmlFiles) {
				PubmedEFetchHandler pubmedEFetchHandler = new PubmedEFetchHandler();
				try {
					SAXParserFactory.newInstance()
					.newSAXParser()
					.parse(xmlFile.getPath(), pubmedEFetchHandler);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pubmedArticleList.addAll(pubmedEFetchHandler.getPubmedArticles());
			}
			
			if (pubmedArticleList.size() != numPubMedArticles) {
				slf4jLogger.warn("cwid: " + cwid + " did not retrieve all the publications. Number of articles on disk=" + pubmedArticleList.size());
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
