package reciter.algorithm.evidence.targetauthor;

import java.util.List;

import reciter.algorithm.evidence.StrategyContext;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public interface TargetAuthorStrategyContext extends StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, Identity identity);	
	double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity);
	void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature);
}
