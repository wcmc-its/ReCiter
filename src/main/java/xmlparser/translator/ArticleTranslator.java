package xmlparser.translator;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleKeywords;
import reciter.model.article.ReCiterJournal;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import xmlparser.pubmed.model.MedlineCitationArticleAuthor;
import xmlparser.pubmed.model.MedlineCitationGrant;
import xmlparser.pubmed.model.MedlineCitationKeyword;
import xmlparser.pubmed.model.MedlineCitationKeywordList;
import xmlparser.pubmed.model.MedlineCitationMeshHeading;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.model.Author;
import xmlparser.scopus.model.ScopusArticle;

/**
 * Translator that translates a PubmedArticle to ReCiterArticle.
 * @author jil3004
 *
 */
public class ArticleTranslator {

	/**
	 * Translates a PubmedArticle into a ReCiterArticle.
	 * @param pubmedArticle
	 * @return
	 */
	public static ReCiterArticle translate(PubmedArticle pubmedArticle, ScopusArticle scopusArticle) {

		// PMID
		String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
		ReCiterArticle reCiterArticle = new ReCiterArticle(Integer.parseInt(pmid));

		// Article title
		String articleTitle = pubmedArticle.getMedlineCitation().getArticle().getArticleTitle();

		// Journal Title
		String journalTitle = pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalTitle();

		// Translating Journal Issue PubDate Year.
		int journalIssuePubDateYear = Integer.parseInt(pubmedArticle.getMedlineCitation().getArticle().getJournal().getJournalIssue().getPubDate().getYear());

		// Co-authors
		List<MedlineCitationArticleAuthor> coAuthors = pubmedArticle.getMedlineCitation().getArticle().getAuthorList();

		// Translating Co-Authors
		ReCiterArticleAuthors reCiterCoAuthors = new ReCiterArticleAuthors();
		for (MedlineCitationArticleAuthor author : coAuthors) {
			String lastName = author.getLastName();
			String foreName = author.getForeName();
			String initials = author.getInitials();
			String firstName = null;
			String middleName = null;

			// PubMed sometimes concatenates the first name and middle initial into <ForeName> xml tag.
			// This extracts the first name and middle initial.

			// Sometimes forename doesn't exist in XML (ie: 8661541). So initials are used instead.
			// Forename take precedence. If foreName doesn't exist, use initials. If initials doesn't exist, use null.
			// TODO: Deal with collective names in XML.
			if (lastName != null) {
				if (foreName != null) {
					String[] foreNameArray = foreName.split("\\s+");
					if (foreNameArray.length == 2) {
						firstName = foreNameArray[0];
						middleName = foreNameArray[1];
					} else {
						firstName = foreName;
					}
				} else if (initials != null) {
					firstName = initials;
				}
				String affiliation = author.getAffiliation();
				AuthorName authorName = new AuthorName(firstName, middleName, lastName);
				AuthorAffiliation authorAffiliation = new AuthorAffiliation(affiliation);

				ReCiterAuthor reCiterAuthor = new ReCiterAuthor(authorName, authorAffiliation);
				reCiterCoAuthors.addAuthor(reCiterAuthor);
			}
		}

		MedlineCitationKeywordList keywordList = pubmedArticle.getMedlineCitation().getKeywordList();

		// Translating Keywords.
		ReCiterArticleKeywords articleKeywords = new ReCiterArticleKeywords();
		if (keywordList != null) {
			for (MedlineCitationKeyword keyword : keywordList.getKeywordList()) {
				articleKeywords.addKeyword(keyword.getKeyword());
			}
		}

		List<MedlineCitationMeshHeading> meshList = pubmedArticle.getMedlineCitation().getMeshHeadingList();
		
		if (meshList != null) {
			// Translating Mesh
			for (MedlineCitationMeshHeading mesh : meshList) {
				String meshTerm = mesh.getDescriptorName().getDescriptorNameString();				
				articleKeywords.addKeyword(meshTerm);
				reCiterArticle.setMeshTerm(meshTerm);
			}	
		}
		reCiterArticle.setArticleTitle(articleTitle);
		reCiterArticle.setJournal(new ReCiterJournal(journalTitle));
		reCiterArticle.setArticleCoAuthors(reCiterCoAuthors);
		reCiterArticle.setArticleKeywords(articleKeywords);
		reCiterArticle.getJournal().setJournalIssuePubDateYear(journalIssuePubDateYear);
		reCiterArticle.getJournal().setIsoAbbreviation(pubmedArticle.getMedlineCitation().getArticle().getJournal().getIsoAbbreviation());

		// Update PubMed's authors' first name from Scopus Article. Logic is as follows:
		// 1. First compare last name if match:
		// 2. Check scopus's first name has length > 1, so no initials (b/c PubMed already contains this info.)
		// 3. Check first initial is same.
		// 4. Check that scopus author's first name is more "complete" than Pubmed'a author name.
		// 5. Only update if PubMed's author name is length = 1.
		// 6. remove periods and whitespaces. Grab only the first name (Scopus also provides middle initial).
		
		if (scopusArticle != null) {
			for (Author scopusAuthor : scopusArticle.getAuthors().values()) {
				String scopusAuthorFirstName = scopusAuthor.getGivenName();
				String scopusAuthorLastName = scopusAuthor.getSurname();
				for (ReCiterAuthor reCiterAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {
					String reCiterAuthorLastName = reCiterAuthor.getAuthorName().getLastName();
					if (StringUtils.equalsIgnoreCase(scopusAuthorLastName, reCiterAuthorLastName)) {
						String reCiterAuthorFirstName = reCiterAuthor.getAuthorName().getFirstName();
						String reCiterAuthorFirstInitial = reCiterAuthor.getAuthorName().getFirstInitial();
						if (scopusAuthorFirstName != null && scopusAuthorFirstName.length() > 1) {
							if (scopusAuthorFirstName.substring(0, 1).equals(reCiterAuthorFirstInitial)) {
								if (scopusAuthorFirstName.length() > reCiterAuthorFirstName.length()) {
//									System.out.println("[" + scopusAuthorFirstName + "], [" + reCiterAuthorFirstName + "]");

									if (reCiterAuthorFirstName.length() == 1) {

										int indexOfWhiteSpace = scopusAuthorFirstName.indexOf(" ");
										scopusAuthorFirstName = scopusAuthorFirstName.replaceAll("[\\.]", "");
										if (indexOfWhiteSpace == -1) {
											reCiterAuthor.getAuthorName().setFirstName(scopusAuthorFirstName);
										} else {
											reCiterAuthor.getAuthorName().setFirstName(scopusAuthorFirstName.substring(0, indexOfWhiteSpace));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		reCiterArticle.setScopusArticle(scopusArticle);
		
		List<MedlineCitationGrant> medlineCitationGrants = pubmedArticle.getMedlineCitation().getArticle().getGrantList();
		
//		for (MedlineCitationGrant medlineCitationGrant : medlineCitationGrants) {
//			
//		}
		return reCiterArticle;
	}
}

