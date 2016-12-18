package reciter.algorithm.evidence.article.citation;

import java.util.List;

import reciter.algorithm.evidence.article.ReCiterArticleStrategy;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.model.article.ReCiterArticle;

public class CitationStrategyContext implements ReCiterArticleStrategyContext {

	private final ReCiterArticleStrategy strategy;
	private double strategyScore;
	
	public CitationStrategyContext(ReCiterArticleStrategy strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		strategyScore = strategy.executeStrategy(reCiterArticle, otherReCiterArticle);
		return strategyScore;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		return strategy.executeStrategy(reCiterArticles, otherReCiterArticle);
	}
}
