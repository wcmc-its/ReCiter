package reciter.algorithm.evidence.article;

import java.util.List;

import reciter.algorithm.evidence.StrategyContext;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;

public interface RemoveReCiterArticleStrategyContext extends StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, Identity identity);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity);
}