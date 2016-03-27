package xmlparser.pubmed;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.parsers.SAXParserFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.model.PubmedArticle;

public class PubmedEFetchHandlerTest {

	private String location = "src/test/resources/xml/";
	private String fileName = "hef9020_19944046_truncated_pmid_test.xml";
	private PubmedArticle article;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.out.println("Set Up before class");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		System.out.println("Tear down after class");
	}

	@Before
	public void setUp() throws Exception {
		System.out.println("Setup");
		File file = new File(location + fileName);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(is, pubmedXmlHandler);
		
		article = pubmedXmlHandler.getPubmedArticles().get(0);
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Tear Down");
	}

	@Test
	public void testPmid() {
		String pmid = article.getMedlineCitation().getPmid().getPmidString();
		assertEquals(pmid, "19944046");
	}
	
	@Test
	public void testArticleTitle() {
		String title = article.getMedlineCitation().getArticle().getArticleTitle();
		assertEquals(title, "[Osteoid osteoma].");
	}
	
	@Test
	public void testArticleJournalTitle() {
		String journalTitle = article.getMedlineCitation().getArticle().getJournal().getJournalTitle();
		assertEquals(journalTitle, "Acta médica portuguesa");
	}
}
