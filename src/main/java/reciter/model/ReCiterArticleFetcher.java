package reciter.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;
import reciter.xml.parser.pubmed.PubmedXmlFetcher;
import reciter.xml.parser.pubmed.PubmedXmlQuery;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
import reciter.xml.parser.pubmed.model.MedlineCitationArticleAuthor;
import reciter.xml.parser.pubmed.model.PubmedArticle;
import reciter.xml.parser.scopus.ScopusXmlFetcher;
import reciter.xml.parser.scopus.model.ScopusArticle;
import reciter.xml.parser.translator.ArticleTranslator;

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
//		String query = targetAuthor.getPubmedSearchQuery();
//		String cwid = targetAuthor.getCwid();
//
//		// Retrieve the PubMed articles for this cwid if the articles have not been retrieved yet. 
//		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
//		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(query, cwid);
//
//		// Retrieve all the scopus xml files if not exists.
//		ScopusXmlFetcher scopusFetcher = new ScopusXmlFetcher();
//		scopusFetcher.fetch(query, cwid);
//
//		// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
//		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
//
//		for (PubmedArticle pubmedArticle : pubmedArticleList) {
//			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
//			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
//			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
//			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
//		}
//		return reCiterArticleList;
		return null;
	}
}
