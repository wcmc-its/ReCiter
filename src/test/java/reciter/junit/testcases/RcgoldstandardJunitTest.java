package reciter.junit.testcases;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.model.article.ReCiterArticle;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;
import database.dao.GoldStandardPmidsDao;
import database.dao.impl.GoldStandardPmidsDaoImpl;

public class RcgoldstandardJunitTest {
	static String cwid = "aad2004";
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
		department = reCiterConfigProperty.getAuthorDepartment();
		pubmedXmlFetcher = new PubmedXmlFetcher();
		pubmedArticleList = pubmedXmlFetcher
				.getPubmedArticle(lastName, firstInitial, middleName, cwid);
		ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
		reCiterArticleList = new ArrayList<ReCiterArticle>();

		GoldStandardPmidsDao gspDao = new GoldStandardPmidsDaoImpl();
		gspPmidList = gspDao.getPmidsByCwid(cwid);

		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			if(gspPmidList.contains(pmid))gspPmidList.remove(pmid);
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
			reCiterArticleList.add(ArticleTranslator.translate(pubmedArticle, scopusArticle));
		}
	}

	@Test
	public void test() {
		int size = gspPmidList.size();
		 assertTrue(size>1);


	}
}
