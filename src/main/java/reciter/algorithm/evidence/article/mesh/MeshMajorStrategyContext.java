package reciter.algorithm.evidence.article.mesh;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class MeshMajorStrategyContext implements TargetAuthorStrategyContext {

private final TargetAuthorStrategy strategy;
	
	public MeshMajorStrategyContext(TargetAuthorStrategy strategy) {
		this.strategy = strategy;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticle, targetAuthor);
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return strategy.executeStrategy(reCiterArticles, targetAuthor);
	}
	
}