package main.reciter.model.article;

/**
 * ReCiterArticle title field.
 * @author jil3004
 *
 */
public class ReCiterArticleTitle {
	
	private final String title;

	public ReCiterArticleTitle(String title) {
		this.title = title;
	}
	public boolean exist() {
		return title != null;
	}
	public String getTitle() {
		return title;
	}	
}
