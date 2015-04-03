package test.pubmed.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.xml.pubmed.PubmedEFetchHandler;
import main.xml.pubmed.model.MedlineCitationArticleAuthor;
import main.xml.pubmed.model.MedlineCitationKeywordList;
import main.xml.pubmed.model.MedlineCitationMeshHeading;
import main.xml.pubmed.model.PubmedArticle;

import org.junit.Test;
import org.xml.sax.SAXException;

public class PubmedXmlTest {

	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		saxParser.parse("test_xml/Kukafka.xml", pubmedXmlHandler);
		List<PubmedArticle> articles = pubmedXmlHandler.getPubmedArticles();
		assertEquals(63, articles.size());
		
		// PMID: 25548331
		PubmedArticle p1 = articles.get(0);
		String p1Title = p1.getMedlineCitation().getArticle().getArticleTitle();
		assertEquals("Registry-linked electronic influenza vaccine provider reminders: a cluster-crossover trial.", p1Title);
		
		String p1JournalTitle = p1.getMedlineCitation().getArticle().getJournal().getJournalTitle();
		assertEquals("Pediatrics", p1JournalTitle);
		
		List<MedlineCitationArticleAuthor> coAuthorList = p1.getMedlineCitation().getArticle().getAuthorList();
		assertEquals(10, coAuthorList.size());
		MedlineCitationArticleAuthor p1Author1 = coAuthorList.get(0);
		assertEquals("Stockwell", p1Author1.getLastName());
		assertEquals("Melissa S", p1Author1.getForeName());
		assertEquals("MS", p1Author1.getInitials());
		assertEquals("Departments of Pediatrics, Population and Family Health, NewYork-Presbyterian Hospital, New York, New York mss2112@columbia.edu.", p1Author1.getAffiliation());
		
		MedlineCitationArticleAuthor p1Author2 = coAuthorList.get(1);
		assertEquals("Catallozzi", p1Author2.getLastName());
		assertEquals("Marina", p1Author2.getForeName());
		assertEquals("M", p1Author2.getInitials());
		assertEquals("Departments of Pediatrics, Population and Family Health, NewYork-Presbyterian Hospital, New York, New York.", p1Author2.getAffiliation());
		
		MedlineCitationArticleAuthor p1Author3 = coAuthorList.get(2);
		assertEquals("Camargo", p1Author3.getLastName());
		assertEquals("Stewin", p1Author3.getForeName());
		assertEquals("S", p1Author3.getInitials());
		assertEquals("Departments of Pediatrics.", p1Author3.getAffiliation());
		
		MedlineCitationKeywordList keywords = p1.getMedlineCitation().getKeywordList();
		String[] keywordsArray = {"alert", "clinical decision support", "immunization", "influenza", "reminder", "vaccine"};
		for (int i = 0; i < keywords.getKeywordList().size(); i++) {
			assertEquals(keywordsArray[i], keywords.getKeywordList().get(i).getKeyword());
		}
		
		// PMID: 24551322
		PubmedArticle p2 = articles.get(1);
		List<MedlineCitationMeshHeading> meshHeadings = p2.getMedlineCitation().getMeshHeadingList();
		String[] meshArray = {"Audiovisual Aids", "Comprehension", "Health Literacy", "Humans", "Mathematical Concepts", "Self Care", "United States"};
		assertEquals(meshArray.length, meshHeadings.size());
		for (int i = 0; i < meshHeadings.size(); i++) {
			assertEquals(meshArray[i], meshHeadings.get(i).getDescriptorName().getDescriptorNameString());
		}
	}
}
