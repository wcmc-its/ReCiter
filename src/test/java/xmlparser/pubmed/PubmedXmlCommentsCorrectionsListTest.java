package xmlparser.pubmed;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import reciter.xml.parser.pubmed.handler.PubmedEFetchHandler;
import reciter.xml.parser.pubmed.model.MedlineCitationCommentsCorrections;
import reciter.xml.parser.pubmed.model.PubmedArticle;

public class PubmedXmlCommentsCorrectionsListTest {

	private String location = "src/test/resources/xml/";
	private String fileName = "14691233.xml";
	private List<MedlineCitationCommentsCorrections> commentsCorrectionsList;

	@Before
	public void setUp() throws Exception {
		File file = new File(location + fileName);
		InputStream inputStream = new FileInputStream(file);
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		InputSource is = new InputSource(reader);
		is.setEncoding("UTF-8");
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(is, pubmedXmlHandler);
		
		PubmedArticle article = pubmedXmlHandler.getPubmedArticles().get(0);
		commentsCorrectionsList = article.getMedlineCitation().getCommentsCorrectionsList();
	}
	
	/**
	 * Test that size of '<CommentsCorrections>' is correct.
	 */
	@Test
	public void testSizeOfCommentsCorrectionsList() {
		assertEquals(61, commentsCorrectionsList.size());
	}
	
	/**
	 * Test that the first pmid is parsed correctly from the '<CommentsCorrections>' tag in the PubMed xml.
	 */
	@Test
	public void testFirstPmidOfCommentsCorrectionsList() {
		MedlineCitationCommentsCorrections first = commentsCorrectionsList.get(0);
		assertEquals("1814507", first.getPmid());
	}
}
