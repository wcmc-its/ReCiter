package reciter.algorithm.evidence.email;

import java.util.List;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface Strategy {

	double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor);
	double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor);
}
