package reciter.algorithm.evidence.article.feedbackevidence.strategy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.engine.analysis.evidence.AcceptedRejectedEvidence;
import reciter.engine.analysis.evidence.FeedbackEvidence;
import reciter.model.article.ReCiterArticle;

/**
 * @author mrj4001
 * This class is to populate FeedbackEvidence for all articles
 */
public class FeedbackEvidenceStrategy extends AbstractReCiterArticleStrategy {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(FeedbackEvidenceStrategy.class);

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
				FeedbackEvidence feedbackEvidence = new FeedbackEvidence();
				feedbackEvidence.setFeedbackScoreCites(reCiterArticle.getCitesFeedbackScore());
				feedbackEvidence.setFeedbackScoreCoAuthorName(reCiterArticle.getCoAuthorNameFeedbackScore());
				feedbackEvidence.setFeedbackScoreEmail(reCiterArticle.getEmailFeedbackScore());
				feedbackEvidence.setFeedbackScoreInstitution(reCiterArticle.getInstitutionFeedbackScore());
				feedbackEvidence.setFeedbackScoreJournal(reCiterArticle.getJournalFeedackScore());
				feedbackEvidence.setFeedbackScoreJournalSubField(reCiterArticle.getJournalSubFieldFeedbackScore());
				feedbackEvidence.setFeedbackScoreKeyword(reCiterArticle.getKeywordFeedackScore());
				feedbackEvidence.setFeedbackScoreOrcid(reCiterArticle.getOrcidFeedbackScore());
				feedbackEvidence.setFeedbackScoreOrcidCoAuthor(reCiterArticle.getOrcidCoAuthorFeedbackScore());
				feedbackEvidence.setFeedbackScoreOrganization(reCiterArticle.getOrganizationFeedbackScore());
				feedbackEvidence.setFeedbackScoreTargetAuthorName(reCiterArticle.getTargetAuthorNameFeedbackScore());
				feedbackEvidence.setFeedbackScoreYear(reCiterArticle.getYearFeedbackScore());
				
				
				
				if(feedbackEvidence != null) {
					slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + feedbackEvidence);
					reCiterArticle.setFeedbackEvidence(feedbackEvidence);
				}
			});
		return 0;
	}

}
