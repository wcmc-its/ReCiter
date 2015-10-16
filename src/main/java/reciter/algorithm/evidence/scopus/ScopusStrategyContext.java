package reciter.algorithm.evidence.scopus;

import java.util.List;

import reciter.algorithm.evidence.Strategy;
import reciter.algorithm.evidence.StrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class ScopusStrategyContext implements StrategyContext {
	private final Strategy strategy;
	
	public ScopusStrategyContext(Strategy strategy) {
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
