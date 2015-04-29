package main.xml.translator;

import java.util.ArrayList;
import java.util.List;

import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.article.ReCiterArticleCoAuthors;
import main.reciter.model.article.ReCiterArticleKeywords;
import main.reciter.model.article.ReCiterArticleTitle;
import main.reciter.model.article.ReCiterJournal;
import main.reciter.model.author.AuthorAffiliation;
import main.reciter.model.author.AuthorName;
import main.reciter.model.author.ReCiterAuthor;
import main.xml.pubmed.model.MedlineCitationArticleAuthor;
import main.xml.pubmed.model.MedlineCitationKeyword;
import main.xml.pubmed.model.MedlineCitationKeywordList;
import main.xml.pubmed.model.MedlineCitationMeshHeading;
import main.xml.pubmed.model.PubmedArticle;

/**
 * Translator that translates a PubmedArticle to ReCiterArticle.
 * @author jil3004
 *
 */
public class ArticleTranslator {

	/**
	 * Translates of list of PubmedArticles into a list of ReCiterArticles.
	 * @param pubmedArticleList a list of PubMedArticles.
	 * @return a list of ReCiterArticles.
	 */
	public static List<ReCiterArticle> translateAll(List<PubmedArticle> pubmedArticleList) {
		List<ReCiterArticle> reCiterArticleList = new ArrayList<ReCiterArticle>();
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			reCiterArticleList.add(translate(pubmedArticle));
		}
		return reCiterArticleList;
	}
	
	/**
	 * Translates a PubmedArticle into a ReCiterArticle.
	 * @param pubmedArticle
	 * @return
	 */
	public static ReCiterArticle translate(PubmedArticle pubmedArticle) {
		String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
		String articleTitle = pubmedArticle.getMedlineCitation().getArticle().getArticleTitle();
		String journalTitle = pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalTitle();
		List<MedlineCitationArticleAuthor> coAuthors = pubmedArticle.getMedlineCitation().getArticle().getAuthorList();
		MedlineCitationKeywordList keywordList = pubmedArticle.getMedlineCitation().getKeywordList();
		List<MedlineCitationMeshHeading> meshList = pubmedArticle.getMedlineCitation().getMeshHeadingList();
		
		// Translating Co-Authors.
		ReCiterArticleCoAuthors reCiterCoAuthors = new ReCiterArticleCoAuthors();
		for (MedlineCitationArticleAuthor author : coAuthors) {
			String lastName = author.getLastName();
			String foreName = author.getForeName();
			String firstName = null;
			String middleName = null;
			// PubMed sometimes concatenates the first name and middle initial into <ForeName> xml tag.
			// This extracts the first name and middle initial.
			// TODO: Deal with collective names in XML.
			if (lastName != null && foreName != null) {
				String[] foreNameArray = foreName.split("\\s+");
				if (foreNameArray.length == 2) {
					firstName = foreNameArray[0];
					middleName = foreNameArray[1];
				} else {
					firstName = foreName;
				}
				String affiliation = author.getAffiliation();
				
				AuthorName authorName = new AuthorName(firstName, middleName, lastName);
				AuthorAffiliation authorAffiliation = new AuthorAffiliation(affiliation);
				ReCiterAuthor reCiterAuthor = new ReCiterAuthor(authorName, authorAffiliation);
				reCiterCoAuthors.addCoAuthor(reCiterAuthor);
			}
		}
		
		// Translating Keywords.
		ReCiterArticleKeywords articleKeywords = new ReCiterArticleKeywords();
		if (keywordList != null) {
			for (MedlineCitationKeyword keyword : keywordList.getKeywordList()) {
				articleKeywords.addKeyword(keyword.getKeyword());
			}
		}
		
		if (meshList != null) {
			// Translating Mesh
			for (MedlineCitationMeshHeading mesh : meshList) {
				articleKeywords.addKeyword(mesh.getDescriptorName().getDescriptorNameString());
			}	
		}
		
		// Translating Journal Issue PubDate Year.
		int journalIssuePubDateYear = Integer.parseInt(pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().getYear());
		
		ReCiterArticle reCiterArticle = new ReCiterArticle(Integer.parseInt(pmid));
		reCiterArticle.setArticleTitle(new ReCiterArticleTitle(articleTitle));
		reCiterArticle.setJournal(new ReCiterJournal(journalTitle));
		reCiterArticle.setArticleCoAuthors(reCiterCoAuthors);
		reCiterArticle.setArticleKeywords(articleKeywords);
		reCiterArticle.getJournal().setJournalIssuePubDateYear(journalIssuePubDateYear);
		return reCiterArticle;
	}
}

