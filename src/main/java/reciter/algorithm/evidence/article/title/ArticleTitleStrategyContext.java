package reciter.algorithm.evidence.article.title;

import java.util.List;

import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class ArticleTitleStrategyContext implements RemoveReCiterArticleStrategyContext {

	private final RemoveReCiterArticleStrategyContext strategy;
	
	public ArticleTitleStrategyContext(RemoveReCiterArticleStrategyContext strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticle, targetAuthor);
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticles, targetAuthor);
	}	
}
