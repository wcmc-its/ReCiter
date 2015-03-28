package test.pubmed.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.article.ReCiterArticleKeywords.Keyword;
import main.reciter.model.author.ReCiterAuthor;
import main.xml.pubmed.PubmedEFetchHandler;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.translator.ArticleTranslator;

import org.junit.Test;
import org.xml.sax.SAXException;

public class ReCiterTranslatorTest {

	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		PubmedEFetchHandler pubmedXmlHandler = new PubmedEFetchHandler();
		saxParser.parse("test_xml/translator.xml", pubmedXmlHandler);
		List<PubmedArticle> articles = pubmedXmlHandler.getPubmedArticles();

		// PMID: 23874357
		PubmedArticle article = articles.get(0);

		ReCiterArticle reCiterArticle = ArticleTranslator.translate(article);
		// Testing PMID.
		assertEquals(23874357, reCiterArticle.getArticleID());
		
		// Testing Article Title.
		assertEquals("An information systems model of the determinants of electronic health record use.", reCiterArticle.getArticleTitle().getTitle());
		
		// Testing Journal Title.
		assertEquals("Applied clinical informatics", reCiterArticle.getJournal().getJournalTitle());

		List<ReCiterAuthor> coAuthors = reCiterArticle.getArticleCoAuthors().getCoAuthors();
		// Testing Authors.
		assertEquals(7, coAuthors.size());
		ReCiterAuthor author1 = coAuthors.get(0);
		
		assertEquals("Messeri", author1.getAuthorName().getLastName());
		assertEquals("P", author1.getAuthorName().getFirstName());
		assertEquals("P", author1.getAuthorName().getFirstInitial());
		assertEquals(null, author1.getAuthorName().getMiddleName());
		assertEquals(null, author1.getAuthorName().getMiddleInitial());
		assertEquals("Mailman School of Public Health, Columbia Univiersity, Sociomedical Sciences, New York, New York 10032, USA. pam9@columbia.edu", author1.getAffiliation().getAffiliation());
		
		ReCiterAuthor author7 = coAuthors.get(6);
		assertEquals("Kukafka", author7.getAuthorName().getLastName());
		assertEquals("Rita", author7.getAuthorName().getFirstName());
		assertEquals("R", author7.getAuthorName().getFirstInitial());
		assertEquals("S", author7.getAuthorName().getMiddleName());
		assertEquals("S", author7.getAuthorName().getMiddleInitial());
		assertEquals(null, author7.getAffiliation().getAffiliation());
		
		// Testing Keywords.
		String[] keywordArray = {"EHR evaluation", "Electronic health records", "health information technology", "provider assessment of EHR quality"};
		String[] meshArray = {"Electronic Health Records", "Health Care Surveys", "Humans", "Information Systems", "Models, Statistical", "Primary Health Care", "Questionnaires", "Reproducibility of Results"};
		
		List<Keyword> keywords = reCiterArticle.getArticleKeywords().getKeywords();
		for (int i = 0; i < keywordArray.length; i++) {
			assertEquals(keywordArray[i], keywords.get(i).getKeyword());
		}
		int offSet = keywordArray.length;
		for (int i = 0; i < meshArray.length; i++) {
			assertEquals(meshArray[i], keywords.get(i + offSet).getKeyword());
		}
	}

}
