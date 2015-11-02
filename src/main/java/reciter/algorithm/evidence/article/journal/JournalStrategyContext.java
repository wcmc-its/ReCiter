package reciter.algorithm.evidence.article.journal;

import java.util.List;

import reciter.algorithm.evidence.article.ReCiterArticleStrategy;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.model.article.ReCiterArticle;

public class JournalStrategyContext implements ReCiterArticleStrategyContext {

	private final ReCiterArticleStrategy strategy;
	
	public JournalStrategyContext(ReCiterArticleStrategy strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		return strategy.executeStrategy(reCiterArticle, otherReCiterArticle);
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		return strategy.executeStrategy(reCiterArticles, otherReCiterArticle);
	}
	
	

}
