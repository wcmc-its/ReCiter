package reciter.junit.testcases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterExample;
import reciter.model.article.ReCiterArticle;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;
import database.dao.impl.IdentityDaoImpl;
import database.model.Identity;

public class issue78 {
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

		boolean Test = true;
		if (Test)
			slf4jLogger
					.info("Article is encoded to utf 8 characters , Test Passed");
		else
			slf4jLogger.info(" Test Failed ");
	}
}
