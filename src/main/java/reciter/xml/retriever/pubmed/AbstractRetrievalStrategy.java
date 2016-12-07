package reciter.xml.retriever.pubmed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import reciter.database.mongo.model.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.xml.parser.pubmed.PubmedXmlQuery;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
import reciter.xml.parser.scopus.ScopusXmlHandler;
import reciter.xml.parser.scopus.ScopusXmlQuery;
import reciter.xml.retriever.pubmed.json.EsearchObject;
import reciter.xml.retriever.pubmed.json.EsearchObjectJsonDeserializer;
import reciter.xml.retriever.pubmed.json.EsearchResult;
import reciter.xml.retriever.pubmed.json.EsearchResultJsonDeserializer;

@Configurable
public abstract class AbstractRetrievalStrategy implements RetrievalStrategy {

	public static class RetrievalResult {
		private final Map<Long, PubMedArticle> pubMedArticles;
		private final List<PubMedQueryResult> pubMedQueryResults;
		public RetrievalResult(Map<Long, PubMedArticle> pubMedArticles, List<PubMedQueryResult> pubMedQueryResults) {
			this.pubMedArticles = pubMedArticles;
			this.pubMedQueryResults = pubMedQueryResults;
		}
		public Map<Long, PubMedArticle> getPubMedArticles() {
			return pubMedArticles;
		}
		public List<PubMedQueryResult> getPubMedQueryResults() {
			return pubMedQueryResults;
		}
	}

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractRetrievalStrategy.class);

	/**
	 * Retrieval threshold.
	 */
	protected static final int DEFAULT_THRESHOLD = 2000;

	/**
	 * Scopus retrieval threshold.
	 */
	protected static final int SCOPUS_DEFAULT_THRESHOLD = 24;

	/**
	 * Scopus retrieval max threshold.
	 */
	protected static final int SCOPUS_MAX_THRESHOLD = 25;

	/**
	 * Should retrieved if threshold exceeds.
	 */
	protected boolean isRetrieveExceedThreshold;

	public void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold) {
		this.isRetrieveExceedThreshold = isRetrieveExceedThreshold;
	}

	protected abstract List<PubMedQuery> buildQuery(Identity identity);
	protected abstract List<PubMedQuery> buildQuery(Identity identity, LocalDate startDate, LocalDate endDate);

	@Override
	public RetrievalResult retrievePubMedArticles(Identity identity, LocalDate startDate, LocalDate endDate) throws IOException {
		List<PubMedQuery> pubMedQueries = buildQuery(identity, startDate, endDate);
		return retrievePubMedArticles(identity, pubMedQueries);
	}

	@Override
	public RetrievalResult retrievePubMedArticles(Identity identity) throws IOException {
		List<PubMedQuery> pubMedQueries = buildQuery(identity);
		for (PubMedQuery pubMedQuery : pubMedQueries) {
			slf4jLogger.info(pubMedQuery.toString());
		}
		return retrievePubMedArticles(identity, pubMedQueries);
	}

	private RetrievalResult retrievePubMedArticles(Identity identity, List<PubMedQuery> pubMedQueries) throws IOException {

		Map<Long, PubMedArticle> pubMedArticles = new HashMap<Long, PubMedArticle>();

		slf4jLogger.info("Query size: " + pubMedQueries.size());
		List<PubMedQueryResult> pubMedQueryResults = new ArrayList<PubMedQueryResult>();

		for (PubMedQuery pubMedQuery : pubMedQueries) {

			String encodedInitialQuery = URLEncoder.encode(pubMedQuery.getLenientQuery().getQuery(), "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(encodedInitialQuery);

			// check number of PubMed results returned by initial query.
			// If it's greater than the threshold, query using the strict query.
			pubMedQuery.getLenientQuery().setNumResult(handler.getCount());

			if (handler.getCount() > DEFAULT_THRESHOLD) {
				String constructedStrictQuery = pubMedQuery.getStrictQuery().getQuery();
				String strictQuery = URLEncoder.encode(constructedStrictQuery, "UTF-8");
				PubmedESearchHandler strictSearchHandler = getPubmedESearchHandler(strictQuery);

				pubMedQuery.getStrictQuery().setNumResult(strictSearchHandler.getCount());

				// only retrieve articles if number is less than threshold, otherwise the article download
				// may take too long
				if (strictSearchHandler.getCount() <= DEFAULT_THRESHOLD) {
					List<PubMedArticle> result = retrievePubMed(constructedStrictQuery, strictSearchHandler.getCount());
					for (PubMedArticle pubMedArticle : result) {
						long pmid = pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid();
						if (!pubMedArticles.containsKey(pmid)) {
							pubMedArticles.put(pmid, pubMedArticle);
						}
					}
					pubMedQuery.getStrictQuery().setUsed(true);
				}
			} else {
				List<PubMedArticle> result = retrievePubMed(encodedInitialQuery, handler.getCount());
				for (PubMedArticle pubMedArticle : result) {
					long pmid = pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid();
					if (!pubMedArticles.containsKey(pmid)) {
						pubMedArticles.put(pmid, pubMedArticle);
					}
				}
				pubMedQuery.getLenientQuery().setUsed(true);
			}

			pubMedQueryResults.add(pubMedQuery.getLenientQuery());
			pubMedQueryResults.add(pubMedQuery.getStrictQuery());
		}
		slf4jLogger.info("Found " + pubMedArticles.size() + " PubMed articles for " + identity.getCwid() 
		+ " using retrieval strategy [" + getRetrievalStrategyName() + "]");

		return new RetrievalResult(pubMedArticles, pubMedQueryResults);
	}

	private static final String nodeUrlBegin = "https://reciter-pubmed-retrieval-";
	private static final String nodeUrlEnd = ".herokuapp.com/reciter/retrieve/pubmed/by/query?";
	private static final int nodeSize = 2;

	private static final String scopusNodeUrlBegin = "https://reciter-pubmed-retrieval-";
	private static final String scopusNodeUrlEnd = ".herokuapp.com/reciter/retrieve/scopus/by/pmids?";
	private static final int scopusNodeSize = 3;

	/**
	 * Randomly select a node.
	 * @return
	 */
	private String loadBalance() {
		int nodeSelected = (int) (Math.random() * nodeSize + 1);
		return nodeUrlBegin + nodeSelected + nodeUrlEnd;
	}

	private String loadBalanceScopus() {
		int nodeSelected = (int) (Math.random() * scopusNodeSize + 1);
		return scopusNodeUrlBegin + nodeSelected + scopusNodeUrlEnd;
	}

	private List<PubMedArticle> retrievePubMedViaRest(String pubMedQuery, int numberOfPubmedArticles) {
		String nodeUrl = loadBalance();
		nodeUrl += "query=" + pubMedQuery + "&numberOfPubmedArticles=" + numberOfPubmedArticles;
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<PubMedArticle[]> responseEntity = restTemplate.getForEntity(nodeUrl, PubMedArticle[].class);
			PubMedArticle[] pubMedArticles = responseEntity.getBody();
			return Arrays.asList(pubMedArticles);
		} catch (Exception e) {
			slf4jLogger.error("Unable to retrieve via external REST api.", e);
			return Collections.emptyList();
		}
	}

	private List<ScopusArticle> retrieveScopusViaRest(Collection<Long> pmids) {
		String nodeUrl = loadBalanceScopus();
		String query = StringUtils.join(pmids, ",");
		nodeUrl += query;
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<ScopusArticle[]> responseEntity = restTemplate.getForEntity(nodeUrl, ScopusArticle[].class);
			ScopusArticle[] scopusArticles = responseEntity.getBody();
			return Arrays.asList(scopusArticles);
		} catch (Exception e) {
			slf4jLogger.error("Unable to retrieve via external REST api.", e);
			return Collections.emptyList();
		}
	}
	
	// @Document(collection = "pubmedarticle") @Document(collection = "scopusarticle")

	/**
	 * Initializes and starts threads that handles the retrieval process. Partition the number of articles
	 * into manageable pieces and ask each thread to handle one partition.
	 * 
	 * @param query
	 * @param commonLocation
	 * @param cwid
	 * @param count
	 */
	public List<PubMedArticle> retrievePubMed(String pubMedQuery, int numberOfPubmedArticles)  {

		List<PubMedArticle> results = retrievePubMedViaRest(pubMedQuery, numberOfPubmedArticles);

		if (results.isEmpty()) {

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

			list.forEach(results::addAll);
			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<ScopusArticle> retrieveScopus(Collection<Long> pmids) {

		List<ScopusArticle> results = new ArrayList<ScopusArticle>();
		results = retrieveScopusViaRest(pmids);

		if (results.isEmpty()) {

			StringBuffer sb = new StringBuffer();
			List<String> pmidQueries = new ArrayList<String>();
			int i = 0;
			Iterator<Long> itr = pmids.iterator();
			while (itr.hasNext()) {
				long pmid = itr.next();
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
				i++;
			}
			// add the remaining pmids
			String remaining = sb.toString();
			if (!remaining.isEmpty()) {
				pmidQueries.add(remaining);
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

			list.forEach(results::addAll);
		}
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
			slf4jLogger.error("Error parsing XML file for query=[" + query + "], full url=[" + fullUrl + "]", e);
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
}
