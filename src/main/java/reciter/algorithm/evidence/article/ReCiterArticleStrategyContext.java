package reciter.algorithm.evidence.article;

import java.util.List;

import reciter.model.article.ReCiterArticle;

public interface ReCiterArticleStrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle);
}
