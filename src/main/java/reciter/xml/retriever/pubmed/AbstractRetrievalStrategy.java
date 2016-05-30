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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reciter.engine.ReCiterEngineProperty;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.service.ESearchResultService;
import reciter.service.impl.ESearchResultServiceImpl;
import reciter.xml.parser.pubmed.PubmedXmlParser;
import reciter.xml.parser.pubmed.PubmedXmlQuery;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
import reciter.xml.parser.pubmed.model.PubmedArticle;
import reciter.xml.parser.translator.ArticleTranslator;
import reciter.xml.retriever.pubmed.json.EsearchObject;
import reciter.xml.retriever.pubmed.json.EsearchObjectJsonDeserializer;
import reciter.xml.retriever.pubmed.json.EsearchResult;
import reciter.xml.retriever.pubmed.json.EsearchResultJsonDeserializer;

public abstract class AbstractRetrievalStrategy implements RetrievalStrategy {

	protected static int THRESHOLD = 2000;
	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractRetrievalStrategy.class);

	protected abstract String constructInitialQuery(TargetAuthor targetAuthor);
	protected abstract String constructStrictQuery(TargetAuthor targetAuthor);
	
	protected boolean isRetrieveExceedThreshold;
	
	public void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}
	
	public static void main(String[] args) {
		AffiliationInDbRetrievalStrategy s = new AffiliationInDbRetrievalStrategy();
//		try {
			PubMedRetrieverWorker worker = new PubMedRetrieverWorker("wang%20y[au]", "wangy", "wangy", 85732);
			Thread workerThread = new Thread(worker);
			workerThread.start();
//			List<String> pmids = s.retrievePmids("wang[au]");
//			s.persistQueryResults("yiwang", pmids);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	public boolean retrieve(TargetAuthor targetAuthor) throws IOException {

		String initialQuery = URLEncoder.encode(constructInitialQuery(targetAuthor), "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(initialQuery);

		// check initial query's threshold. if it's greater than the threshold, retrieve using the strict query.
		if (handler.getCount() > THRESHOLD) {
			String strictQuery = URLEncoder.encode(constructStrictQuery(targetAuthor), "UTF-8");
			PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(strictQuery);
			if (isRetrieveExceedThreshold) {
				startRetrieval(strictQuery, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
				return true;
			} else {
				return false;
			}
		} else {
			startRetrieval(initialQuery, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handler.getCount());
			return true;
		}
	}
	
	/**
	 * Initializes and starts a thread that handles the retrieval process.
	 * 
	 * @param query
	 * @param commonLocation
	 * @param cwid
	 * @param count
	 */
	private void startRetrieval(String query, String commonLocation, String cwid, int count) {
		PubMedRetrieverWorker worker = new PubMedRetrieverWorker(query, commonLocation, cwid, count);
		Thread workerThread = new Thread(worker);
		workerThread.start();
	}

	public void persistQueryResults(String cwid, List<String> pmids) {
		ESearchResultService eSearchResultService = new ESearchResultServiceImpl();
		eSearchResultService.insertESearchResult(cwid, pmids);
	}
	
	/**
	 * Query the PubMed database and returns a list of PMIDs.
	 * @param query
	 * @return
	 * @throws IOException 
	 */
	public List<String> retrievePmids(String query) throws IOException {
		List<String> pmids = new ArrayList<String>();
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(query);
		pubmedXmlQuery.setRetMode("json");
		pubmedXmlQuery.setRetMax(1);

		String pageText;
		// "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=wang[au]&retmode=json&retmax=1"
		String firstQuery = pubmedXmlQuery.buildESearchQuery();

		URL url = new URL(firstQuery);
		URLConnection conn = url.openConnection();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
			pageText = reader.lines().collect(Collectors.joining("\n"));
		}

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(EsearchObject.class, new EsearchObjectJsonDeserializer());
		gsonBuilder.registerTypeAdapter(EsearchResult.class, new EsearchResultJsonDeserializer());
		Gson gson = gsonBuilder.create();

		// Parse JSON to Java
		EsearchObject eSearchObject = gson.fromJson(pageText, EsearchObject.class);
		String count = eSearchObject.geteSearchResult().getCount();
		
		PubmedXmlQuery queryToGetPmids = new PubmedXmlQuery();
		queryToGetPmids.setTerm(query);
		queryToGetPmids.setRetMode("json");
		queryToGetPmids.setRetMax(Integer.parseInt(count));
		URL secondUrl = new URL(queryToGetPmids.buildESearchQuery());
		conn = secondUrl.openConnection();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
			pageText = reader.lines().collect(Collectors.joining("\n"));
		}

		// Parse JSON to Java
		EsearchObject eSearchObject2 = gson.fromJson(pageText, EsearchObject.class);
		String[] results = eSearchObject2.geteSearchResult().getIdList();
		
		for (String r : results) {
			pmids.add(r);
		}
		return pmids;
	}

	protected PubmedESearchHandler getPubmedESearchHandler(String query) throws IOException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery(query);
		String fullUrl = pubmedXmlQuery.buildESearchQuery(); // build eSearch query.
		slf4jLogger.info("URL=[" + fullUrl + "]");
		PubmedESearchHandler pubmedESearchHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(fullUrl).openStream();
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(esearchStream, pubmedESearchHandler);
		} catch (SAXException | ParserConfigurationException e) {
			slf4jLogger.error("Error parsing XML file for query=[" + query + "].", e);
		}
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
