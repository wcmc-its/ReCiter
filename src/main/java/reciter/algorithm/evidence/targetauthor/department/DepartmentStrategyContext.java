package reciter.algorithm.evidence.targetauthor.department;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;

public class DepartmentStrategyContext implements TargetAuthorStrategyContext {

	private final TargetAuthorStrategy strategy;
	private double strategyScore;
	
	public DepartmentStrategyContext(TargetAuthorStrategy strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		strategyScore = strategy.executeStrategy(reCiterArticle, identity);
		return strategyScore;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		return strategy.executeStrategy(reCiterArticles, identity);
	}
}
