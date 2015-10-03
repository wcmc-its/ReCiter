package reciter.junit.testcases;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.ReCiterExample;
import reciter.erroranalysis.ReCiterConfigProperty;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterArticleKeywords.Keyword;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import xmlparser.translator.ArticleTranslator;

public class SpecialCharactersJunitTest {
	private final static Logger slf4jLogger = LoggerFactory
			.getLogger(ReCiterExample.class);
	static String cwid = "rdgranst";
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


		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid()
					.getPmidString();
			ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid,
					pmid);
			ReCiterArticle article = ArticleTranslator.translate(pubmedArticle,
					scopusArticle);
			reCiterArticleList.add(article);
		}
	
	}

	@Test
	public void test() {
		 boolean success=  false;
		 String origKeyword = null;
		 for (ReCiterArticle article : reCiterArticleList)
		 {
			 ReCiterArticleKeywords keywords = article.getArticleKeywords();
			 for(Keyword keyword: keywords.getKeywords()){
				 origKeyword = keyword.getKeyword();
				 if(origKeyword!= null)
					success = validate(origKeyword);
				}
			 
			 if(success)slf4jLogger.info("Article is encoded to utf 8 characters , Test Passed");
			 else slf4jLogger.info(" Test Failed     because of Keyword "+origKeyword);
		 }
		 
		 
		 
	}
	
	private boolean validate(String s)
	{
		CharsetDecoder cs = Charset.forName("UTF-8").newDecoder();
		try {
	        cs.decode(ByteBuffer.wrap(s.getBytes()));
	    }
	    catch(CharacterCodingException e){
	    	return false;
	    } 
		return true;
		
	}

}
