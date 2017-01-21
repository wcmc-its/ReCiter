package reciter.algorithm.evidence.article.citation.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticle.CoCitation;

/**
 * Cites: A (positive) and B (candidate article)
 * CitedBy:
 * @author Jie
 *
 */
public class CoCitationStrategy extends AbstractReCiterArticleStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		return checkCoCitationReference(reCiterArticle, otherReCiterArticle);
	}

	private double checkCoCitationReference(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		int count = 0;
		Set<Long> sharedPmids = new HashSet<>();
		if (reCiterArticle.getCommentsCorrectionsPmids() != null && otherReCiterArticle.getCommentsCorrectionsPmids() != null) {

			Set<Long> pmids = reCiterArticle.getCommentsCorrectionsPmids();
			for (long pmid : pmids) {
				if (otherReCiterArticle.getCommentsCorrectionsPmids().contains(pmid)) {
					count++;
					sharedPmids.add(pmid);
				}
			}
		}

		reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
				"[article " + reCiterArticle.getArticleId() + "(" + reCiterArticle.getGoldStandard() + ") " +
				" and article " + otherReCiterArticle.getArticleId() + "(" + otherReCiterArticle.getGoldStandard() + ")" + 
				" share " + count + " references and those are + " + sharedPmids + "], ");
		
		if (count > 0) {
			CoCitation coCitation = new CoCitation();
			coCitation.setPmid(reCiterArticle.getArticleId());
			coCitation.setPmids(new ArrayList<>(sharedPmids));
			reCiterArticle.getCoCitation().add(coCitation);
			otherReCiterArticle.getCoCitation().add(coCitation);
		}
		return count;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

}
