package reciter.xml.parser.pubmed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
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
import reciter.model.article.ReCiterArticleAuthors;
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

	/**
	 * Maximum number of articles that can be retrieved at once.
	 */
	private static final int THRESHOLD = 2000;

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
	 * @param cwid
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public Set<AuthorName> getAuthorNameVariationFromEmails(TargetAuthor targetAuthor, String cwid) 
			throws ParserConfigurationException, SAXException, IOException {
		Set<AuthorName> nameVariations = new HashSet<AuthorName>();
		List<PubmedArticle> pubmedArticles = getPubmedArticle(ReCiterEngineProperty.emailXmlFolder, cwid);
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
	 * Fetch PubMed xmls using <code>pubmedQuery</code> and store the xmls in <code>commonDirectory</code> specified
	 * by <code>cwid</code>
	 * @param pubmedQuery
	 * @param commonDirectory
	 * @param cwid
	 * @param numberOfPubmedArticles
	 */
	public void fetch(String pubmedQuery, String commonDirectory, String cwid, int numberOfPubmedArticles) {
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
			saveXml(eFetchUrl, commonDirectory, cwid, cwid + "_" + i);

			// Update the retstart value.
			currentRetStart += pubmedXmlQuery.getRetMax();
			pubmedXmlQuery.setRetStart(currentRetStart);
		}
	}

	private PubmedESearchHandler getPubmedESearchHandler(String query) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery(query);
		String fullUrl = pubmedXmlQuery.buildESearchQuery(); // build eSearch query.

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

	public void retrieveByEmail(TargetAuthor targetAuthor) throws MalformedURLException, IOException, SAXException, ParserConfigurationException {
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

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		String firstName = targetAuthor.getAuthorName().getFirstName();

		// Search for verbose name and common affiliations.
		String query = URLEncoder.encode(lastName + " " + firstInitial + " AND ((new york) OR 10065 OR 10021 OR weill OR cornell OR (newyork AND presbyterian) OR (new york AND presbyterian) OR HSS OR (hospital special surgery) OR (North Shore hospital) OR (Long Island Jewish) OR (memorial sloan) OR (sloan kettering) OR sloan-kettering OR hamad OR (mount sinai) OR (methodist houston) OR (National Institute of Mental Health) OR (beth israel) OR (University of Pennsylvania Medicine) OR (Merck Research) OR (New York Medical College) OR (Medicine Dentistry New Jersey) OR Montefiore OR (Lenox Hill) OR (Cold Spring Harbor) OR (St. Luke's-Roosevelt) OR (New York University Medicine) OR Langone OR (SUNY Downstate) OR (Albert Einstein Medicine) OR Yeshiva OR UMDNJ OR Icahn Medicine OR (Mount Sinai) OR (columbia medical) OR (columbia physicians))", "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(query);

		PubmedXmlFetcher xmlFetcher = new PubmedXmlFetcher();

		if (handler.getCount() > THRESHOLD) {
			String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " AND ((new york) OR 10065 OR 10021 OR weill OR cornell OR (newyork AND presbyterian) OR (new york AND presbyterian) OR HSS OR (hospital special surgery) OR (North Shore hospital) OR (Long Island Jewish) OR (memorial sloan) OR (sloan kettering) OR sloan-kettering OR hamad OR (mount sinai) OR (methodist houston) OR (National Institute of Mental Health) OR (beth israel) OR (University of Pennsylvania Medicine) OR (Merck Research) OR (New York Medical College) OR (Medicine Dentistry New Jersey) OR Montefiore OR (Lenox Hill) OR (Cold Spring Harbor) OR (St. Luke's-Roosevelt) OR (New York University Medicine) OR Langone OR (SUNY Downstate) OR (Albert Einstein Medicine) OR Yeshiva OR UMDNJ OR Icahn Medicine OR (Mount Sinai) OR (columbia medical) OR (columbia physicians))", "UTF-8");
			fetch(queryVerboseFirstName, ReCiterEngineProperty.commonAffiliationsXmlFolder, targetAuthor.getCwid(), handler.getCount());
		} else {
			fetch(query, ReCiterEngineProperty.commonAffiliationsXmlFolder, targetAuthor.getCwid(), handler.getCount());
		}
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
		String department = targetAuthor.getDepartment();

		String lastName = targetAuthor.getAuthorName().getLastName();
		String firstInitial = targetAuthor.getAuthorName().getFirstInitial();
		String firstName = targetAuthor.getAuthorName().getFirstName();

		String query = URLEncoder.encode(lastName + " " + firstInitial + " AND " + department, "UTF-8");
		PubmedESearchHandler handler = getPubmedESearchHandler(query);

		PubmedXmlFetcher xmlFetcher = new PubmedXmlFetcher();

		if (handler.getCount() > THRESHOLD) {
			String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " AND " + department, "UTF-8");
			fetch(queryVerboseFirstName, ReCiterEngineProperty.departmentXmlFolder, targetAuthor.getCwid(), handler.getCount());
		} else {
			fetch(query, ReCiterEngineProperty.departmentXmlFolder, targetAuthor.getCwid(), handler.getCount());
		}
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

			PubmedXmlFetcher xmlFetcher = new PubmedXmlFetcher();

			if (handler.getCount() > THRESHOLD) {
				String queryVerboseFirstName = URLEncoder.encode(lastName + " " + firstName + " AND (" + sb.toString() + ")", "UTF-8");
				fetch(queryVerboseFirstName, ReCiterEngineProperty.grantXmlFolder, targetAuthor.getCwid(), handler.getCount());
			} else {
				fetch(query, ReCiterEngineProperty.grantXmlFolder, targetAuthor.getCwid(), handler.getCount());
			}
		}
	}
}
