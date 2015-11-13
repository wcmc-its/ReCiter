package reciter.algorithm.evidence.article;

import java.util.List;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface RemoveReCiterArticleStrategy {

	double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor);
}
