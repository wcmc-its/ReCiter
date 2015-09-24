package reciter.junit.testcases;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterExample;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterArticleKeywords.Keyword;
import reciter.utils.stemmer.PorterStemmer;
import reciter.utils.stemmer.SnowballStemmer;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;

public class StemmerImplementationJuitTest {
	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);
	static String cwid = "aad2004";
	 ReCiterConfigProperty reCiterConfigProperty;
	 String lastName;
	 String middleName;
	 String firstName;
	 String affiliation;
	 String firstInitial;
	 String authorKeywords;
	 String coAuthors;
	 double similarityThreshold;
	 String department;
	 PubmedXmlFetcher pubmedXmlFetcher;
	 List<PubmedArticle> pubmedArticleList;
	List<ReCiterArticle> reCiterArticleList;
	List<String> gspPmidList;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		String path = (new File("").getAbsolutePath())+File.separator+ReCiterConfigProperty
				.getDefaultLocation();
		ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
		try {
			reCiterConfigProperty
					.loadProperty(path
							+ cwid
							+ "/"
							+ cwid
							+ ".properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
		lastName = reCiterConfigProperty.getLastName();
		middleName = reCiterConfigProperty.getMiddleName();
		firstName = reCiterConfigProperty.getFirstName();
		affiliation = reCiterConfigProperty.getAuthorAffiliation();
		firstInitial = firstName.substring(0, 1);
		cwid = reCiterConfigProperty.getCwid();
		authorKeywords = reCiterConfigProperty.getAuthorKeywords();
		coAuthors = reCiterConfigProperty.getCoAuthors();
		similarityThreshold = reCiterConfigProperty.getSimilarityThreshold();
		department = reCiterConfigProperty.getAuthorDepartment();
		pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName,
				firstInitial, middleName, cwid);
		ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
		reCiterArticleList = new ArrayList<ReCiterArticle>();

		GoldStandardPmidsDao gspDao = new GoldStandardPmidsDaoImpl();
		gspPmidList = gspDao.getPmidsByCwid(cwid);

		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid()
					.getPmidString();
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid,
					pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle,
					scopusArticle));
		}
	}

	@Test
	public void test() {
		for (ReCiterArticle article : reCiterArticleList) {
			{
				List<String> articleKeywordList = Arrays.asList(article
						.getArticleTitle().split(" "));
				List<String> journalKeywordList = Arrays.asList(article.getJournal().getJournalTitle().split(" "));
				for (String articleKeyword : articleKeywordList) {
					
				String stemmedArticlekeyword = stemWord(articleKeyword);
				slf4jLogger.info("Article Title before stemming =   " + articleKeyword);
				slf4jLogger.info("After stemming =     " + stemmedArticlekeyword);
				}
				slf4jLogger.info("-- Finished stemming for article Title List --");
				for (String journalKeyword : journalKeywordList) {
					String stemmedJournalKeyword = stemWord(journalKeyword);
					slf4jLogger.info("Journal keyword before stemming =   " + journalKeyword);
					slf4jLogger.info("After stemming =     " + stemmedJournalKeyword);
				}
				slf4jLogger.info("-- Finished stemming for journal Keyword List --");
				ReCiterArticleKeywords articleKeywords = article.getArticleKeywords();
				for(Keyword keyword: articleKeywords.getKeywords()){
					String origKeyword = keyword.getKeyword();
					String stemmedKeyword = stemWord(origKeyword);
					slf4jLogger.info("Article keyword before stemming =   " + origKeyword);
					slf4jLogger.info("After stemming =     " + stemmedKeyword);
				}
				slf4jLogger.info("-- Finished stemming for articleKeywordList --");
			}
			

		}

	}
	private String stemWord(String word){
		SnowballStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
