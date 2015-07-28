package reciter.model.author;

/**
 * 
 * @author jil3004
 *
 */
public class ReCiterAuthor {
	
	private AuthorName name;
	private AuthorAffiliation affiliation;
	
	public ReCiterAuthor(AuthorName name, AuthorAffiliation affiliation) {
		this.name = name;
		this.affiliation = affiliation;
	}
	
	public AuthorAffiliation getAffiliation() {
		return affiliation;
	}
	
	public AuthorName getAuthorName() {
		return name;
	}
}
