package xmlparser.pubmed;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import xmlparser.pubmed.model.MedlineCitationCommentsCorrections;
import xmlparser.pubmed.model.PubmedArticle;

public class PubmedXmlCommentsCorrectionsListTest {

	private String location = "src/test/resources/xml/";
	private String fileName = "14691233.xml";
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
	
	/**
	 * Test that pmid is parsed corrected from the '<CommentsCorrections>' tag in the PubMed xml.
	 */
	@Test
	public void testCommentsCorrections() {
		System.out.println("Testing 'testCommentsCorrections'");
		List<MedlineCitationCommentsCorrections> commentsCorrectionsList = article.getMedlineCitation().getCommentsCorrectionsList();
		assertEquals(commentsCorrectionsList.size(), 61);
		MedlineCitationCommentsCorrections first = commentsCorrectionsList.get(0);
		assertEquals(first.getPmid(), "1814507");
	}

}
