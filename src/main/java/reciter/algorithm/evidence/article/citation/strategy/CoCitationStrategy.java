package reciter.algorithm.evidence.article.citation.strategy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;

public class CoCitationStrategy extends AbstractReCiterArticleStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		return checkCoCitationReference(reCiterArticle, otherReCiterArticle);
	}

	private double checkCoCitationReference(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		int count = 0;
		Set<Integer> sharedPmids = new HashSet<Integer>();
		if (reCiterArticle.getCommentsCorrectionsPmids() != null && otherReCiterArticle.getCommentsCorrectionsPmids() != null) {

			Set<Integer> pmids = reCiterArticle.getCommentsCorrectionsPmids();
			for (int pmid : pmids) {
				if (otherReCiterArticle.getCommentsCorrectionsPmids().contains(pmid)) {
					count++;
					sharedPmids.add(pmid);
				}
			}
		}

		if (count > 0) {
			reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[article " + reCiterArticle.getArticleId() +
					" and article " + otherReCiterArticle.getArticleId() + " share " + count + " references and those are + " + sharedPmids + "], ");
		}
		return count;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

}
