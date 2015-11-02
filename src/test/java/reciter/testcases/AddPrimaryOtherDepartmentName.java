package reciter.testcases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterExample;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterArticleKeywords.Keyword;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;
import database.dao.impl.IdentityDaoImpl;
import database.model.Identity;

public class AddPrimaryOtherDepartmentName {

	// Issue #46

	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);
	Identity identity = null;
	PubmedXmlFetcher pubmedXmlFetcher;
	List<PubmedArticle> pubmedArticleList;
	List<ReCiterArticle> reCiterArticleList;
	List<String> gspPmidList;

	@Before
	public void setUp() throws Exception {
		IdentityDaoImpl dao = new IdentityDaoImpl();
		identity = dao.getIdentityByCwid(TestController.cwid_junit);
		pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(
				identity.getLastName(), identity.getFirstInitial(),
				identity.getMiddleName(), TestController.cwid_junit);

		ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
		reCiterArticleList = new ArrayList<ReCiterArticle>();
		GoldStandardPmidsDao gspDao = new GoldStandardPmidsDaoImpl();
		gspPmidList = gspDao.getPmidsByCwid(TestController.cwid_junit);
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid()
					.getPmidString();
			while (gspPmidList.contains(pmid))
				gspPmidList.remove(pmid);
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(
					TestController.cwid_junit, pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle,
					scopusArticle));
		}
	}

	@Test
	public void test() {
		boolean pTest = false;
		boolean oTest = false;
		slf4jLogger.info("other Department = " + identity.getOtherDepartment());
		slf4jLogger.info("Primary Department = "
				+ identity.getPrimaryDepartment());

		for (ReCiterArticle article : reCiterArticleList) {
			List<String> articleKeywordList = Arrays.asList(article
					.getArticleTitle().split(" "));
			List<String> journalKeywordList = Arrays.asList(article
					.getJournal().getJournalTitle().split(" "));
			for (String articleKeyword : articleKeywordList) {
				if (identity.getOtherDepartment() == null
						|| articleKeyword.equals(identity.getOtherDepartment())) {
					slf4jLogger
							.info("Found other Department in List of Keywords");
					oTest = true;
					slf4jLogger.info("otest = " + oTest + " &&&& ptest = "
							+ pTest);
				} else if (identity.getPrimaryDepartment() == null
						|| articleKeyword.equals(identity
								.getPrimaryDepartment())) {
					slf4jLogger
							.info("Found Primary Department in List of Keywords");
					pTest = true;
					slf4jLogger.info("otest = " + oTest + " &&&& ptest = "
							+ pTest);
				}
			}
			for (String journalKeyword : journalKeywordList) {
				if (identity.getOtherDepartment() == null
						|| journalKeyword.equals(identity.getOtherDepartment())) {
					slf4jLogger
							.info("Found other Department in List of Keywords");
					oTest = true;
					slf4jLogger.info("otest = " + oTest + " &&&& ptest = "
							+ pTest);
				} else if (identity.getPrimaryDepartment() == null
						|| journalKeyword.equals(identity
								.getPrimaryDepartment())) {
					slf4jLogger
							.info("Found Primary Department in List of Keywords");
					pTest = true;
					slf4jLogger.info("otest = " + oTest + " &&&& ptest = "
							+ pTest);
				}
			}

			ReCiterArticleKeywords articleKeywords = article
					.getArticleKeywords();
			for (Keyword keyword : articleKeywords.getKeywords()) {
				String origKeyword = keyword.getKeyword();
				if (identity.getOtherDepartment() == null
						|| origKeyword.equals(identity.getOtherDepartment())) {
					slf4jLogger
							.info("Found other Department in List of Keywords");
					oTest = true;
					slf4jLogger.info("otest = " + oTest + " &&&& ptest = "
							+ pTest);
				} else if (identity.getPrimaryDepartment() == null
						|| origKeyword.equals(identity.getPrimaryDepartment())) {
					slf4jLogger
							.info("Found Primary Department in List of Keywords");
					pTest = true;
					slf4jLogger.info("otest = " + oTest + " &&&& ptest = "
							+ pTest);
				}

			}
			slf4jLogger.info("otest = " + oTest + " &&&& ptest = " + pTest);

			if (pTest == true && oTest == true)
				slf4jLogger
						.info("Both Primary and Other Departments are added to List of Keywords , Test Passed");
			else if (pTest == true)
				slf4jLogger
						.info("Only Primary Department is added to List of Keywords , Test Failed");
			else if (oTest == true)
				slf4jLogger
						.info("Only Other Department is added to List of Keywords , Test Failed");
			else
				slf4jLogger.info(" Test for issue number 46  Failed ");

		}
	}
}
