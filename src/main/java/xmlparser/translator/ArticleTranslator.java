package xmlparser.translator;

import java.util.ArrayList;
import java.util.List;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleCoAuthors;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterArticleTitle;
import reciter.model.article.ReCiterJournal;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import xmlparser.pubmed.model.MedlineCitationArticleAuthor;
import xmlparser.pubmed.model.MedlineCitationKeyword;
import xmlparser.pubmed.model.MedlineCitationKeywordList;
import xmlparser.pubmed.model.MedlineCitationMeshHeading;
import xmlparser.pubmed.model.PubmedArticle;

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
		
		ReCiterArticle reCiterArticle = new ReCiterArticle(Integer.parseInt(pmid));
		
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
				
				// Translate Scopus Affiliation.
				if (lastName.equals(pmid)) {
					reCiterArticle.setScopusAffiliation(affiliation);
				}
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
		
		
		reCiterArticle.setArticleTitle(new ReCiterArticleTitle(articleTitle));
		reCiterArticle.setJournal(new ReCiterJournal(journalTitle));
		reCiterArticle.setArticleCoAuthors(reCiterCoAuthors);
		reCiterArticle.setArticleKeywords(articleKeywords);
		reCiterArticle.getJournal().setJournalIssuePubDateYear(journalIssuePubDateYear);
		reCiterArticle.getJournal().setIsoAbbreviation(pubmedArticle.getMedlineCitation().getArticle().getJournal().getIsoAbbreviation());
		return reCiterArticle;
	}
}

