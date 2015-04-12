package main.reciter.model.author;

public class ReCiterAuthor {
	
	private final AuthorName name;
	private final AuthorAffiliation affiliation;
	
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
	
	@Override
	public String toString() {
		return "ReCiterAuthor [name=" + name + ", affiliation=" + affiliation
				+ "]";
	}
}
