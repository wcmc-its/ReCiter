package reciter.algorithm.evidence.boardcertification;

import java.util.List;

import reciter.algorithm.evidence.Strategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class BoardCertificationStrategyContext {
	private final Strategy strategy;
	
	public BoardCertificationStrategyContext(Strategy strategy) {
		this.strategy = strategy;
	}

	double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticles, targetAuthor);
	}
}
