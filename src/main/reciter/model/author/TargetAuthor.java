package main.reciter.model.author;

/**
 * Singleton TargetAuthor
 * @author jil3004
 *
 */
public class TargetAuthor extends ReCiterAuthor {

	private static TargetAuthor instance;
	
	private TargetAuthor(AuthorName name, AuthorAffiliation affiliation) {
		super(name, affiliation);
	}
	
	public static TargetAuthor getInstance() {
		return instance;
	}
	
	public static void init(AuthorName name, AuthorAffiliation affiliation) {
		instance = new TargetAuthor(name, affiliation);
	}
}
