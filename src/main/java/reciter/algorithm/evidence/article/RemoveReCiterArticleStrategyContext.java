package reciter.algorithm.evidence.article;

import java.util.List;

import reciter.algorithm.evidence.StrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface RemoveReCiterArticleStrategyContext extends StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor);
}
