package reciter.junit.testcases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterExample;
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
import database.dao.impl.IdentityDaoImpl;
import database.model.Identity;

// Issue # 17

public class StemmerImplementationJuitTest {
	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);
	Identity identity = null;
	PubmedXmlFetcher pubmedXmlFetcher;
	List<PubmedArticle> pubmedArticleList;
	List<ReCiterArticle> reCiterArticleList;
	List<String> gspPmidList;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		IdentityDaoImpl dao = new IdentityDaoImpl();
		identity = dao.getIdentityByCwid(TestController.cwid_junit);
		pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(identity.getLastName(),
				identity.getFirstInitial(), identity.getMiddleName(), TestController.cwid_junit);
		ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
		reCiterArticleList = new ArrayList<ReCiterArticle>();

		GoldStandardPmidsDao gspDao = new GoldStandardPmidsDaoImpl();
		gspPmidList = gspDao.getPmidsByCwid(TestController.cwid_junit);

		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid()
					.getPmidString();
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(TestController.cwid_junit,
					pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle,
					scopusArticle));
		}
	}

	@Test
	public void test() {
		for (ReCiterArticle article : reCiterArticleList) {
			{
				for (int i = 0; i < 1; i++) {
					List<String> articleKeywordList = Arrays.asList(article
							.getArticleTitle().split(" "));
					List<String> journalKeywordList = Arrays.asList(article
							.getJournal().getJournalTitle().split(" "));
					for (String articleKeyword : articleKeywordList) {

						String stemmedArticlekeyword = stemWord(articleKeyword);
						if (!articleKeyword.equals(stemmedArticlekeyword)) {
							slf4jLogger
									.info("Article Title before stemming =   "
											+ articleKeyword);
							slf4jLogger.info("After stemming =     "
									+ stemmedArticlekeyword);
						}
					}
					slf4jLogger
							.info("-- Finished stemming for article Title List --");
					for (String journalKeyword : journalKeywordList) {
						String stemmedJournalKeyword = stemWord(journalKeyword);
						if (!journalKeyword.equals(stemmedJournalKeyword)) {
							slf4jLogger
									.info("Journal keyword before stemming =   "
											+ journalKeyword);
							slf4jLogger.info("After stemming =     "
									+ stemmedJournalKeyword);
						}
					}
					slf4jLogger
							.info("-- Finished stemming for journal Keyword List --");
					ReCiterArticleKeywords articleKeywords = article
							.getArticleKeywords();
					for (Keyword keyword : articleKeywords.getKeywords()) {
						String origKeyword = keyword.getKeyword();
						String stemmedKeyword = stemWord(origKeyword);
						if (!origKeyword.equals(stemmedKeyword)) {
							slf4jLogger
									.info("Article keyword before stemming =   "
											+ origKeyword);
							slf4jLogger.info("After stemming =     "
									+ stemmedKeyword);
						}
					}
					slf4jLogger
							.info("-- Finished stemming for articleKeywordList --");
				}
			}

		}

	}

	private String stemWord(String word) {
		SnowballStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
