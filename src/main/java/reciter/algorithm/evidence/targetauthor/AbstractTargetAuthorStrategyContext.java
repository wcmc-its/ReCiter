package reciter.algorithm.evidence.targetauthor;

import java.util.List;

import reciter.database.mongo.model.Identity;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;

public abstract class AbstractTargetAuthorStrategyContext implements TargetAuthorStrategyContext {

	private TargetAuthorStrategy strategy;
	private double strategyScore;

	public AbstractTargetAuthorStrategyContext(TargetAuthorStrategy strategy) {
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
	
	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		strategy.populateFeature(reCiterArticle, identity, feature);
	}
	
	protected double getStrategyScore() {
		return strategyScore;
	}
	
	protected void setStrategyScore(double strategyScore) {
		this.strategyScore = strategyScore;
	}

}
