package reciter.algorithm.evidence.targetauthor.name;

import java.util.List;

import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategy;
import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class RemoveByNameStrategyContext implements RemoveReCiterArticleStrategyContext {

	private final RemoveReCiterArticleStrategy strategy;
	
	public RemoveByNameStrategyContext(RemoveReCiterArticleStrategy strategy) {
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
