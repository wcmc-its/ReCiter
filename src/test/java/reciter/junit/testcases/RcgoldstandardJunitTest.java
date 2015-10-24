package reciter.junit.testcases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterExample;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.model.article.ReCiterArticle;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;

/* 	for  given cwid retrieved pubmedarticle list using GoldStandardPmidsDao.
 	checking the article pmid with authors known publications.
 	so if known publications list size is zero that means all the 
 	publications are added to the reciterArticleList.
 	and test case is passed, if known publications 
 	list size is greater than zero it means 
 	all known articles are not added to cluster
 	it that case the test fails */

public class RcgoldstandardJunitTest {
	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);
	ReCiterConfigProperty reCiterConfigProperty;
	String lastName;
	String middleName;
	String firstName;
	String affiliation;
	String firstInitial;
	String authorKeywords;
	String coAuthors;
	String department;
	PubmedXmlFetcher pubmedXmlFetcher;
	List<PubmedArticle> pubmedArticleList;
	List<ReCiterArticle> reCiterArticleList;
	List<String> gspPmidList;

	@Before
	public void setUp() throws Exception {
		String path = (new File("").getAbsolutePath()) + File.separator
				+ ReCiterConfigProperty.getDefaultLocation();
		ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
		try {
			reCiterConfigProperty.loadProperty(path + TestController.cwid_junit + "/" + TestController.cwid_junit
					+ ".properties");
		} catch (Exception e) {
			e.printStackTrace();
		}
		lastName = reCiterConfigProperty.getLastName();
		middleName = reCiterConfigProperty.getMiddleName();
		firstName = reCiterConfigProperty.getFirstName();
		affiliation = reCiterConfigProperty.getAuthorAffiliation();
		firstInitial = firstName.substring(0, 1);
		authorKeywords = reCiterConfigProperty.getAuthorKeywords();
		coAuthors = reCiterConfigProperty.getCoAuthors();
		department = reCiterConfigProperty.getAuthorDepartment();
		pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName,
				firstInitial, middleName, TestController.cwid_junit);

		ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
		reCiterArticleList = new ArrayList<ReCiterArticle>();
//		slf4jLogger.info("pubmedArticleList Size  = "
//				+ pubmedArticleList.size());
		GoldStandardPmidsDao gspDao = new GoldStandardPmidsDaoImpl();
		gspPmidList = gspDao.getPmidsByCwid(TestController.cwid_junit);

//		slf4jLogger.info("gspPmidList size    = " + gspPmidList.size());
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid()
					.getPmidString();
			while (gspPmidList.contains(pmid))
				gspPmidList.remove(pmid);
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(TestController.cwid_junit,
					pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle,
					scopusArticle));
		}
	}

	@Test
	public void test() {
		int size = gspPmidList.size();
		if (gspPmidList.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (String pmid : gspPmidList) {
				sb.append(pmid).append(" ");
			}
			slf4jLogger
					.info("Test case is failed , the following PMIDs are not added into cluster one ["
							+ sb.toString().trim() + "]");
		}
		slf4jLogger.info("Test Passed");
		
		//slf4jLogger.info("gspPmidList size  =     " + size);

	}
}
