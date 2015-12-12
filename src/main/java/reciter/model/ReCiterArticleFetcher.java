package reciter.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.MedlineCitationArticleAuthor;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;

public class ReCiterArticleFetcher {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterArticleFetcher.class);
	private static final String PROPERTIES_FILE_LOCATION = "src/main/resources/config/reciter.properties";

	private String pubmedEmailXmlFolder;
	private String pubmedXmlFolder;
	private String pubmedAffiliationsXmlFolder;
	
	private String scopusEmailXmlFolder;
	private String scopusXmlFolder;
	private String commonAffiliationsQuery; // common_affiliations_query
	
	public ReCiterArticleFetcher() {
		Properties p = new Properties();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(PROPERTIES_FILE_LOCATION);
			p.load(inputStream);
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage(), e);
		}
		pubmedEmailXmlFolder = p.getProperty("pubmed_email_xml_folder");
		pubmedXmlFolder = p.getProperty("pubmed_xml_folder");
		scopusEmailXmlFolder = p.getProperty("scopus_email_xml_folder");
		scopusXmlFolder = p.getProperty("scopus_xml_folder");
		commonAffiliationsQuery = p.getProperty("common_affiliations_query");
	}
	
	public List<ReCiterArticle> fetch(TargetAuthor targetAuthor) {
		String query = targetAuthor.getPubmedSearchQuery();
		String cwid = targetAuthor.getCwid();
		
		// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(query, cwid);
		
		// Retrieve all the scopus xml files if not exists.
		ScopusXmlFetcher scopusFetcher = new ScopusXmlFetcher();
		scopusFetcher.fetch(query, cwid);

		// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();

		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
		}
		return reCiterArticleList;
	}
	
	public List<ReCiterArticle> fetchRevised(TargetAuthor targetAuthor) {
		String cwid = targetAuthor.getCwid();

		// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();

		// Retrieve articles by target author's email address.
		String targetAuthorEmailAddress = targetAuthor.getEmail();
		if (targetAuthorEmailAddress == null) {
			targetAuthorEmailAddress = cwid + "@med.cornell.edu";
		}
		
		PubmedXmlFetcher pubmedXmlFetcherEmail = new PubmedXmlFetcher(pubmedEmailXmlFolder);
		List<PubmedArticle> pubmedArticles = pubmedXmlFetcherEmail.getPubmedArticle(targetAuthorEmailAddress, cwid);
		
		List<AuthorName> authorNamesFromEmailFetch = new ArrayList<AuthorName>();
		for (PubmedArticle pubmedArticle : pubmedArticles) {
			for (MedlineCitationArticleAuthor author : pubmedArticle.getMedlineCitation().getArticle().getAuthorList()) {
				AuthorName authorName = new AuthorName(author.getForeName(), "", author.getLastName());
				authorNamesFromEmailFetch.add(authorName);
			}
		}
		
		targetAuthor.setAuthorNamesFromEmailFetch(authorNamesFromEmailFetch);
		
		// Retrieve all the scopus xml files if not exists.
		ScopusXmlFetcher scopusFetcherEmail = new ScopusXmlFetcher(scopusEmailXmlFolder);
		scopusFetcherEmail.fetch(targetAuthorEmailAddress, cwid, pubmedEmailXmlFolder, scopusEmailXmlFolder);

		for (PubmedArticle pubmedArticle : pubmedArticles) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher(scopusEmailXmlFolder);
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
		}

		// Construct all the different possible name variants.
	    Set<String> nameVariants = new HashSet<String>();

	    nameVariants.add(targetAuthor.getAuthorName().pubmedFormat());

	    List<AuthorName> aliasList = targetAuthor.getAliasList();
	    for (AuthorName authorName : aliasList) {
	      nameVariants.add(authorName.pubmedFormat());
	    }

	    for (PubmedArticle pubmedArticle : pubmedArticles) {
	      for (MedlineCitationArticleAuthor author : pubmedArticle.getMedlineCitation().getArticle().getAuthorList())
	        if (StringUtils.equalsIgnoreCase(targetAuthor.getAuthorName().getLastName(), 
	            author.getLastName())) {

	          String foreName = author.getForeName();
	          String lastName = author.getLastName();
	          nameVariants.add(lastName + " " + foreName.substring(0, 1));
	        }
	    }

	    String pubmedQuery = "";
	    for (String nameVariant : nameVariants) {
	      pubmedQuery += nameVariant + "%20OR%20";
	    }

	    if (pubmedQuery.length() > 0) {
	      pubmedQuery = pubmedQuery.substring(0, pubmedQuery.length() - "%20OR%20".length());
	    }

	    pubmedQuery = pubmedQuery.replaceAll("\\s+", "%20");

	    // Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
	    PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher(pubmedXmlFolder);
	    int count = pubmedXmlFetcher.fetchArticleCount(pubmedQuery);
	    
	    if (count > 2000) {
	    	String lastAndFirstName = targetAuthor.getAuthorName().getLastName() + " " + targetAuthor.getAuthorName().getFirstInitial();
	    	String verboseNameAndCommonAffiliations = lastAndFirstName + " " + commonAffiliationsQuery;
	    	
	    	String departmentQuery = "";
	    	if (targetAuthor.getDepartment() != null && targetAuthor.getDepartment().length() > 0) {
	    		departmentQuery += targetAuthor.getDepartment() + " " + targetAuthor.getAuthorName().getLastName() + " "
	    				+ targetAuthor.getAuthorName().getFirstInitial();
	    	}
	    	
	    	List<String> affiliationQueries = new ArrayList<String>();
	    	if (targetAuthor.getAffiliation() != null && targetAuthor.getAffiliation().getAffiliationName() != null &&
	    			targetAuthor.getAffiliation().getAffiliationName().length() > 0) {
	    		
	    	}
	    } else {
	    	
	    }
	    List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(pubmedQuery, cwid);

	    // Retrieve all the scopus xml files if not exists.
	    ScopusXmlFetcher scopusFetcher = new ScopusXmlFetcher(scopusXmlFolder);
	    scopusFetcher.fetch(pubmedQuery, cwid);

	    for (PubmedArticle pubmedArticle : pubmedArticleList) {
	      String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
	      ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher(scopusXmlFolder);
	      ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
	      reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
	    }
	    
		return reCiterArticleList;
	}



	public int checkNumQueries(TargetAuthor targetAuthor) {
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		String query = targetAuthor.getPubmedSearchQuery();
		return pubmedXmlFetcher.fetchArticleCount(query);
	}
}
