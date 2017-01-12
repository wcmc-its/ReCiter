package reciter.model.article;

import reciter.model.identity.AuthorName;

/**
 * 
 * @author jil3004
 *
 */
public class ReCiterAuthor {
	
	private AuthorName name;
	private String affiliation;
	
	public ReCiterAuthor(AuthorName name, String affiliation) {
		this.name = name;
		this.affiliation = affiliation;
	}
	
	public String getAffiliation() {
		return affiliation;
	}
	
	public AuthorName getAuthorName() {
		return name;
	}
}
