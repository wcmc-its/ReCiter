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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	// Helper method to compute score
	protected double computeScore(int countAccepted, int countRejected) {
		return (1 / (1 + Math.exp(-(countAccepted - countRejected) / (Math.sqrt(countAccepted + countRejected) + 1))))
				- 0.5;
	}
	
	protected double determineFeedbackScore(int goldStandard, double scoreWithout1Accepted, double scoreWithout1Rejected, double scoreAll) 
	{
		// Create a DecimalFormat instance with the desired pattern
     //   DecimalFormat df = new DecimalFormat("#.###");
        double feedbackScore=0.0; 
       
        
        try {
        	if(goldStandard == 1)
        	{	
        		// Format the double value
                //String formattedValue = decimalFormat.format(scoreWithout1Accepted);
                //feedbackScore = decimalFormat.parse(formattedValue).doubleValue();
        	   //System.out.println("coming into accepted in determinedFeedbackScore"+feedbackScore);
        		feedbackScore = scoreWithout1Accepted;
        	}
        	else if(goldStandard == -1)
        	{
        		//BigDecimal bigDecimalValue = new BigDecimal(Double.toString(scoreWithout1Rejected));
        		 //feedbackScore = bigDecimalValue.setScale(6, RoundingMode.HALF_UP);
        		//System.out.println("coming into rejected in determinedFeedbackScore"+feedbackScore);
        		 // Format the double value
               // String formattedValue = decimalFormat.format(scoreWithout1Rejected);
               // feedbackScore = decimalFormat.parse(formattedValue).doubleValue();
        		feedbackScore = scoreWithout1Rejected;
        	}
        	else
        	{
        		//BigDecimal bigDecimalValue = new BigDecimal(Double.toString(scoreAll));
        		// feedbackScore = bigDecimalValue.setScale(6, RoundingMode.HALF_UP);
        		//System.out.println("coming into else in determinedFeedbackScore"+feedbackScore);
    		  // String formattedValue = decimalFormat.format(scoreAll);
    		  // feedbackScore = decimalFormat.parse(formattedValue).doubleValue();
        		feedbackScore = scoreAll;
        	}
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return feedbackScore;
	    
	}

	protected void exportConsolidatedFeedbackScores(String personIdentifier,String feedbackScoreFieldName,String[] csvHeaders, Map<Long,Double> scoreMap)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
		
		
		// Get current date and time
		LocalDateTime now = LocalDateTime.now();
		
		// Format the current date and time to a safe string for file names
		String timestamp = now.format(formatter);

		Path filePath = Paths.get(timestamp + "-" + personIdentifier + "-feedbackScoring-consolidated.csv");
		
		try ( // Create BufferedWriter
				BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
				// Create CSVPrinter
				CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create(CSVFormat.DEFAULT)
		                             .setHeader(csvHeaders)
		                             .build())) {	
			scoreMap.forEach((articleId, score) -> {
				// System.out.println(" Inner Key: " + articleId + "Score:" + score);
				try {
					if(score != 0.0)
						csvPrinter.printRecord(personIdentifier, articleId, feedbackScoreFieldName, decimalFormat.format(score));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});
			csvPrinter.flush();
			// byte[] csvData = baos.toByteArray();

			// Upload CSV to S3
			/*
			 * String bucketName = "your-bucket-name"; String keyName =
			 * "data/total_scores.csv"; try (ByteArrayInputStream bais = new
			 * ByteArrayInputStream(csvData)) { PutObjectRequest putObjectRequest = new
			 * PutObjectRequest(bucketName, keyName, bais, null);
			 * s3Client.putObject(putObjectRequest); }
			 * 
			 * System.out.println("CSV file uploaded successfully to S3 bucket.");
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected void exportItemLevelFeedbackScores(String personIdentifier,String feedbackScoreFieldName,String[] csvHeaders, Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> feedbackItemScoreMap)
	{
		// Define a DateTimeFormatter for safe file name format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");

		// Get current date and time
		LocalDateTime now = LocalDateTime.now();

		// Format the current date and time to a safe string for file names
		String timestamp = now.format(formatter);

		Path filePath = Paths.get(timestamp + "-" + personIdentifier + "-feedbackScoring-"+feedbackScoreFieldName+"-itemLevel.csv");

		try ( // Create BufferedWriter
				BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()));
				// Create CSVPrinter
				CSVPrinter csvPrinter = new CSVPrinter(writer,CSVFormat.Builder.create(CSVFormat.DEFAULT)
                        .setHeader(csvHeaders).build())) {

			feedbackItemScoreMap.forEach((outerKey, innerMap) -> {
				innerMap.forEach((innerKey, list) -> {
					list.forEach(article -> {
						try {
								if(article.getFeedbackScore() != 0.0)
								{
									System.out.println("Feedback Score before conversion in Export"+article.getFeedbackScore());
									String feedbackScoreStr = decimalFormat.format(article.getFeedbackScore());
									System.out.println("FeedbackScore str****************"+feedbackScoreStr);
									String feedbackScore = article.getExportedFeedbackScore();
									System.out.println("Feedback Score After conversion in Export"+ feedbackScore);
									csvPrinter.printRecord(personIdentifier, article.getArticleId(),
											article.getAcceptedCount(), article.getRejectedCount(), feedbackScoreFieldName,
											article.getFeedbackScoreFieldValue(), feedbackScore,article.getGoldStandard());
								}
							} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					});
				});
			});

			csvPrinter.flush();
			// byte[] csvData = baos.toByteArray();

			// Upload CSV to S3
			/*
			 * String bucketName = "your-bucket-name"; String keyName =
			 * "data/total_scores.csv"; try (ByteArrayInputStream bais = new
			 * ByteArrayInputStream(csvData)) { PutObjectRequest putObjectRequest = new
			 * PutObjectRequest(bucketName, keyName, bais, null);
			 * s3Client.putObject(putObjectRequest); }
			 * 
			 * System.out.println("CSV file uploaded successfully to S3 bucket.");
			 */
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	protected ReCiterArticleFeedbackScore populateArticleFeedbackScore(long articleId,String feedbackScoreFieldValue, int countAccepted,int countRejected,double scoreAll,double scoreWithout1Accepted,double scoreWithout1Rejected, int goldStandard,String exportedFeedbackScore)
	{
		ReCiterArticleFeedbackScore reciterArticleFeedbackScore = new ReCiterArticleFeedbackScore();
		//feedbackKeyword.setPersonIdentifier(identity.getUid());
		reciterArticleFeedbackScore.setGoldStandard(goldStandard);
		reciterArticleFeedbackScore.setArticleId(articleId);
		reciterArticleFeedbackScore.setFeedbackScoreFieldValue(feedbackScoreFieldValue);
		reciterArticleFeedbackScore.setAcceptedCount(countAccepted);
		reciterArticleFeedbackScore.setRejectedCount(countRejected);
		reciterArticleFeedbackScore.setScoreAll(scoreAll);
		reciterArticleFeedbackScore.setScoreWithout1Accepted(scoreWithout1Accepted);
		reciterArticleFeedbackScore.setScoreWithout1Rejected(scoreWithout1Rejected);
		
		if(scoreWithout1Accepted > 0  || scoreWithout1Rejected > 0)
		{	
			reciterArticleFeedbackScore.setFeedbackScore(determineFeedbackScore(goldStandard,
											scoreWithout1Accepted, scoreWithout1Rejected, scoreAll));
		}
		else
		{	
			
			reciterArticleFeedbackScore.setFeedbackScore(determineFeedbackScore(0,
					0.0, 0.0, scoreAll));	
		}
		reciterArticleFeedbackScore.setExportedFeedbackScore(exportedFeedbackScore);
		return reciterArticleFeedbackScore;
	}
}
