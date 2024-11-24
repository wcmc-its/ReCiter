package reciter.algorithm.evidence.article.feedbackevidence.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
	protected DecimalFormat decimalFormat = new DecimalFormat("#.###");
	
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
				if (reCiterArticle.getCitesFeedbackScore() != 0) {
					feedbackEvidence.setFeedbackScoreCites(reCiterArticle.getCitesFeedbackScore() * 100);
				}
				if (reCiterArticle.getCoAuthorNameFeedbackScore() != 0) {
					feedbackEvidence.setFeedbackScoreCoAuthorName(reCiterArticle.getCoAuthorNameFeedbackScore() * 100);
				}
				if (reCiterArticle.getEmailFeedbackScore() != 0) {
					feedbackEvidence.setFeedbackScoreEmail(reCiterArticle.getEmailFeedbackScore() * 100);
				}
				if (reCiterArticle.getInstitutionFeedbackScore() != 0) {
					feedbackEvidence.setFeedbackScoreInstitution(reCiterArticle.getInstitutionFeedbackScore() * 100);
				}
				if (reCiterArticle.getJournalFeedackScore() != 0) {
					feedbackEvidence.setFeedbackScoreJournal(reCiterArticle.getJournalFeedackScore() * 100);
				}
				if (reCiterArticle.getJournalSubFieldFeedbackScore() != 0) {
					feedbackEvidence
							.setFeedbackScoreJournalSubField(reCiterArticle.getJournalSubFieldFeedbackScore() * 100);
				}
				if (reCiterArticle.getKeywordFeedackScore() != 0) {
					feedbackEvidence.setFeedbackScoreKeyword(reCiterArticle.getKeywordFeedackScore() * 100);
				}
				if (reCiterArticle.getOrcidFeedbackScore() != 0) {
					feedbackEvidence.setFeedbackScoreOrcid(reCiterArticle.getOrcidFeedbackScore() * 100);
				}
 
				if (reCiterArticle.getOrcidCoAuthorFeedbackScore() != 0) {
					feedbackEvidence
							.setFeedbackScoreOrcidCoAuthor(reCiterArticle.getOrcidCoAuthorFeedbackScore() * 100);
				}
				if (reCiterArticle.getOrganizationFeedbackScore() != 0) {
					feedbackEvidence.setFeedbackScoreOrganization(reCiterArticle.getOrganizationFeedbackScore() * 100);
				}
				if (reCiterArticle.getTargetAuthorNameFeedbackScore() != 0) {
					feedbackEvidence
							.setFeedbackScoreTargetAuthorName(reCiterArticle.getTargetAuthorNameFeedbackScore() * 100);
				}
				if (reCiterArticle.getYearFeedbackScore() != 0) {
					feedbackEvidence.setFeedbackScoreYear(reCiterArticle.getYearFeedbackScore() * 100);
				}
				
				
				if(feedbackEvidence != null) {
					//slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + feedbackEvidence);
					reCiterArticle.setFeedbackEvidence(feedbackEvidence);
				}
			});
		return 0;
	}
	//Do not delete. Probably will be used in a near future
	 private double truncate(double value, int decimalPlaces) {
		return BigDecimal.valueOf(value)
         .setScale(decimalPlaces, RoundingMode.DOWN)
         .doubleValue();
	       
	    }
}
