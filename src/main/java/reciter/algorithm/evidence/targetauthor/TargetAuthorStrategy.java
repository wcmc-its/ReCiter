package reciter.algorithm.evidence.targetauthor;

import java.util.List;

import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public interface TargetAuthorStrategy {

	double executeStrategy(ReCiterArticle reCiterArticle, Identity identity);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity);
	void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature);
}
