package test.pubmed.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import main.xml.pubmed.PubmedEFetchHandler;
import main.xml.pubmed.PubmedESearchHandler;
import main.xml.pubmed.model.MedlineCitationArticleAuthor;
import main.xml.pubmed.model.MedlineCitationKeywordList;
import main.xml.pubmed.model.MedlineCitationMeshHeading;
import main.xml.pubmed.model.PubmedArticle;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PubmedXmlTest {

	private static final String ESEARCH = "http://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=1&usehistory=y&term=";
	private static final String WEB_ENV = "http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?retmode=xml&db=pubmed&query_key=1&WebEnv=";
	
	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException {
		PubmedESearchHandler webEnvHandler = new PubmedESearchHandler();
		InputStream esearchStream = new URL(ESEARCH + "Kukafka%20R[au]").openStream();
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(esearchStream, webEnvHandler);
		
		System.out.println("The web env is: " + webEnvHandler.getWebEnv());
		System.out.println("The article count is: " + webEnvHandler.getCount());
		
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		String webEnv = webEnvHandler.getWebEnv();
		InputSource webEnvStream = new InputSource(new URL(WEB_ENV + webEnv).openStream());
		SAXParserFactory.newInstance()
						.newSAXParser()
						.parse(webEnvStream, pubmedXmlHandler);
		
		List<PubmedArticle> articles = pubmedXmlHandler.getPubmedArticles();
		assertEquals(64, articles.size());
		
		PubmedArticle p1 = null;
		PubmedArticle p2 = null;
		
		for (PubmedArticle article : articles) {
			if (article.getMedlineCitation().getPmid().getPmidString().equals("25548331")) {
				p1 = article;
			} else if (article.getMedlineCitation().getPmid().getPmidString().equals("24551322")) {
				p2 = article;
			}
		}
		// PMID: 25548331
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
		
		String pubDateYear = p1.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().getYear();
		assertEquals("2015", pubDateYear);
		
		// PMID: 24551322
		List<MedlineCitationMeshHeading> meshHeadings = p2.getMedlineCitation().getMeshHeadingList();
		String[] meshArray = {"Audiovisual Aids", "Comprehension", "Health Literacy", "Humans", "Mathematical Concepts", "Self Care", "United States"};
		assertEquals(meshArray.length, meshHeadings.size());
		for (int i = 0; i < meshHeadings.size(); i++) {
			assertEquals(meshArray[i], meshHeadings.get(i).getDescriptorName().getDescriptorNameString());
		}
		
		assertEquals("2013", p2.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().getYear());
	}
}
