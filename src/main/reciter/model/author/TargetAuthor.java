package main.reciter.model.author;

import main.reciter.model.article.ReCiterArticle;

/**
 * Singleton TargetAuthor
 * @author jil3004
 *
 */
public class TargetAuthor extends ReCiterAuthor {

	private static TargetAuthor instance;
	private ReCiterArticle targetAuthorArticleIndexed;
	
	private TargetAuthor(AuthorName name, AuthorAffiliation affiliation) {
		super(name, affiliation);
	}
	
	public static TargetAuthor getInstance() {
		return instance;
	}
	
	public static void init(AuthorName name, AuthorAffiliation affiliation) {
		instance = new TargetAuthor(name, affiliation);
	}

	public ReCiterArticle getTargetAuthorArticleIndexed() {
		return targetAuthorArticleIndexed;
	}

	public void setTargetAuthorArticleIndexed(ReCiterArticle targetAuthorArticleIndexed) {
		this.targetAuthorArticleIndexed = targetAuthorArticleIndexed;
	}
}
