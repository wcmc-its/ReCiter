package xmlparser.pubmed;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;
import reciter.xml.parser.pubmed.model.PubMedArticle;

public class RetrievalArticleByEmailTest {

	private static final String ESEARCH = "http://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=1&usehistory=y&term=";
	private static final String WEB_ENV = "http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?retmode=xml&db=pubmed&query_key=1&WebEnv=";
	private static final Logger slf4jLogger = LoggerFactory.getLogger(RetrievalArticleByEmailTest.class);
	private String testEmail = "wcb2001@med.cornell.edu";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPubmedArticleRetrievalByEmail() throws ParserConfigurationException, SAXException, IOException {
		PubmedESearchHandler webEnvHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(ESEARCH + testEmail).openStream();
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(esearchStream, webEnvHandler);
		
		slf4jLogger.info("The web env is: " + webEnvHandler.getWebEnv());
		slf4jLogger.info("The article count is: " + webEnvHandler.getCount());
		
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		String webEnv = webEnvHandler.getWebEnv();
		InputSource webEnvStream = new InputSource(new URL(WEB_ENV + webEnv).openStream());
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(webEnvStream, pubmedXmlHandler);
		
		List<PubMedArticle> articles = pubmedXmlHandler.getPubmedArticles();
		slf4jLogger.info("Number of articles is: " + articles.size());
		
		assertEquals(2, articles.size()); // as of 11/1/2015, there are only two articles returned.
		
		PubMedArticle article1 = articles.get(0);
		PubMedArticle article2 = articles.get(1);
		
		// Below tests are optional, because we are not testing whether the parser is working, but that the number
		// of articles retrieved by using email is correct.
		assertEquals("Combining prediction, computation and experiment for the characterization of protein disorder.",
				article1.getMedlineCitation().getArticle().getArticleTitle());
		assertEquals("NMR spin relaxation methods for characterization of disorder and folding in proteins.",
				article2.getMedlineCitation().getArticle().getArticleTitle());
	}
}
