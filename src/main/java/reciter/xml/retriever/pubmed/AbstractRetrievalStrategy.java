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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reciter.database.mongo.model.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.xml.parser.pubmed.PubmedXmlQuery;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
import reciter.xml.parser.scopus.ScopusXmlHandler;
import reciter.xml.parser.scopus.ScopusXmlQuery;
import reciter.xml.parser.scopus.model.ScopusArticle;
import reciter.xml.retriever.pubmed.json.EsearchObject;
import reciter.xml.retriever.pubmed.json.EsearchObjectJsonDeserializer;
import reciter.xml.retriever.pubmed.json.EsearchResult;
import reciter.xml.retriever.pubmed.json.EsearchResultJsonDeserializer;

@Configurable
public abstract class AbstractRetrievalStrategy implements RetrievalStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractRetrievalStrategy.class);

	/**
	 * PubMed Query
	 */
	private String pubMedQuery;

	/**
	 * Number of articles retrieved by query.
	 */
	private int numberOfPubmedArticles;

	/**
	 * Retrieval threshold.
	 */
	protected static final int DEFAULT_THRESHOLD = 2000;

	/**
	 * Scopus retrieval threshold.
	 */
	protected static final int SCOPUS_DEFAULT_THRESHOLD = 99;

	/**
	 * Scopus retrieval max threshold.
	 */
	protected static final int SCOPUS_MAX_THRESHOLD = 100;

	/**
	 * Should retrieved if threshold exceeds.
	 */
	protected boolean isRetrieveExceedThreshold;

	/**
	 * Initial query.
	 * @param targetAuthor
	 * @return
	 */
	protected abstract String constructInitialQuery(Identity identity);

	/**
	 * Strict query.
	 * @param targetAuthor
	 * @return
	 */
	protected abstract String constructStrictQuery(Identity identity);

	public void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}

	@Override
	public void constructPubMedQuery(Identity identity) throws IOException {

		// Construct initial relaxed query.
		String constructedInitialQuery = constructInitialQuery(identity);
		if (constructedInitialQuery != null) {
			String initialQuery = URLEncoder.encode(constructedInitialQuery, "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(initialQuery);

			// check initial query's threshold. if it's greater than the threshold, retrieve using the strict query.
			if (handler.getCount() > DEFAULT_THRESHOLD) {
				String constructedStrictQuery = constructStrictQuery(identity);
				if (constructedStrictQuery != null) {
					String strictQuery = URLEncoder.encode(constructedStrictQuery, "UTF-8");
					PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(strictQuery);
					pubMedQuery = strictQuery;
					numberOfPubmedArticles = handlerVerboseFirstName.getCount();
				}
			} else {
				pubMedQuery = initialQuery;
				numberOfPubmedArticles = handler.getCount();
			}
		}
	}

	public String getPubMedQuery() {
		return pubMedQuery;
	}

	public void setPubMedQuery(String pubMedQuery) {
		this.pubMedQuery = pubMedQuery;
	}

	public int getNumberOfPubmedArticles() {
		return numberOfPubmedArticles;
	}

	public void setNumberOfPubmedArticles(int numberOfPubmedArticles) {
		this.numberOfPubmedArticles = numberOfPubmedArticles;
	}

	/**
	 * Initializes and starts a thread that handles the retrieval process.
	 * 
	 * @param query
	 * @param commonLocation
	 * @param cwid
	 * @param count
	 */
	@Override
	public List<PubMedArticle> retrieve()  {

		// Check if some of the pmids has already been retrieved. And modify the pubMedQuery to not include
		// already retrieved articles.

		int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numAvailableProcessors);

		// Get the count (number of publications for this query).
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(pubMedQuery);

		// The number of articles will be less than 10,000. Set retMax equal to minimum of number of articles needed to be
		// retrieved divided by the number of available processors and 10,000.
		// If number of articles is less than 4, use number of articles as retmax.
		pubmedXmlQuery.setRetMax(Math.min(Math.max(numberOfPubmedArticles / Math.max(numAvailableProcessors, 1), numberOfPubmedArticles)
				, PubmedXmlQuery.DEFAULT_RETMAX));
		slf4jLogger.info("numAvailableProcessors=[" + numAvailableProcessors + "] retMax=[" 
				+ pubmedXmlQuery.getRetMax() + "], pubMedQuery=[" + pubMedQuery + "], "
				+ "numberOfPubmedArticles=[" + numberOfPubmedArticles + "].");

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
			slf4jLogger.info("eFetchUrl=[" + eFetchUrl + "].");
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
			}).forEach(list::add);
		} catch (InterruptedException e) {
			slf4jLogger.error("Unable to invoke callable.", e);
		}

		List<PubMedArticle> results = new ArrayList<PubMedArticle>();
		list.forEach(results::addAll);
		return results;
	}

	@Override
	public List<ScopusArticle> retrieveScopus(List<Long> pmids) {
		StringBuffer sb = new StringBuffer();
		List<String> pmidQueries = new ArrayList<String>();
		for (int i = 0; i < pmids.size(); i++) {
			long pmid = pmids.get(i);
			if (i == 0 || (i % SCOPUS_DEFAULT_THRESHOLD != 0 && i != pmids.size() - 1)) {
				sb.append("pmid(");
				sb.append(pmid);
				sb.append(")+OR+");
			} else {
				sb.append("pmid(");
				sb.append(pmid);
				sb.append(")");
			}
			if (i != 0 && i % SCOPUS_DEFAULT_THRESHOLD == 0) {
				pmidQueries.add(sb.toString());
				sb = new StringBuffer();
			}
		}
		List<Callable<List<ScopusArticle>>> callables = new ArrayList<Callable<List<ScopusArticle>>>();

		for (String query : pmidQueries) {
			ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder(query, SCOPUS_MAX_THRESHOLD).build();
			String scopusUrl = scopusXmlQuery.getQueryUrl();
			ScopusUriParserCallable scopusUriParserCallable = new ScopusUriParserCallable(new ScopusXmlHandler(), scopusUrl);
			callables.add(scopusUriParserCallable);
		}

		List<List<ScopusArticle>> list = new ArrayList<List<ScopusArticle>>();

		int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numAvailableProcessors);

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
			}).forEach(list::add);
		} catch (InterruptedException e) {
			slf4jLogger.error("Unable to invoke callable.", e);
		}

		List<ScopusArticle> results = new ArrayList<ScopusArticle>();
		list.forEach(results::addAll);
		return results;
	}

	/**
	 * Retrieve only the pmids for a query.
	 * 
	 * @param pubMedQuery
	 * @param numberOfPubmedArticles
	 * @return
	 * @throws IOException
	 */
	public List<Long> retrievePmids(String pubMedQuery, int numberOfPubmedArticles) throws IOException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(pubMedQuery);
		pubmedXmlQuery.setRetMode("json");
		pubmedXmlQuery.setRetMax(numberOfPubmedArticles);

		// "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=wang[au]&retmode=json&retmax=100"
		String eSearchQuery = pubmedXmlQuery.buildESearchQuery();

		// Retrieve ESearch result.
		URLConnection conn = new URL(eSearchQuery).openConnection();
		String pageText = null;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
			pageText = reader.lines().collect(Collectors.joining("\n"));
		}

		// Initialize deserializer.
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(EsearchObject.class, new EsearchObjectJsonDeserializer());
		gsonBuilder.registerTypeAdapter(EsearchResult.class, new EsearchResultJsonDeserializer());
		Gson gson = gsonBuilder.create();

		// Parse JSON to EsearchObject.
		EsearchObject eSearchObject = gson.fromJson(pageText, EsearchObject.class);
		String[] results = eSearchObject.geteSearchResult().getIdList();

		List<Long> pmids = new ArrayList<Long>();
		for (String r : results) {
			pmids.add(Long.valueOf(r));
		}
		return pmids;
	}

	/**
	 * Query the PubMed database and returns a list of PMIDs.
	 * @param query
	 * @return
	 * @throws IOException 
	 */
	@Override
	public String[] retrievePmids(String query) throws IOException {
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
		return results;
	}

	protected PubmedESearchHandler getPubmedESearchHandler(String query) throws IOException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery(query);
		String fullUrl = pubmedXmlQuery.buildESearchQuery(); // build eSearch query.
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
