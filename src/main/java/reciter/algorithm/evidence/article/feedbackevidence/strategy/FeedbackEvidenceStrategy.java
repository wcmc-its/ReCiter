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
				feedbackEvidence.setFeedbackScoreCites(truncate(reCiterArticle.getCitesFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreCoAuthorName(truncate(reCiterArticle.getCoAuthorNameFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreEmail(truncate(reCiterArticle.getEmailFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreInstitution(truncate(reCiterArticle.getInstitutionFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreJournal(truncate(reCiterArticle.getJournalFeedackScore()*100,3));
				feedbackEvidence.setFeedbackScoreJournalSubField(truncate(reCiterArticle.getJournalSubFieldFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreKeyword(truncate(reCiterArticle.getKeywordFeedackScore()*100,3));
				feedbackEvidence.setFeedbackScoreOrcid(truncate(reCiterArticle.getOrcidFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreOrcidCoAuthor(truncate(reCiterArticle.getOrcidCoAuthorFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreOrganization(truncate(reCiterArticle.getOrganizationFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreTargetAuthorName(truncate(reCiterArticle.getTargetAuthorNameFeedbackScore()*100,3));
				feedbackEvidence.setFeedbackScoreYear(truncate(reCiterArticle.getYearFeedbackScore()*100,3));
				
				
				
				if(feedbackEvidence != null) {
					slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + feedbackEvidence);
					reCiterArticle.setFeedbackEvidence(feedbackEvidence);
				}
			});
		return 0;
	}
	 private double truncate(double value, int decimalPlaces) {
		return BigDecimal.valueOf(value)
         .setScale(decimalPlaces, RoundingMode.DOWN)
         .doubleValue();
	       
	    }
}
