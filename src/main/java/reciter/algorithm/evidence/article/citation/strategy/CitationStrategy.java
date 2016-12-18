package reciter.algorithm.evidence.article.citation.strategy;

import java.util.List;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;

public class CitationStrategy extends AbstractReCiterArticleStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {

		// check citation references in both ways.
		if (checkCitationReference(reCiterArticle, otherReCiterArticle) == 0) {
			return checkCitationReference(otherReCiterArticle, reCiterArticle);
		} else {
			return 1;
		}
	}

	private double checkCitationReference(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		if (reCiterArticle.getCommentsCorrectionsPmids() != null && 
				reCiterArticle.getCommentsCorrectionsPmids().contains(otherReCiterArticle.getArticleId())) {

			reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[article " + reCiterArticle.getArticleId() + 
					"(" + reCiterArticle.getGoldStandard() + ")" + 
					" references article " + otherReCiterArticle.getArticleId() + "(" + otherReCiterArticle.getGoldStandard() + ")]");
			return 1;
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

}
