package reciter.algorithm.evidence.targetauthor.degree;

import java.util.List;

import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategy;
import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class DegreeStrategyContext implements RemoveReCiterArticleStrategyContext {
	private final RemoveReCiterArticleStrategy strategy;
	private double strategyScore;
	
	public DegreeStrategyContext(RemoveReCiterArticleStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		strategyScore = strategy.executeStrategy(reCiterArticle, targetAuthor);
		return strategyScore;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticles, targetAuthor);
	}
}
