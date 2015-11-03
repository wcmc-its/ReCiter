package reciter.testcases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterExample;
import reciter.algorithm.evidence.targetauthor.boardcertification.strategy.CosineSimilarityStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.service.TargetAuthorService;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;
import database.dao.impl.IdentityDaoImpl;
import database.model.Identity;

// Issue #45

public class LeverageDataOnBoardCertifications {
	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);
	Identity identity = null;
	PubmedXmlFetcher pubmedXmlFetcher;
	List<PubmedArticle> pubmedArticleList;
	List<ReCiterArticle> reCiterArticleList;
	List<String> gspPmidList;
	private TargetAuthorService targetAuthorService;

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
		CosineSimilarityStrategy statergy = new CosineSimilarityStrategy();
		TargetAuthor targetAuthor = targetAuthorService.getTargetAuthor(TestController.cwid_junit);
		double score = 0;
		boolean Test = true;
		for(ReCiterArticle article : reCiterArticleList)
		{
		score =  statergy.executeStrategy(article, targetAuthor);
		slf4jLogger.info("Score = "+score);
		
		}
		
		if(score>0)
			Test = true;
		else Test = false;
		if (Test)
			slf4jLogger
					.info(" Test Passed issue 45");
		else
			slf4jLogger.info(" Test Failed  issue 45");

	}
}
