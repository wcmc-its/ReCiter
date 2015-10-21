package reciter.algorithm.evidence.targetauthor;

import java.util.List;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface TargetAuthorStrategyContext {

	double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor);	
	double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor);
}
