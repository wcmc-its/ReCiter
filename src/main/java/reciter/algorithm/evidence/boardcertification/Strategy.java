package reciter.algorithm.evidence.boardcertification;

import java.util.List;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface Strategy {

	double executeStrategy(List<String> boardCertifications, ReCiterArticle article);
	double executeStrategy(List<ReCiterArticle> reCiterArticle, TargetAuthor targetAuthor);
}
