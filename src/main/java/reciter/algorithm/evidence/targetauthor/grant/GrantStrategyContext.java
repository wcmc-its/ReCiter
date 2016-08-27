package reciter.algorithm.evidence.targetauthor.grant;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;

public class GrantStrategyContext implements TargetAuthorStrategyContext {
	private final TargetAuthorStrategy strategy;
	private double strategyScore;
	
	public GrantStrategyContext(TargetAuthorStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		strategyScore = strategy.executeStrategy(reCiterArticles, identity);
		return strategyScore;
	}

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		return strategy.executeStrategy(reCiterArticle, identity);
	}
}
