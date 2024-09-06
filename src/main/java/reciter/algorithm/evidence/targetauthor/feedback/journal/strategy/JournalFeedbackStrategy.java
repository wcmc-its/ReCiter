package reciter.algorithm.evidence.targetauthor.feedback.journal.strategy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterFeedbackScoreArticle;
import reciter.model.identity.Identity;

public class JournalFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(JournalFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackJournalsListMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleJournalsMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {

		StopWatch stopWatchforArticleFeedbackScore = new StopWatch("Journal");
		stopWatchforArticleFeedbackScore.start("Journal");
		try {
			slf4jLogger.info("reCiterArticles size:", reCiterArticles.size());
			
			
			
			// Count articles based on status per journal title
	        Map<String, Map<Integer, Long>> journalTitleCountByArticleStatus = reCiterArticles.stream()
	        	.filter(article -> article!=null && article.getJournal()!=null  && article.getJournal().getJournalTitle()!=null && !article.getJournal().getJournalTitle().isEmpty())	
	            .collect(Collectors.groupingBy(
	                article -> article.getJournal().getJournalTitle(),  // Group by journal title
	                Collectors.groupingBy(
	                    ReCiterArticle::getGoldStandard,    // Group by status
	                    Collectors.counting()   // Count articles
	                )
	            ));

	        // Print the result
	        journalTitleCountByArticleStatus.forEach((journalTitle, statusCounts) -> {
	            System.out.println("Journal: " + journalTitle);
	            statusCounts.forEach((status, count) -> 
	                System.out.println("Status: " + status + ", Count: " + count)
	            );
	        });

	        reCiterArticles.stream()
	        .filter(article -> article!=null && article.getJournal()!=null  && article.getJournal().getJournalTitle()!=null && !article.getJournal().getJournalTitle().isEmpty())	
			 	.forEach(article -> {
			 	
			 	feedbackJournalsListMap = new HashMap<>();
			 	int countAccepted = 0;
			 	int countRejected = 0;
				double scoreAll = 0.0;
				double scoreWithout1Accepted = 0.0;
				double scoreWithout1Rejected = 0.0;
				
				     if(journalTitleCountByArticleStatus !=null && journalTitleCountByArticleStatus.size() > 0 &&journalTitleCountByArticleStatus.containsKey(article.getJournal().getJournalTitle()) && journalTitleCountByArticleStatus.get(article.getJournal().getJournalTitle().trim()).containsKey(ACCEPTED))
				    	 countAccepted = Math.toIntExact(journalTitleCountByArticleStatus.get(article.getJournal().getJournalTitle().trim()).get(ACCEPTED).longValue());
					 if(journalTitleCountByArticleStatus!=null && journalTitleCountByArticleStatus.size()>0 && journalTitleCountByArticleStatus.containsKey(article.getJournal().getJournalTitle()) && journalTitleCountByArticleStatus.get(article.getJournal().getJournalTitle().trim()).containsKey(REJECTED))
						 countRejected = Math.toIntExact(journalTitleCountByArticleStatus.get(article.getJournal().getJournalTitle().trim()).get(REJECTED).longValue());
							
					if(countAccepted > 0 || countRejected > 0)
					{	
						scoreAll = computeScore(countAccepted, countRejected);
						scoreWithout1Accepted = computeScore(countAccepted > 0 ? countAccepted - 1 : countAccepted,
								countRejected);
						scoreWithout1Rejected = computeScore(countAccepted,
								countRejected > 0 ? countRejected - 1 : countRejected);
	
						
						ReCiterArticleFeedbackScore feedbackJournal = populateArticleFeedbackScore(article.getArticleId(),article.getJournal().getJournalTitle(),
																								   countAccepted,countRejected,
																								   scoreAll,scoreWithout1Accepted,
																								   scoreWithout1Rejected,article.getGoldStandard(),null);	
	
						feedbackJournalsListMap.computeIfAbsent(article.getJournal().getJournalTitle().trim(), k -> new ArrayList<>()).add(feedbackJournal);
						
				
						feedbackJournalsListMap.entrySet().stream()
								.filter(entry -> entry.getKey() != null && entry.getValue() != null)
								.sorted(Map.Entry.comparingByKey())
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
										(oldValue, newValue) -> oldValue, // merge function
										LinkedHashMap::new // to maintain insertion order
								));
						articleJournalsMap.put(article.getArticleId(), feedbackJournalsListMap);
						totalScoresByArticleMap.putIfAbsent(article.getArticleId(), feedbackJournal.getFeedbackScore());
						article.setJournalFeedackScore(feedbackJournal.getFeedbackScore());	
						String exportedJournalFeedackScore = decimalFormat.format(feedbackJournal.getFeedbackScore()); 
						System.out.println("Exported Journal article Score***************"+exportedJournalFeedackScore);
						article.setExportedJournalFeedackScore(exportedJournalFeedackScore);
				 }
			});
			 
			 if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

					// Printing using forEach
					slf4jLogger.info("********STARTING OF ARTICLE JOURNAL SCORING********************");
					if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

						String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
								"subscoreType1", "subscoreValue", "subScoreIndividualScore" };
						exportItemLevelFeedbackScores(identity.getUid(), "Journal", csvHeaders, articleJournalsMap);

					}
					slf4jLogger.info("********END OF THE ARTICLE JOURNAL SCORING********************\n");
				} else {
					slf4jLogger.info("********NO FEEDBACK SCORE FOR THE JOURNAL SECTION********************\n");
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		stopWatchforArticleFeedbackScore.stop();
			slf4jLogger.info(stopWatchforArticleFeedbackScore.getId() + " took "
					+ stopWatchforArticleFeedbackScore.getTotalTimeSeconds() + "s"); 

		return 0.0;
	}

	
}
