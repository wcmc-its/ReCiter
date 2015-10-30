package reciter.algorithm.evidence.article;

import java.util.List;

import reciter.algorithm.evidence.StrategyContext;
import reciter.model.article.ReCiterArticle;

public interface ReCiterArticleStrategyContext extends StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle);
}
