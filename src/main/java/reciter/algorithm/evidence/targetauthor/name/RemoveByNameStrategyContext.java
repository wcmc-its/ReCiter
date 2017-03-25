package reciter.algorithm.evidence.targetauthor.name;

import java.util.List;

import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategy;
import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public class RemoveByNameStrategyContext implements RemoveReCiterArticleStrategyContext {

	private final RemoveReCiterArticleStrategy strategy;
	
	public RemoveByNameStrategyContext(RemoveReCiterArticleStrategy strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		return strategy.executeStrategy(reCiterArticle, identity);
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		return strategy.executeStrategy(reCiterArticles, identity);
	}
}
