package reciter.model.article.completeness;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;

public interface ArticleCompleteness {
	public double computeCompleteness(ReCiterArticle article, ReCiterAuthor target);
}
