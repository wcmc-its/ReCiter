package reciter.algorithm.evidence.feedback.targetauthor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.identity.Identity;

public class AbstractTargetAuthorFeedbackStrategy implements TargetAuthorFeedbackStrategy {

	protected final int ACCEPTED = 1;
	protected final int REJECTED = -1;
	protected DecimalFormat decimalFormat = new DecimalFormat("#.######");
	
	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		return 0;
	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		return 0;
	}
	
	// Helper method to compute score
	protected double computeScore(int countAccepted, int countRejected) {
		return (1 / (1 + Math.exp(-(countAccepted - countRejected) / (Math.sqrt(countAccepted + countRejected) + 1))))
				- 0.5;
	}
	
	protected double determineFeedbackScore(int goldStandard, double scoreWithout1Accepted, double scoreWithout1Rejected, double scoreAll) 
	{
	    double feedbackScore=0.0; 
        try {
        	if(goldStandard == 1)
        		feedbackScore = scoreWithout1Accepted;
        	else if(goldStandard == -1)
        		feedbackScore = scoreWithout1Rejected;
        	else
        		feedbackScore = scoreAll;
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feedbackScore;
	    
	}
	
	protected ReCiterArticleFeedbackScore populateArticleFeedbackScore(long articleId,String feedbackScoreFieldValue, int countAccepted,int countRejected,double scoreAll,double scoreWithout1Accepted,double scoreWithout1Rejected, int goldStandard,double feedbackScore,String exportedFeedbackScore,String feedbackScoreType)
	{
		ReCiterArticleFeedbackScore reciterArticleFeedbackScore = new ReCiterArticleFeedbackScore();
		reciterArticleFeedbackScore.setGoldStandard(goldStandard);
		reciterArticleFeedbackScore.setArticleId(articleId);
		reciterArticleFeedbackScore.setFeedbackScoreType(feedbackScoreType);
		reciterArticleFeedbackScore.setFeedbackScoreFieldValue(feedbackScoreFieldValue);
		reciterArticleFeedbackScore.setAcceptedCount(countAccepted);
		reciterArticleFeedbackScore.setRejectedCount(countRejected);
		reciterArticleFeedbackScore.setScoreAll(scoreAll);
		reciterArticleFeedbackScore.setScoreWithout1Accepted(scoreWithout1Accepted);
		reciterArticleFeedbackScore.setScoreWithout1Rejected(scoreWithout1Rejected);
		reciterArticleFeedbackScore.setFeedbackScore(feedbackScore);
		reciterArticleFeedbackScore.setExportedFeedbackScore(exportedFeedbackScore);
		return reciterArticleFeedbackScore;
	}
}
