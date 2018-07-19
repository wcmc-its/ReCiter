package reciter.algorithm.evidence.article.standardizedscore.strategy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.algorithm.evidence.article.acceptedrejected.strategy.AcceptedRejectedStrategy;
import reciter.model.article.ReCiterArticle;

public class StandardScoreStrategy extends AbstractReCiterArticleStrategy {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(AcceptedRejectedStrategy.class);

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles) {
		reCiterArticles.stream().forEach(reCiterArticle -> {
			
		});
		return 0;
	}

}
