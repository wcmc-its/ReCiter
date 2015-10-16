package reciter.algorithm.evidence;

import java.util.List;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor);	
	double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor);
}
