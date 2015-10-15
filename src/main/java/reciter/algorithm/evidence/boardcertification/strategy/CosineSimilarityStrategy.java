package reciter.algorithm.evidence.boardcertification.strategy;

import java.util.List;

import reciter.algorithm.evidence.AbstractStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;


public class CosineSimilarityStrategy extends AbstractStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		List<String> boardCertifications = targetAuthor.getBoardCertifications();
		
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		
		return 0;
	}
}
