package reciter.algorithm.evidence.targetauthor;

import java.util.List;

import reciter.algorithm.evidence.StrategyContext;
import reciter.database.mongo.model.Feature;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;

public interface TargetAuthorStrategyContext extends StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, Identity identity);	
	double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity);
	void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature);
}
