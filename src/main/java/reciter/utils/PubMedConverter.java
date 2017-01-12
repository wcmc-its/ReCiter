package reciter.utils;

import reciter.model.identity.AuthorName;
import reciter.model.pubmed.MedlineCitationArticleAuthor;

public class PubMedConverter {

	/**
	 * PubMed sometimes concatenates the first name and middle initial into <ForeName> xml tag.
	 * This extracts the first name and middle initial. 
	 * Sometimes forename doesn't exist in XML (ie: 8661541). So initials are used instead. 
	 * Forename take precedence. If foreName doesn't exist, use initials. If initials doesn't exist, use null.
	 *  
	 * TODO: Deal with collective names in XML.
	 * @param medlineCitationArticleAuthor
	 * @return
	 */
	public static AuthorName extractAuthorName(MedlineCitationArticleAuthor medlineCitationArticleAuthor) {
		String lastName = medlineCitationArticleAuthor.getLastName();

		if (lastName == null) {
			return null;
		}
		
		String foreName = medlineCitationArticleAuthor.getForeName();
		String initials = medlineCitationArticleAuthor.getInitials();

		String firstName = null;
		String middleName = null;
		
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
		
		AuthorName authorName = new AuthorName(firstName, middleName, lastName);
		return authorName;
	}
}
