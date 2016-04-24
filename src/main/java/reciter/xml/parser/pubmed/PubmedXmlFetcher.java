package reciter.xml.parser.pubmed;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import reciter.xml.parser.AbstractXmlFetcher;
import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
import reciter.xml.parser.pubmed.model.PubmedArticle;
import reciter.xml.parser.translator.ArticleTranslator;

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
	private static final String AFFILIATION_QUERY = "AND ((new york) OR 10065 OR 10021 OR weill OR cornell OR (newyork AND presbyterian) OR (new york AND presbyterian) OR HSS OR (hospital special surgery) OR (North Shore hospital) OR (Long Island Jewish) OR (memorial sloan) OR (sloan kettering) OR sloan-kettering OR hamad OR (mount sinai) OR (methodist houston) OR (National Institute of Mental Health) OR (beth israel) OR (University of Pennsylvania Medicine) OR (Merck Research) OR (New York Medical College) OR (Medicine Dentistry New Jersey) OR Montefiore OR (Lenox Hill) OR (Cold Spring Harbor) OR (St. Luke's-Roosevelt) OR (New York University Medicine) OR Langone OR (SUNY Downstate) OR (Albert Einstein Medicine) OR Yeshiva OR UMDNJ OR Icahn Medicine OR (Mount Sinai) OR (columbia medical) OR (columbia physicians))";
	private boolean isFetch = false;
	
	private static enum SearchType {
		EMAIL,
		FIRST_INITIAL_LAST_NAME,
		FIRST_NAME_LAST_NAME,
		DEPARTMENT,
		GRANTS,
		COMMON_AFFILIATIONS,
		AFFILIATIONS_IN_DB
	}
	
	/**
	 * Maximum number of articles that can be retrieved at once.
	 */
	private static final int THRESHOLD = 2000;

	public List<PubmedArticle> getPubmedArticle(String cwid) throws ParserConfigurationException, SAXException, IOException {
		List<PubmedArticle> all = new ArrayList<PubmedArticle>();
		Set<String> pmids = new HashSet<String>();

		List<PubmedArticle> regular = getPubmedArticle(ReCiterEngineProperty.pubmedFolder, cwid);
		for (PubmedArticle article : regular) {
			String pmid = article.getMedlineCitation().getPmid().getPmidString();
			if (!pmids.contains(pmid)) {
				all.add(article);
				pmids.add(pmid);
			}
		}

		List<PubmedArticle> affiliations = getPubmedArticle(ReCiterEngineProperty.affiliationsXmlFolder, cwid);
		for (PubmedArticle article : affiliations) {
			String pmid = article.getMedlineCitation().getPmid().getPmidString();
			if (!pmids.contains(pmid)) {
				all.add(article);
				pmids.add(pmid);
			}
		}

		List<PubmedArticle> commonAffiliations = getPubmedArticle(ReCiterEngineProperty.commonAffiliationsXmlFolder, cwid);
		for (PubmedArticle article : commonAffiliations) {
			String pmid = article.getMedlineCitation().getPmid().getPmidString();
			if (!pmids.contains(pmid)) {
				all.add(article);
				pmids.add(pmid);
			}
		}

		List<PubmedArticle> depts = getPubmedArticle(ReCiterEngineProperty.departmentXmlFolder, cwid);
		for (PubmedArticle article : depts) {
			String pmid = article.getMedlineCitation().getPmid().getPmidString();
			if (!pmids.contains(pmid)) {
				all.add(article);
				pmids.add(pmid);
			}
		}

		List<PubmedArticle> emails = getPubmedArticle(ReCiterEngineProperty.emailXmlFolder, cwid);
		for (PubmedArticle article : emails) {
			String pmid = article.getMedlineCitation().getPmid().getPmidString();
			if (!pmids.contains(pmid)) {
				all.add(article);
				pmids.add(pmid);
			}
		}

		List<PubmedArticle> grants = getPubmedArticle(ReCiterEngineProperty.grantXmlFolder, cwid);
		for (PubmedArticle article : grants) {
			String pmid = article.getMedlineCitation().getPmid().getPmidString();
			if (!pmids.contains(pmid)) {
				all.add(article);
				pmids.add(pmid);
			}
		}

		return all;
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
	public Set<AuthorName> getAuthorNameVariationFromEmails(TargetAuthor targetAuthor) 
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

	public void fetchUsingFirstName(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		File directory = new File(ReCiterEngineProperty.pubmedFolder + targetAuthor.getCwid());

		if (directory.exists()) {
//			slf4jLogger.info("Directory [" + ReCiterEngineProperty.pubmedFolder + "] exists for user=[" + targetAuthor.getCwid() + "]. Please delete it before re-retrieving.");
			return;
		}

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		String firstName = targetAuthor.getAuthorName().getFirstName();

		// Search for verbose name and common affiliations.
		String query = URLEncoder.encode(lastName + " " + firstInitial, "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(query);

		if (handler.getCount() > THRESHOLD) {
			String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName, "UTF-8");
			PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);

			if (handlerVerboseFirstName.getCount() > THRESHOLD) {
				// do not fetch.
				return;
			} else {
				fetch(queryVerboseFirstName, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
			}
		} else {
			fetch(query, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handler.getCount());
		}
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

	public void fetchByPmids(Set<String> pmids, String commonDirectory, String cwid) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		int i = 1;

		for (String pmid : pmids) {
			String encodedUrl = URLEncoder.encode(pmid, "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(encodedUrl);
			fetch(encodedUrl, commonDirectory, cwid, handler.getCount());
			//			if (i % 100 != 0) {
			//				sb.append(pmid + ",");
			//			} else {
			//				sb.append(pmid);
			//				i = 1;
			//				String encodedUrl = URLEncoder.encode(sb.toString(), "UTF-8");
			//				PubmedESearchHandler handler = getPubmedESearchHandler(encodedUrl);
			//				fetch(encodedUrl, commonDirectory, cwid, handler.getCount());
			//				sb = new StringBuffer();
			//			}
		}
	}

	/**
	 * Returns true if fetch by last name and first initial. False otherwise.
	 * @param targetAuthor
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public boolean fetchRegular(TargetAuthor targetAuthor) throws ParserConfigurationException, SAXException, IOException {
		File directory = new File(ReCiterEngineProperty.pubmedFolder + targetAuthor.getCwid());

		if (directory.exists()) {
			slf4jLogger.info("Directory [" + ReCiterEngineProperty.pubmedFolder + "] exists for user=[" + targetAuthor.getCwid() + "]. Please delete it before re-retrieving.");
			return true;
		}

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstName = targetAuthor.getAuthorName().getFirstName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();

		String encodedUrl = URLEncoder.encode(lastName + " " + firstInitial + "[au]", "UTF-8");
		int count = retrieveNumberOfSearchResult(encodedUrl);
		slf4jLogger.info("Number of articles need to be retrieved for : " + targetAuthor.getCwid() + " is "+ count);

		Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
		if (!nameVariations.isEmpty()) {
			for (AuthorName authorName : nameVariations) {
				String anotherFirstName = authorName.getFirstName();
				String anotherFirstInitial = authorName.getFirstInitial();
				if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

					slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

					// Search again using name variations for verbose name and common affiliations.
					String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + "[au]", "UTF-8");
					PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

					if (anotherHandler.getCount() > THRESHOLD) {
						String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + "[au]", "UTF-8");
						PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
						// fetch only if less than threshold.
						if (handlerVerboseFirstName.getCount() <= THRESHOLD) {
							fetch(queryVerboseFirstName, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
						}
					} else {
						fetch(anotherQuery, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), anotherHandler.getCount());
					}
				}
			}
		}

		if (count > THRESHOLD) {
			return false;
		}
		fetch(encodedUrl, ReCiterEngineProperty.pubmedFolder, targetAuthor.getCwid(), count);
		return true;
	}

	private PubmedESearchHandler getPubmedESearchHandler(String query) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery(query);
		String fullUrl = pubmedXmlQuery.buildESearchQuery(); // build eSearch query.
		slf4jLogger.info("URL=[" + fullUrl + "]");
		PubmedESearchHandler pubmedESearchHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(fullUrl).openStream();
		SAXParserFactory.newInstance().newSAXParser().parse(esearchStream, pubmedESearchHandler);
		return pubmedESearchHandler;
	}

	private PubmedESearchHandler getPubmedESearchHandlerJson(String query) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
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
	 * Returns the number of articles that needs to be retrieved.
	 * @param query
	 * @return
	 */
	public int retrieveNumberOfSearchResult(String query) {
		int numPubMedArticles = 0;

		// Get the count (number of publications for this query).
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(query);

		// set retmax = 1 so that query can be executed fast.
		pubmedXmlQuery.setRetMax(1);

		String eSearchUrl = pubmedXmlQuery.buildESearchQuery();

		PubmedESearchHandler xmlHandler = PubmedESearchHandler.executeESearchQuery(eSearchUrl);
		numPubMedArticles = xmlHandler.getCount();

		slf4jLogger.info("PubMed Seach Query Url: " + eSearchUrl);
		return numPubMedArticles;
	}

	public void fetchByEmail(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		File directory = new File(ReCiterEngineProperty.emailXmlFolder + targetAuthor.getCwid());

		if (directory.exists()) {
			slf4jLogger.info("Directory [" + ReCiterEngineProperty.emailXmlFolder + "] exists for user=[" + targetAuthor.getCwid() + "]. Please delete it before re-retrieving.");
			return;
		}

		List<String> emailAddresses = targetAuthor.getEmailAddresses();
		String email = targetAuthor.getEmail();
		String emailOther = targetAuthor.getEmailOther();
		Set<String> distinctEmailAddresses = new HashSet<String>();
		distinctEmailAddresses.addAll(emailAddresses);

		if (email != null) {
			distinctEmailAddresses.add(email);
		}

		if (emailOther != null) {
			distinctEmailAddresses.add(emailOther);
		}

		slf4jLogger.info("rc_identity_email: " + distinctEmailAddresses);

		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (String s : distinctEmailAddresses) {
			if (i != distinctEmailAddresses.size() - 1) {
				sb.append(s + " OR ");
			} else {
				sb.append(s);
			}
		}

		String encodedUrl = URLEncoder.encode(sb.toString(), "UTF-8");
		int count = retrieveNumberOfSearchResult(encodedUrl);
		slf4jLogger.info("Number of articles need to be retrieved for : " + targetAuthor.getCwid() + " is "+ count);

		fetch(encodedUrl, ReCiterEngineProperty.emailXmlFolder, targetAuthor.getCwid(), count);
	}

	public Set<String> fetchPmidsByEmail(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		Set<String> pmids = new HashSet<String>();

		// Construct email query.
		List<String> emailAddresses = targetAuthor.getEmailAddresses();
		String email = targetAuthor.getEmail();
		String emailOther = targetAuthor.getEmailOther();
		Set<String> distinctEmailAddresses = new HashSet<String>();
		distinctEmailAddresses.addAll(emailAddresses);

		if (email != null) {
			distinctEmailAddresses.add(email);
		}

		if (emailOther != null) {
			distinctEmailAddresses.add(emailOther);
		}

		slf4jLogger.info("rc_identity_email: " + distinctEmailAddresses);

		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (String s : distinctEmailAddresses) {
			if (i != distinctEmailAddresses.size() - 1) {
				sb.append(s + " OR ");
			} else {
				sb.append(s);
			}
		}

		// Fetch pmids only.
		String encodedUrl = URLEncoder.encode(sb.toString(), "UTF-8");
		pmids = getPmids(encodedUrl);

		return pmids;
	}

	/**
	 * Fetch PubMed articles by searching for verbose name and common affiliations.
	 * 
	 * @param targetAuthor
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public void fetchByCommonAffiliations(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		File directory = new File(ReCiterEngineProperty.commonAffiliationsXmlFolder + targetAuthor.getCwid());

		if (directory.exists()) {
			slf4jLogger.info("Directory [" + ReCiterEngineProperty.commonAffiliationsXmlFolder + "] exists for user=[" + targetAuthor.getCwid() + "]. Please delete it before re-retrieving.");
			return;
		}

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		String firstName = targetAuthor.getAuthorName().getFirstName();

		// Search for verbose name and common affiliations.
		String query = URLEncoder.encode(lastName + " " + firstInitial + " " + AFFILIATION_QUERY, "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(query);

		if (handler.getCount() > THRESHOLD) {
			String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " " + AFFILIATION_QUERY, "UTF-8");
			PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
			fetch(queryVerboseFirstName, ReCiterEngineProperty.commonAffiliationsXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
		} else {
			fetch(query, ReCiterEngineProperty.commonAffiliationsXmlFolder, targetAuthor.getCwid(), handler.getCount());
		}

		Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
		if (!nameVariations.isEmpty()) {
			for (AuthorName authorName : nameVariations) {
				String anotherFirstName = authorName.getFirstName();
				String anotherFirstInitial = authorName.getFirstInitial();
				if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

					slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

					// Search again using name variations for verbose name and common affiliations.
					String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + " " + AFFILIATION_QUERY, "UTF-8");
					PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

					if (anotherHandler.getCount() > THRESHOLD) {
						String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + " " + AFFILIATION_QUERY, "UTF-8");
						PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
						fetch(queryVerboseFirstName, ReCiterEngineProperty.commonAffiliationsXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
					} else {
						fetch(anotherQuery, ReCiterEngineProperty.commonAffiliationsXmlFolder, targetAuthor.getCwid(), anotherHandler.getCount());
					}
				}
			}
		}
	}

	private Set<String> getPmids(String encodedUrl) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		Set<String> pmids = new HashSet<String>();
		// Fetch pmids only.
		// String encodedUrl = URLEncoder.encode(query, "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandlerJson(encodedUrl);
		String webEnv = handler.getWebEnv();

		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setRetMode("json");
		pubmedXmlQuery.setWevEnv(webEnv);

		String url = pubmedXmlQuery.buildEFetchQuery();

		// Save Pmids.
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));

			String inputLine;
			while ((inputLine = bufferedReader.readLine()) != null) {
				pmids.add(inputLine);
			}

			bufferedReader.close();
		} catch (IOException e) {
			slf4jLogger.warn(e.getMessage());
		}

		return pmids;
	}

	public Set<String> fetchPmidsByCommonAffiliations(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		Set<String> pmids = new HashSet<String>();

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		String firstName = targetAuthor.getAuthorName().getFirstName();

		// Search for verbose name and common affiliations.
		String query = URLEncoder.encode(lastName + " " + firstInitial + " " + AFFILIATION_QUERY, "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(query);

		if (handler.getCount() > THRESHOLD) {
			String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " " + AFFILIATION_QUERY, "UTF-8");
			pmids.addAll(getPmids(queryVerboseFirstName));
		} else {
			pmids.addAll(getPmids(query));
		}

		Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
		if (!nameVariations.isEmpty()) {
			for (AuthorName authorName : nameVariations) {
				String anotherFirstName = authorName.getFirstName();
				String anotherFirstInitial = authorName.getFirstInitial();
				if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

					slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

					// Search again using name variations for verbose name and common affiliations.
					String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + " " + AFFILIATION_QUERY, "UTF-8");
					PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

					if (anotherHandler.getCount() > THRESHOLD) {
						String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + " " + AFFILIATION_QUERY, "UTF-8");
						pmids.addAll(getPmids(queryVerboseFirstName));
					} else {
						pmids.addAll(getPmids(anotherQuery));
					}
				}
			}
		}

		return pmids;
	}

	/**
	 * Fetch PubMed articles by searching for department.
	 * 
	 * @param targetAuthor
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void fetchByDepartment(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		File directory = new File(ReCiterEngineProperty.departmentXmlFolder + targetAuthor.getCwid());

		if (directory.exists()) {
			slf4jLogger.info("Directory [" + ReCiterEngineProperty.commonAffiliationsXmlFolder + "] exists for user=[" + targetAuthor.getCwid() + "]. Please delete it before re-retrieving.");
			return;
		}

		String department = targetAuthor.getDepartment();

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		String firstName = targetAuthor.getAuthorName().getFirstName();

		String query = URLEncoder.encode(lastName + " " + firstInitial + " AND " + department, "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(query);

		if (handler.getCount() > THRESHOLD) {
			String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " AND " + department, "UTF-8");
			PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
			fetch(queryVerboseFirstName, ReCiterEngineProperty.departmentXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
		} else {
			fetch(query, ReCiterEngineProperty.departmentXmlFolder, targetAuthor.getCwid(), handler.getCount());
		}

		Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
		if (!nameVariations.isEmpty()) {
			for (AuthorName authorName : nameVariations) {
				String anotherFirstName = authorName.getFirstName();
				String anotherFirstInitial = authorName.getFirstInitial();
				if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

					slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

					String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + " AND " + department, "UTF-8");
					PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

					if (anotherHandler.getCount() > THRESHOLD) {
						String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + " AND " + department, "UTF-8");
						PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
						fetch(queryVerboseFirstName, ReCiterEngineProperty.departmentXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
					} else {
						fetch(anotherQuery, ReCiterEngineProperty.departmentXmlFolder, targetAuthor.getCwid(), anotherHandler.getCount());
					}
				}
			}
		}
	}

	public Set<String> fetchPmidsByDepartment(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		Set<String> pmids = new HashSet<String>();

		String department = targetAuthor.getDepartment();

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		String firstName = targetAuthor.getAuthorName().getFirstName();

		String query = URLEncoder.encode(lastName + " " + firstInitial + " AND " + department, "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(query);

		if (handler.getCount() > THRESHOLD) {
			String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " AND " + department, "UTF-8");
			pmids.addAll(getPmids(queryVerboseFirstName));
		} else {
			pmids.addAll(getPmids(query));
		}

		Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
		if (!nameVariations.isEmpty()) {
			for (AuthorName authorName : nameVariations) {
				String anotherFirstName = authorName.getFirstName();
				String anotherFirstInitial = authorName.getFirstInitial();
				if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

					slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

					String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + " AND " + department, "UTF-8");
					PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

					if (anotherHandler.getCount() > THRESHOLD) {
						String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + " AND " + department, "UTF-8");
						pmids.addAll(getPmids(queryVerboseFirstName));
					} else {
						pmids.addAll(getPmids(anotherQuery));
					}
				}
			}
		}

		return pmids;
	}

	/**
	 * Search for name of affiliations as recorded in database table 'rc_identity_institution'.
	 * 
	 * For example, this returns 106 results: "Wang Yi[Author] and Fudan University(China)"
	 * 
	 * @param targetAuthor
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void fetchByAffiliationInDb(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		File directory = new File(ReCiterEngineProperty.affiliationsXmlFolder + targetAuthor.getCwid());

		if (directory.exists()) {
			slf4jLogger.info("Directory [" + ReCiterEngineProperty.commonAffiliationsXmlFolder + "] exists for user=[" + targetAuthor.getCwid() + "]. Please delete it before re-retrieving.");
			return;
		}

		List<String> affiliations = targetAuthor.getInstitutions();
		if (!affiliations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			// Concantenate the string with 'OR'.
			// ex: Wang Yi[Author] and (Fudan University (China) OR University of Wisconsin, Madison OR University of Wisconsin, Milwaukee).
			for (String affiliation : affiliations) {
				if (i != affiliations.size() - 1) {
					sb.append(affiliation + " OR ");
				} else {
					sb.append(affiliation);
				}
				i++;
			}

			slf4jLogger.info("Institutions=[" + sb.toString() + "].");
			String lastName = targetAuthor.getAuthorName().getLastName();
			String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
			String firstName = targetAuthor.getAuthorName().getFirstName();

			String query = URLEncoder.encode(lastName + " " + firstInitial + "[Author] AND (" + sb.toString() + ")", "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(query);

			if (handler.getCount() > THRESHOLD) {
				String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + "[Author] AND (" + sb.toString() + ")", "UTF-8");
				PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
				fetch(queryVerboseFirstName, ReCiterEngineProperty.affiliationsXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
			} else {
				fetch(query, ReCiterEngineProperty.affiliationsXmlFolder, targetAuthor.getCwid(), handler.getCount());
			}

			Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
			if (!nameVariations.isEmpty()) {
				for (AuthorName authorName : nameVariations) {

					String anotherFirstName = authorName.getFirstName();
					String anotherFirstInitial = authorName.getFirstInitial();
					if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

						slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

						String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + "[Author] AND (" + sb.toString() + ")", "UTF-8");
						PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

						if (anotherHandler.getCount() > THRESHOLD) {
							String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + "[Author] AND (" + sb.toString() + ")", "UTF-8");
							PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
							fetch(queryVerboseFirstName, ReCiterEngineProperty.affiliationsXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
						} else {
							fetch(anotherQuery, ReCiterEngineProperty.affiliationsXmlFolder, targetAuthor.getCwid(), anotherHandler.getCount());
						}
					}
				}
			}
		}
	}

	public Set<String> fetchPmidsByAffiliationInDb(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		Set<String> pmids = new HashSet<String>();

		List<String> affiliations = targetAuthor.getInstitutions();
		if (!affiliations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			// Concantenate the string with 'OR'.
			// ex: Wang Yi[Author] and (Fudan University (China) OR University of Wisconsin, Madison OR University of Wisconsin, Milwaukee).
			for (String affiliation : affiliations) {
				if (i != affiliations.size() - 1) {
					sb.append(affiliation + " OR ");
				} else {
					sb.append(affiliation);
				}
				i++;
			}

			slf4jLogger.info("Institutions=[" + sb.toString() + "].");
			String lastName = targetAuthor.getAuthorName().getLastName();
			String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
			String firstName = targetAuthor.getAuthorName().getFirstName();

			String query = URLEncoder.encode(lastName + " " + firstInitial + "[Author] AND (" + sb.toString() + ")", "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(query);

			if (handler.getCount() > THRESHOLD) {
				String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + "[Author] AND (" + sb.toString() + ")", "UTF-8");
				pmids.addAll(getPmids(queryVerboseFirstName));
			} else {
				pmids.addAll(getPmids(query));
			}

			Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
			if (!nameVariations.isEmpty()) {
				for (AuthorName authorName : nameVariations) {

					String anotherFirstName = authorName.getFirstName();
					String anotherFirstInitial = authorName.getFirstInitial();
					if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

						slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

						String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + "[Author] AND (" + sb.toString() + ")", "UTF-8");
						PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

						if (anotherHandler.getCount() > THRESHOLD) {
							String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + "[Author] AND (" + sb.toString() + ")", "UTF-8");
							pmids.addAll(getPmids(queryVerboseFirstName));
						} else {
							pmids.addAll(getPmids(anotherQuery));
						}
					}
				}
			}
		}

		return pmids;
	}

	/**
	 * Function to parse sponsortAwardId.
	 * @param sponsorAwardId
	 * @return
	 */
	private String parseSponsorAwardId(String sponsorAwardId) {
		int lastIndexOfSpace = sponsorAwardId.lastIndexOf(" ");
		if (lastIndexOfSpace != -1) {
			String temp = sponsorAwardId.substring(lastIndexOfSpace + 1, sponsorAwardId.length());
			final Pattern pattern = Pattern.compile("([^-]*)-");
			final Matcher matcher = pattern.matcher(temp);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return "";
	}

	/**
	 * Fetch PubMed articles by sponsor award ids.
	 * @param targetAuthor
	 * @return
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public void fetchByGrants(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		File directory = new File(ReCiterEngineProperty.grantXmlFolder + targetAuthor.getCwid());

		if (directory.exists()) {
			slf4jLogger.info("Directory [" + ReCiterEngineProperty.commonAffiliationsXmlFolder + "] exists for user=[" + targetAuthor.getCwid() + "]. Please delete it before re-retrieving.");
			return;
		}

		List<String> sponsorAwardIds = targetAuthor.getSponsorAwardIds();

		if (sponsorAwardIds.size() > 0) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (String sponsorAwardId : sponsorAwardIds) {
				String parsed = parseSponsorAwardId(sponsorAwardId);
				if (i != sponsorAwardIds.size() - 1) {
					sb.append(parsed + "[Grant Number] OR ");
				} else {
					sb.append(parsed + "[Grant Number]");
				}
			}

			String lastName = targetAuthor.getAuthorName().getLastName();
			String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
			String firstName = targetAuthor.getAuthorName().getFirstName();

			// Search for verbose name and common affiliations.
			String query = URLEncoder.encode("(" + lastName + " " + firstInitial + " AND (" + sb.toString() + ")", "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(query);

			if (handler.getCount() > THRESHOLD) {
				String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " AND (" + sb.toString() + ")", "UTF-8");
				PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
				fetch(queryVerboseFirstName, ReCiterEngineProperty.grantXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
			} else {
				fetch(query, ReCiterEngineProperty.grantXmlFolder, targetAuthor.getCwid(), handler.getCount());
			}

			Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
			if (!nameVariations.isEmpty()) {
				for (AuthorName authorName : nameVariations) {
					String anotherFirstName = authorName.getFirstName();
					String anotherFirstInitial = authorName.getFirstInitial();
					if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

						slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

						String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + " AND (" + sb.toString() + ")", "UTF-8");
						PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

						if (anotherHandler.getCount() > THRESHOLD) {
							String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + " AND (" + sb.toString() + ")", "UTF-8");
							PubmedESearchHandler handlerVerboseFirstName = getPubmedESearchHandler(queryVerboseFirstName);
							fetch(queryVerboseFirstName, ReCiterEngineProperty.grantXmlFolder, targetAuthor.getCwid(), handlerVerboseFirstName.getCount());
						} else {
							fetch(anotherQuery, ReCiterEngineProperty.grantXmlFolder, targetAuthor.getCwid(), anotherHandler.getCount());
						}
					}
				}
			}
		}
	}

	public Set<String> fetchPmidsByGrants(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {

		Set<String> pmids = new HashSet<String>();

		List<String> sponsorAwardIds = targetAuthor.getSponsorAwardIds();

		if (sponsorAwardIds.size() > 0) {
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (String sponsorAwardId : sponsorAwardIds) {
				String parsed = parseSponsorAwardId(sponsorAwardId);
				if (i != sponsorAwardIds.size() - 1) {
					sb.append(parsed + "[Grant Number] OR ");
				} else {
					sb.append(parsed + "[Grant Number]");
				}
			}

			String lastName = targetAuthor.getAuthorName().getLastName();
			String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
			String firstName = targetAuthor.getAuthorName().getFirstName();

			// Search for verbose name and common affiliations.
			String query = URLEncoder.encode("(" + lastName + " " + firstInitial + " AND (" + sb.toString() + ")", "UTF-8");
			PubmedESearchHandler handler = getPubmedESearchHandler(query);

			if (handler.getCount() > THRESHOLD) {
				String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " AND (" + sb.toString() + ")", "UTF-8");
				pmids.addAll(getPmids(queryVerboseFirstName));
			} else {
				pmids.addAll(getPmids(query));
			}

			Set<AuthorName> nameVariations = getAuthorNameVariationFromEmails(targetAuthor);
			if (!nameVariations.isEmpty()) {
				for (AuthorName authorName : nameVariations) {
					String anotherFirstName = authorName.getFirstName();
					String anotherFirstInitial = authorName.getFirstInitial();
					if (!StringUtils.equalsIgnoreCase(anotherFirstInitial, firstInitial) && !StringUtils.equalsIgnoreCase(anotherFirstName, firstName)) {

						slf4jLogger.info("Fetch using name variation=[" + authorName + "]");

						String anotherQuery = URLEncoder.encode(lastName + " " + anotherFirstInitial + " AND (" + sb.toString() + ")", "UTF-8");
						PubmedESearchHandler anotherHandler = getPubmedESearchHandler(anotherQuery);

						if (anotherHandler.getCount() > THRESHOLD) {
							String queryVerboseFirstName = URLEncoder.encode(lastName + " " + anotherFirstName + " AND (" + sb.toString() + ")", "UTF-8");
							pmids.addAll(getPmids(queryVerboseFirstName));
						} else {
							pmids.addAll(getPmids(anotherQuery));
						}
					}
				}
			}
		}

		return pmids;
	}
}
