package xmlparser.pubmed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xmlparser.AbstractXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;

/**
 * Fetches XML articles from PubMed and writes to a file. One can specify the location
 * where the fetched XML files will go, otherwise a default location will be used based
 * on the configuration setting.
 * 
 * @author jil3004
 *
 */
public class PubmedXmlFetcher extends AbstractXmlFetcher {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(PubmedXmlFetcher.class);
	private static final String PROPERTIES_FILE_LOCATION = "src/main/resources/config/reciter.properties";
	private PubmedXmlParser pubmedXmlParser;

	private void loadProperty() {
		Properties p = new Properties();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(PROPERTIES_FILE_LOCATION);
			p.load(inputStream);
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage(), e);
		}

		directory = p.getProperty("pubmed_xml_folder");
	}

	public PubmedXmlFetcher() {
		loadProperty();
		pubmedXmlParser = new PubmedXmlParser(new PubmedEFetchHandler());
	}

	public PubmedXmlFetcher(String directory) {
		super(directory);
		pubmedXmlParser = new PubmedXmlParser(new PubmedEFetchHandler());
	}

	// Create default location if not exist.
	public void createPubMedLocation() {
		File pubmedDir = new File(directory);
		if (!pubmedDir.exists()) {
			pubmedDir.mkdirs();
		}
	}

	/**
	 * Get all the pubmed articles from PubMed from directory.
	 * 
	 * @param query
	 * @param fileName
	 * @return
	 */
	public List<PubmedArticle> getPubmedArticle(String query, String fileName) {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();

		// Create default location if not exist.
		createPubMedLocation();

		File directory = new File(getDirectory() + fileName);
		if (!directory.exists()) {
			slf4jLogger.info("Fetching PubMed articles from PubMed for " + fileName);
			fetch(query, fileName);
		}

		for (File xmlFile : new File(getDirectory() + fileName).listFiles()) {
			pubmedXmlParser.setXmlInputSource(xmlFile);
			pubmedArticleList.addAll(pubmedXmlParser.parse());
		}
		return pubmedArticleList;
	}

	/**
	 * Fetch all the publications for this query in PubMed and store it on disk with name fileName.
	 * 
	 * @param lastName last name of the author.
	 * @param firstInitial first initial of the author.
	 * @param cwid cwid of the author.
	 */
	public void fetch(String query, String fileName) {
		int numPubMedArticles = 0;

		// Get the count (number of publications for this query).
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(query);

		// set retmax = 1 so that query can be executed fast.
		pubmedXmlQuery.setRetMax(1);

		String eSearchUrl = pubmedXmlQuery.buildESearchQuery();
		PubmedESearchHandler xmlHandler = PubmedESearchHandler.executeESearchQuery(eSearchUrl);
		numPubMedArticles = xmlHandler.getCount();

		slf4jLogger.info("PubMed Seach Query: " + eSearchUrl);
		slf4jLogger.info("Number of articles need to be retrieved for : " + fileName + " is "+ numPubMedArticles);

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
			slf4jLogger.info("PubMed EFetch Url = " + eFetchUrl);

			// Save the xml file to directory data/xml/cwid
			saveXml(eFetchUrl, fileName, fileName + "_" + i);

			// Update the retstart value.
			currentRetStart += pubmedXmlQuery.getRetMax();
			pubmedXmlQuery.setRetStart(currentRetStart);
		}
	}

	/**
	 * 
	 * @param filePath
	 * @param expectedResult
	 * @return
	 */
	public boolean isNumberOfArticleMatch(String filePath, int expectedResult) {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();
		for (File xmlFile : new File(filePath).listFiles()) {
			pubmedXmlParser.setXmlInputSource(xmlFile);
			pubmedArticleList.addAll(pubmedXmlParser.parse());
		}
		return pubmedArticleList.size() == expectedResult;
	}

	/**
	 * <p>
	 * Performs a check on a PubMed retrieved XML file for {@code <ERROR>Unable to obtain query #1</ERROR>} on
	 * {@code line 3}. 
	 * </p>
	 * 
	 * <p>
	 * This function might be useful if you need to perform a check on the XML files for this error
	 * message, and perform a re-retrieval if necessary.
	 * </p>
	 * 
	 * @param filePath file location of the PubMed XML file.
	 * @return true if XML file contains the {@code <ERROR>Unable to obtain query #1</ERROR>}, false otherwise.
	 */
	public boolean isUnableToObtainQueryError(String filePath) {
		int lineNumber = 3;
		String lineContent = null;
		String unableToObtainQueryMessage = "<ERROR>Unable to obtain query #1</ERROR>";
		try {
			lineContent = Files.lines(Paths.get(filePath))
					.skip(lineNumber)
					.findFirst()
					.get()
					.trim();
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage());
		}
		return unableToObtainQueryMessage.equals(lineContent);
	}
}
