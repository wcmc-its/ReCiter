package reciter.algorithm.evidence.targetauthor;

import java.util.List;

import reciter.algorithm.evidence.StrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface TargetAuthorStrategyContext extends StrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor);	
	double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor);
}
