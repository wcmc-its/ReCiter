package reciter.algorithm.evidence.boardcertification;

import java.util.List;

import reciter.model.article.ReCiterArticle;

public class BoardCertificationStrategyContext {
	private final Strategy strategy;
	
	public BoardCertificationStrategyContext(Strategy strategy) {
		this.strategy = strategy;
	}

	public double executeStrategy(List<String> boardCertifications, ReCiterArticle article) {
		return strategy.executeStrategy(boardCertifications, article);
	}
}
