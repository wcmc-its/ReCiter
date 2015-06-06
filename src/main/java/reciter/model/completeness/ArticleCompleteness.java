package reciter.model.completeness;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;

public interface ArticleCompleteness {
	public double computeCompleteness(ReCiterArticle article, ReCiterAuthor target);
}
