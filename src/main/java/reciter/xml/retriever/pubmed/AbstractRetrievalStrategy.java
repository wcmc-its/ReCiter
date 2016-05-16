package reciter.xml.retriever.pubmed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import reciter.engine.ReCiterEngineProperty;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.xml.parser.pubmed.PubmedXmlParser;
import reciter.xml.parser.pubmed.PubmedXmlQuery;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
import reciter.xml.parser.pubmed.model.PubmedArticle;
import reciter.xml.parser.translator.ArticleTranslator;

public abstract class AbstractRetrievalStrategy implements RetrievalStrategy {

	protected static int THRESHOLD = 2000;
	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractRetrievalStrategy.class);
	
	protected abstract String constructInitialQuery(TargetAuthor targetAuthor);
	protected abstract String constructStrictQuery(TargetAuthor targetAuthor);
	
	@Override
	public void retrieve(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		String initialQuery = URLEncoder.encode(constructInitialQuery(targetAuthor), "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(initialQuery);

		if (handler.getCount() > THRESHOLD) {
			String strictQuery = URLEncoder.encode(constructStrictQuery(targetAuthor), "UTF-8");
			PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(strictQuery);
			
			// Still greater than THRESHOLD, do not retrieve.
			if (handlerVerboseFirstName.getCount() > THRESHOLD) {
				return;
			} else {
				fetch(strictQuery, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
			}
		} else {
			fetch(initialQuery, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handler.getCount());
		}
	}
	
	/**
	 * Query the PubMed database and returns a list of PMIDs.
	 * @param query
	 * @return
	 * @throws IOException 
	 */
	public List<Integer> retrievePmids(String query, int numberOfPubmedArticles) throws IOException {
		List<Integer> pmids = new ArrayList<Integer>();
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(query);
		pubmedXmlQuery.setRetMode("json");
		pubmedXmlQuery.setRetMax(1);
		
		String pageText;
		URL url = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=wang[au]&retmode=json&retmax=1");
		URLConnection conn = url.openConnection();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
		    pageText = reader.lines().collect(Collectors.joining("\n"));
		}
		
		System.out.println(pageText);
		return pmids;
	}
	
	/**
	 * Fetch PubMed xmls using <code>pubmedQuery</code> and store the xmls in <code>commonDirectory</code> specified
	 * by <code>cwid</code>
	 * @param pubmedQuery
	 * @param commonDirectory
	 * @param cwid
	 * @param numberOfPubmedArticles
	 */
	public void fetch(String pubmedQuery, String commonDirectory, String cwid, int numberOfPubmedArticles) {

		slf4jLogger.info("query=[" + pubmedQuery + "]");
		slf4jLogger.info("number of articles needed to be retrieved=[" + numberOfPubmedArticles + "].");

		// Get the count (number of publications for this query).
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(pubmedQuery);

		// Retrieve the publications retMax records at one time and store to disk.
		int currentRetStart = 0;

		// Number of partitions that we need to finish retrieving all XML.
		int numSteps = (int) Math.ceil((double)numberOfPubmedArticles / pubmedXmlQuery.getRetMax()); 

		// Use the retstart value to iteratively fetch all XMLs.
		for (int i = 0; i < numSteps; i++) {
			// Get webenv value.
			pubmedXmlQuery.setRetStart(currentRetStart);
			String eSearchUrl = pubmedXmlQuery.buildESearchQuery();

			pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

			// Use the webenv value to retrieve xml.
			String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();
			slf4jLogger.info("PubMed EFetch Url = " + eFetchUrl);

			// Save the xml file to directory data/xml/cwid
			LocalDateTime timePoint = LocalDateTime.now();
			saveXml(eFetchUrl, commonDirectory, cwid, timePoint.toString().replace(":", "-"));

			// Update the retstart value.
			currentRetStart += pubmedXmlQuery.getRetMax();
			pubmedXmlQuery.setRetStart(currentRetStart);
		}
	}
	
	/**
	 * Save the url (XML) content in the {@code directoryLocation} with directory
	 * name {@code directoryName} and file name {@code fileName}.
	 * 
	 * @param url URL
	 * @param commonDirectory directory path.
	 * @param cwid directory name.
	 * @param xmlFileName file name.
	 */
	public void saveXml(String url, String commonDirectory, String cwid, String xmlFileName) {
		
		slf4jLogger.info("commonDirectory=[" + commonDirectory + "].");
		
		File dir = new File(commonDirectory + cwid);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));
			String outputFileName = commonDirectory + cwid + "/" + xmlFileName + ".xml";
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileName), "UTF-8"));
			
			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				bufferedWriter.write(inputLine);
				bufferedWriter.newLine();
			}
			
			bufferedReader.close();
			bufferedWriter.close();
		} catch (IOException e) {
			slf4jLogger.warn(e.getMessage());
		}
	}
	
	
	protected PubmedESearchHandler getPubmedESearchHandler(String query) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery(query);
		String fullUrl = pubmedXmlQuery.buildESearchQuery(); // build eSearch query.
		slf4jLogger.info("URL=[" + fullUrl + "]");
		PubmedESearchHandler pubmedESearchHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(fullUrl).openStream();
		SAXParserFactory.newInstance().newSAXParser().parse(esearchStream, pubmedESearchHandler);
		return pubmedESearchHandler;
	}

	protected PubmedESearchHandler getPubmedESearchHandlerJson(String query) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery(query);
		pubmedXmlQuery.setRetMode("json");
		String fullUrl = pubmedXmlQuery.buildESearchQuery(); // build eSearch query.
		slf4jLogger.info("URL=[" + fullUrl + "]");
		PubmedESearchHandler pubmedESearchHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(fullUrl).openStream();
		SAXParserFactory.newInstance().newSAXParser().parse(esearchStream, pubmedESearchHandler);
		return pubmedESearchHandler;
	}
	
	/**
	 * Go through each of the articles retrieved by email and check if the first name matches the one in the
	 * database. If it doesn't, include that first name in the initial retrieval.
	 *
	 * @param targetAuthor
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected Set<AuthorName> getAuthorNameVariationFromEmails(TargetAuthor targetAuthor) 
			throws ParserConfigurationException, SAXException, IOException {
		Set<AuthorName> nameVariations = new HashSet<AuthorName>();
		List<PubmedArticle> pubmedArticles = getPubmedArticle(ReCiterEngineProperty.emailXmlFolder, targetAuthor.getCwid());
		List<ReCiterArticle> reCiterArticles = new ArrayList<ReCiterArticle>();
		for (PubmedArticle pubmedArticle : pubmedArticles) {
			reCiterArticles.add(ArticleTranslator.translate(pubmedArticle, null));
		}

		String targetAuthorFirstName = targetAuthor.getAuthorName().getFirstName();
		String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();

		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			List<ReCiterAuthor> authors = reCiterArticle.getArticleCoAuthors().getAuthors();
			for (ReCiterAuthor author : authors) {
				String lastName = author.getAuthorName().getLastName();
				String firstName = author.getAuthorName().getFirstName();
				if (StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName) && 
						!StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {
					nameVariations.add(author.getAuthorName());
				}
			}
		}

		// Set targetAuthor's author name variations.
		targetAuthor.setAuthorNameVariationsRetrievedFromPubmedUsingEmail(nameVariations);
		return nameVariations;
	}
	
	/**
	 * Retrieve and parse the PubMed xml in <code>commonDirectory</code> specified by <code>cwid</code>
	 * 
	 * @param query
	 * @param cwid
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public List<PubmedArticle> getPubmedArticle(String commonDirectory, String cwid) 
			throws ParserConfigurationException, SAXException, IOException {
		List<PubmedArticle> pubmedArticleList = new ArrayList<PubmedArticle>();

		File directory = new File(commonDirectory + cwid);

		if (directory.exists()) {

			PubmedXmlParser pubmedXmlParser = new PubmedXmlParser(new PubmedEFetchHandler());

			for (File xmlFile : directory.listFiles()) {
				pubmedArticleList.addAll(pubmedXmlParser.parse(xmlFile));
			}
		} else {
			slf4jLogger.warn("Please retrieve PubMed articles for [" + cwid + "] to directory =[" + commonDirectory + "]");
		}
		return pubmedArticleList;
	}
}
