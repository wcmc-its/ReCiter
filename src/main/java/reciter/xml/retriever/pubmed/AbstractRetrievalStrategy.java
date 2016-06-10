package reciter.xml.retriever.pubmed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reciter.engine.ReCiterEngineProperty;
import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;
import reciter.service.ESearchResultService;
import reciter.service.impl.ESearchResultServiceImpl;
import reciter.xml.parser.pubmed.PubmedXmlQuery;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
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

	@Override
	public List<PubMedArticle> retrieve(TargetAuthor targetAuthor) throws IOException {

		String initialQuery = URLEncoder.encode(constructInitialQuery(targetAuthor), "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(initialQuery);

		// check initial query's threshold. if it's greater than the threshold, retrieve using the strict query.
		if (handler.getCount() > THRESHOLD) {
			String strictQuery = URLEncoder.encode(constructStrictQuery(targetAuthor), "UTF-8");
			PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(strictQuery);
			if (isRetrieveExceedThreshold) {
				return retrieve(strictQuery, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
			}
		} else {
			return retrieve(initialQuery, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handler.getCount());
		}
		return new ArrayList<PubMedArticle>();
	}

	/**
	 * Initializes and starts a thread that handles the retrieval process.
	 * 
	 * @param query
	 * @param commonLocation
	 * @param cwid
	 * @param count
	 */
	private List<PubMedArticle> retrieve(String pubmedQuery, String commonDirectory, String cwid, int numberOfPubmedArticles)  {
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		// Get the count (number of publications for this query).
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(pubmedQuery);

		// Retrieve the publications retMax records at one time and store to disk.
		int currentRetStart = 0;

		// Number of partitions that we need to finish retrieving all XML.
		int numSteps = (int) Math.ceil((double)numberOfPubmedArticles / pubmedXmlQuery.getRetMax()); 

		List<Callable<List<PubMedArticle>>> callables = new ArrayList<Callable<List<PubMedArticle>>>();
		
		// Use the retstart value to iteratively fetch all XMLs.
		for (int i = 0; i < numSteps; i++) {
			// Get webenv value.
			pubmedXmlQuery.setRetStart(currentRetStart);
			String eSearchUrl = pubmedXmlQuery.buildESearchQuery();

			pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

			// Use the webenv value to retrieve xml.
			String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();
			PubMedUriParserCallable pubMedUriParserCallable = new PubMedUriParserCallable(new PubmedEFetchHandler(), eFetchUrl);
			callables.add(pubMedUriParserCallable);
			
			// Update the retstart value.
			currentRetStart += pubmedXmlQuery.getRetMax();
			pubmedXmlQuery.setRetStart(currentRetStart);
		}
		
		List<List<PubMedArticle>> list = new ArrayList<List<PubMedArticle>>();
		
		try {
			executor.invokeAll(callables)
				.stream()
				.map(future -> {
					try {
						return future.get();
					}
					catch (Exception e) {
						throw new IllegalStateException(e);
					}
				})
				.forEach(list::add);
		} catch (InterruptedException e) {
			slf4jLogger.error("Unable to invoke callable.", e);
		}
		
		List<PubMedArticle> results = new ArrayList<PubMedArticle>();
		list.forEach(results::addAll);
		return results;
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
	//	protected Set<AuthorName> getAuthorNameVariationFromEmails(TargetAuthor targetAuthor) 
	//			throws ParserConfigurationException, SAXException, IOException {
	//		Set<AuthorName> nameVariations = new HashSet<AuthorName>();
	//		List<PubMedArticle> pubmedArticles = getPubmedArticle(ReCiterEngineProperty.emailXmlFolder, targetAuthor.getCwid());
	//		List<ReCiterArticle> reCiterArticles = new ArrayList<ReCiterArticle>();
	//		for (PubMedArticle pubmedArticle : pubmedArticles) {
	//			reCiterArticles.add(ArticleTranslator.translate(pubmedArticle, null));
	//		}
	//
	//		String targetAuthorFirstName = targetAuthor.getAuthorName().getFirstName();
	//		String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();
	//
	//		for (ReCiterArticle reCiterArticle : reCiterArticles) {
	//			List<ReCiterAuthor> authors = reCiterArticle.getArticleCoAuthors().getAuthors();
	//			for (ReCiterAuthor author : authors) {
	//				String lastName = author.getAuthorName().getLastName();
	//				String firstName = author.getAuthorName().getFirstName();
	//				if (StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName) && 
	//						!StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {
	//					nameVariations.add(author.getAuthorName());
	//				}
	//			}
	//		}
	//
	//		// Set targetAuthor's author name variations.
	//		targetAuthor.setAuthorNameVariationsRetrievedFromPubmedUsingEmail(nameVariations);
	//		return nameVariations;
	//	}
}
