package reciter.algorithm.evidence;

import reciter.model.article.ReCiterArticle;

public interface ReCiterArticleStrategy {

	double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle);
}
