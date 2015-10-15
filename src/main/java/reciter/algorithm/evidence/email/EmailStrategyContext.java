package reciter.algorithm.evidence.email;

import java.util.List;

import reciter.algorithm.evidence.Strategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class EmailStrategyContext {
	private final Strategy strategy;
	
	public EmailStrategyContext(Strategy strategy) {
		this.strategy = strategy;
	}
	
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticle, targetAuthor);
	}
	
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticles, targetAuthor);
	}
}
