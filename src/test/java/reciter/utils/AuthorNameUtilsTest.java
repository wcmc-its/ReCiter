package reciter.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import reciter.model.converter.PubMedConverter;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.pubmed.MedlineCitation;
import reciter.model.pubmed.MedlineCitationArticle;
import reciter.model.pubmed.MedlineCitationArticleAuthor;
import reciter.model.pubmed.MedlineCitationPMID;
import reciter.model.pubmed.PubMedArticle;

public class AuthorNameUtilsTest {

	@Test
	public void testIsFullNameMatch() {
		AuthorName authorName = new AuthorName("John", "C", "Smith");
		AuthorName otherAuthorName = new AuthorName("John", "C", "Smith");
		boolean actual = AuthorNameUtils.isFullNameMatch(authorName, otherAuthorName);
		assertEquals(true, actual);
	}
	
	/**
	 * In this test case, one of the authors in the PubMed article matches fully to the target author. Therefore,
	 * we assume that no alternative author name for the target author in this PubMed article.
	 */
	@Test
	public void testExtractAlternateNamesFromPubMedArticlesRetrievedByEmail() {
		List<PubMedArticle> pubMedArticles = new ArrayList<>();
		PubMedArticle pubMedArticle = new PubMedArticle();
		pubMedArticles.add(pubMedArticle);
		pubMedArticle.setMedlineCitation(new MedlineCitation());
		pubMedArticle.getMedlineCitation().setMedlineCitationPMID(new MedlineCitationPMID(1L));
		pubMedArticle.getMedlineCitation().setArticle(new MedlineCitationArticle());
		pubMedArticle.getMedlineCitation().getArticle().setAuthorList(new ArrayList<>());
		MedlineCitationArticleAuthor author1 = new MedlineCitationArticleAuthor();
		author1.setForeName("John M");
		author1.setInitials("JM");
		author1.setLastName("Smith");
		MedlineCitationArticleAuthor author2 = new MedlineCitationArticleAuthor();
		author2.setForeName("Jason S");
		author2.setInitials("JS");
		author2.setLastName("Smith");
		MedlineCitationArticleAuthor author3 = new MedlineCitationArticleAuthor();
		author3.setForeName("Jorge M");
		author3.setInitials("JM");
		author3.setLastName("Smith");
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author1);
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author2);
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author3);
		
		Identity targetAuthor = new Identity();
		targetAuthor.setPrimaryName(new AuthorName("John", "M", "Smith"));
		Map<Long, List<AuthorName>> map = AuthorNameUtils.extractAlternateNamesFromPubMedArticlesRetrievedByEmail(pubMedArticles, targetAuthor);
		assertEquals(0, map.size());
	}
	
	/**
	 * Given 3 author names, all of which do not fully match that of the target author. There exists 2 authors
	 * in the list of authors that match the target author's last name and first name initial. One of those might
	 * be an alternative name for the target author, so we add both to the list of possible alternative names.
	 */
//	@Test
	public void testExtractAlternateName() {
		List<PubMedArticle> pubMedArticles = new ArrayList<>();
		PubMedArticle pubMedArticle = new PubMedArticle();
		pubMedArticles.add(pubMedArticle);
		pubMedArticle.setMedlineCitation(new MedlineCitation());
		pubMedArticle.getMedlineCitation().setMedlineCitationPMID(new MedlineCitationPMID(1L));
		pubMedArticle.getMedlineCitation().setArticle(new MedlineCitationArticle());
		pubMedArticle.getMedlineCitation().getArticle().setAuthorList(new ArrayList<>());
		MedlineCitationArticleAuthor author1 = new MedlineCitationArticleAuthor();
		author1.setForeName("George W");
		author1.setInitials("GW");
		author1.setLastName("Washington");
		MedlineCitationArticleAuthor author2 = new MedlineCitationArticleAuthor();
		author2.setForeName("Jason S");
		author2.setInitials("JS");
		author2.setLastName("Smith");
		MedlineCitationArticleAuthor author3 = new MedlineCitationArticleAuthor();
		author3.setForeName("Jorge M");
		author3.setInitials("JM");
		author3.setLastName("Smith");
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author1);
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author2);
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author3);
		
		Identity targetAuthor = new Identity();
		targetAuthor.setPrimaryName(new AuthorName("John", "M", "Smith"));
		Map<Long, List<AuthorName>> map = AuthorNameUtils.extractAlternateNamesFromPubMedArticlesRetrievedByEmail(pubMedArticles, targetAuthor);
		assertEquals(1, map.size());
		List<AuthorName> authorNames = map.get(1L);
		assertEquals(false, authorNames.contains(PubMedConverter.extractAuthorName(author1)));
		assertEquals(true, authorNames.contains(PubMedConverter.extractAuthorName(author2)));
		assertEquals(true, authorNames.contains(PubMedConverter.extractAuthorName(author3)));
	}
	
	/**
	 * In this test case, the PubMed article doesn't contain an author name whose last name matches that of the
	 * target author's last name. So no such alternative names exist.
	 */
	@Test
	public void testExtractAlternateNameThatContainsNoAlternateNames() {
		List<PubMedArticle> pubMedArticles = new ArrayList<>();
		PubMedArticle pubMedArticle = new PubMedArticle();
		pubMedArticles.add(pubMedArticle);
		pubMedArticle.setMedlineCitation(new MedlineCitation());
		pubMedArticle.getMedlineCitation().setMedlineCitationPMID(new MedlineCitationPMID(1L));
		pubMedArticle.getMedlineCitation().setArticle(new MedlineCitationArticle());
		pubMedArticle.getMedlineCitation().getArticle().setAuthorList(new ArrayList<>());
		MedlineCitationArticleAuthor author1 = new MedlineCitationArticleAuthor();
		author1.setForeName("George W");
		author1.setInitials("GW");
		author1.setLastName("Washington");
		MedlineCitationArticleAuthor author2 = new MedlineCitationArticleAuthor();
		author2.setForeName("Jason S");
		author2.setInitials("JS");
		author2.setLastName("Harvey");
		MedlineCitationArticleAuthor author3 = new MedlineCitationArticleAuthor();
		author3.setForeName("Jorge M");
		author3.setInitials("JM");
		author3.setLastName("Wells");
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author1);
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author2);
		pubMedArticle.getMedlineCitation().getArticle().getAuthorList().add(author3);
		
		Identity targetAuthor = new Identity();
		targetAuthor.setPrimaryName(new AuthorName("John", "M", "Smith"));
		Map<Long, List<AuthorName>> map = AuthorNameUtils.extractAlternateNamesFromPubMedArticlesRetrievedByEmail(pubMedArticles, targetAuthor);
		assertEquals(0, map.size());
	}
	

}
