package reciter.algorithm.evidence.targetauthor.internship;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;

public class InternshipAndResidenceStrategyContext implements TargetAuthorStrategyContext{
	private final TargetAuthorStrategy strategy;
	private double strategyScore;
	
	public InternshipAndResidenceStrategyContext(TargetAuthorStrategy strategy) {
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
