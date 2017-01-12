package reciter.algorithm.evidence.article;

import java.util.List;

import reciter.algorithm.evidence.StrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public interface RemoveReCiterArticleStrategyContext extends StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, Identity identity);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity);
}