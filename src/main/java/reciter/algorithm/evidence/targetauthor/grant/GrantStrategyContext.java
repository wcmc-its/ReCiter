package reciter.algorithm.evidence.targetauthor.grant;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class GrantStrategyContext implements TargetAuthorStrategyContext {
	private final TargetAuthorStrategy strategy;
	
	public GrantStrategyContext(TargetAuthorStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticles, targetAuthor);
	}

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticle, targetAuthor);
	}
}
