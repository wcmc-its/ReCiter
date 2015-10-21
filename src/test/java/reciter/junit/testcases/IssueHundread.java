package reciter.junit.testcases;

import java.io.File;
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

public class IssueHundread {

	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);
	static String cwid = "dml2005";
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
			reCiterConfigProperty.loadProperty(path + cwid + "/" + cwid
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

	}

	@Test
	public void test() {

		if (firstName.contains("II") || firstName.contains("III")
				|| firstName.contains("IV"))
			slf4jLogger.info("Test Failed, Circumstance 3");

		else
			slf4jLogger.info("Test Passed , Circumstance 3");

		// TODO need to validate Circumstance 4,5

		// if (firstName.contains("II") || firstName.contains("III")
		// || firstName.contains("IV"))
		// slf4jLogger.info("Test Failed, Circumstance 4");
		//
		// else
		// slf4jLogger.info("Test Passed , Circumstance 4");
		//
		// if (firstName.contains("II") || firstName.contains("III")
		// || firstName.contains("IV"))
		// slf4jLogger.info("Test Failed,Circumstance 5");
		//
		// else
		// slf4jLogger.info("Test Passed , Circumstance 5");
	}

}
