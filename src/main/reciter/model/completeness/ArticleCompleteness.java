package main.reciter.model.completeness;

import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.author.ReCiterAuthor;

public interface ArticleCompleteness {
	public double computeCompleteness(ReCiterArticle article, ReCiterAuthor target);
}
