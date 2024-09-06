package reciter.algorithm.evidence.targetauthor.feedback.year.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.article.ReCiterFeedbackScoreArticle;
import reciter.model.identity.Identity;

public class YearFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(YearFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackYearMap = new HashMap<>();
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleJournalsMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();
	private Integer minArticleYear=0;

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}
	
	public static int pickArticleYear(ReCiterArticle article) {
		int articleYear =0;
		if (article != null && article.getPublicationDateStandardized() != null
				&& !article.getPublicationDateStandardized().equalsIgnoreCase(""))
			articleYear = Integer.parseInt(article.getPublicationDateStandardized().substring(0, 4));
		else if (article != null && article.getDatePublicationAddedToEntrez() != null
				&& !article.getDatePublicationAddedToEntrez().equalsIgnoreCase(""))
			articleYear = Integer.parseInt(article.getDatePublicationAddedToEntrez().substring(0, 4));
		
		return articleYear;
	}
	
	private static Optional<Integer> parseYear(String yearString) {
        try {
            return yearString != null && !yearString.isEmpty()
                ? Optional.of(Integer.parseInt(yearString.substring(0, 4)))
                : Optional.empty();
        } catch (NumberFormatException e) {
            // Handle cases where the yearString is not a valid integer
            return Optional.empty();
        }
    }

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		StopWatch stopWatchforYearFeedback = new StopWatch("Year");
		stopWatchforYearFeedback.start("Year");
		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());
			
			
			 // Group by year and count based on status
	        Map<Integer, Map<Integer, Long>> yearCountByArticleStatus = reCiterArticles.stream()
	            .collect(Collectors.groupingBy(
	                article -> pickArticleYear(article), 
	                Collectors.groupingBy(
	                    ReCiterArticle::getGoldStandard,
	                    Collectors.counting()
	                )
	            ));

	        // Print the result
	        yearCountByArticleStatus.forEach((year, statusMap) -> {
	            System.out.println("Year: " + year);
	            statusMap.forEach((status, count) -> 
	                System.out.println("  Status " + status + ": " + count)
	            );
	        });
	        
	        // Find the minimum year
	        Optional<Integer> minYear = reCiterArticles.stream()
	            .filter(article -> article != null && article.getGoldStandard() == 1) // Filter out null and empty
	            .map(article -> {
	                try {
	                    return pickArticleYear(article); 
	                } catch (NumberFormatException e) {
	                    // Handle invalid integer format
	                    return 9999;
	                }
	            })
	            .filter(year -> year != null) // Filter out any parsing errors
	            .min(Integer::compareTo); // Find the minimum year

	        // Print the result
	        minYear.ifPresent(year -> System.out.println("Minimum year: " + year));

			
			reCiterArticles.stream()
						   .filter(article-> article!=null && (article.getPublicationDateStandardized()!=null || article.getDatePublicationAddedToEntrez()!=null))
						   .forEach(article -> {
							    int countAccepted = 0;
								int countRejected = 0;
								double scoreAll = 0.0;
								double scoreWithout1Accepted = 0.0;
								double scoreWithout1Rejected = 0.0;
								Integer articleYear = 0;
								
								articleYear = pickArticleYear(article);
								System.out.println("articleYear picked for processing***********"+articleYear);

							if (articleYear != null && articleYear != 0) {
								
								
							/*for (ReCiterArticle innerArticle : reCiterArticles) {
		
								Integer innerArticleYear = 0;
								if (innerArticle != null && innerArticle.getPublicationDateStandardized() != null
										&& !innerArticle.getPublicationDateStandardized().equalsIgnoreCase("")) {
									innerArticleYear = Integer
											.parseInt(innerArticle.getPublicationDateStandardized().substring(0, 4));
								} else if (innerArticle != null && innerArticle.getDatePublicationAddedToEntrez() != null
										&& !innerArticle.getDatePublicationAddedToEntrez().equalsIgnoreCase("")) {
									innerArticleYear = Integer
											.parseInt(innerArticle.getDatePublicationAddedToEntrez().substring(0, 4));
								}
								if(innerArticle!=null && innerArticle.getGoldStandard() == 1)
									listOfArticleYears.add(innerArticleYear);
								if (outerArticleYear != 0 && innerArticleYear != 0
										&& outerArticleYear.intValue() == innerArticleYear.intValue()) {
									if (innerArticle!=null && innerArticle.getGoldStandard() == 1) {
										countAccepted = countAccepted + 1;
									} else if (innerArticle!=null && innerArticle.getGoldStandard() == -1) {
										countRejected = countRejected + 1;
									}
									else
										countNull = countNull + 1;
								}
								
							}*/
								 if(yearCountByArticleStatus !=null && yearCountByArticleStatus.size() > 0 &&yearCountByArticleStatus.containsKey(articleYear) && yearCountByArticleStatus.get(articleYear).containsKey(ACCEPTED))
							    	 countAccepted = Math.toIntExact(yearCountByArticleStatus.get(articleYear).get(ACCEPTED).longValue());
							     if(yearCountByArticleStatus!=null && yearCountByArticleStatus.size()>0 && yearCountByArticleStatus.containsKey(articleYear) && yearCountByArticleStatus.get(articleYear).containsKey(REJECTED))
									 countRejected = Math.toIntExact(yearCountByArticleStatus.get(articleYear).get(REJECTED).longValue());
								
							
							 if(countAccepted > 0 && minArticleYear==0)
							 {	 
								 if(minYear.isPresent())
									 minArticleYear = minYear.get();
								 else
									 minArticleYear = 9999;
							 }
							 System.out.println("Minimum Article Year******************"+minArticleYear);
							
							scoreAll = calculateScore(articleYear, countAccepted, countRejected, minArticleYear);
							scoreWithout1Accepted = calculateScore(articleYear, countAccepted > 0 ? countAccepted - 1 : 0, countRejected, minArticleYear);
							scoreWithout1Rejected = calculateScore(articleYear, countAccepted, countRejected > 0 ? countRejected - 1 : 0, minArticleYear);
							
							
							ReCiterArticleFeedbackScore feedbackYear = populateArticleFeedbackScore(article.getArticleId(),Integer.toString(articleYear),
									   countAccepted,countRejected,
									   scoreAll,scoreWithout1Accepted,
									   scoreWithout1Rejected,article.getGoldStandard(),null);	

							feedbackYearMap.computeIfAbsent(Integer.toString(articleYear), k -> new ArrayList<>()).add(feedbackYear);
							
							feedbackYearMap.entrySet().stream()
							.filter(entry -> entry.getKey() != null && entry.getValue() != null)
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
									(oldValue, newValue) -> oldValue, // merge function
									LinkedHashMap::new // to maintain insertion order
							));
							feedbackYearMap.forEach((key,value)->{
								
								System.out.println("Article Year -> : "+ key);
								value.forEach(articleFeedbackYear -> System.out.println("YearMap contents"+articleFeedbackYear));
							});
							
							
							articleJournalsMap.put(article.getArticleId(), feedbackYearMap);
							totalScoresByArticleMap.putIfAbsent(article.getArticleId(), feedbackYear.getFeedbackScore());
							article.setYearFeedbackScore(feedbackYear.getFeedbackScore());	
							String exportedYearFeedbackScore = decimalFormat.format(feedbackYear.getFeedbackScore());
							System.out.println("Exported OrCid Year article Score***************"+exportedYearFeedbackScore);
							article.setExportedYearFeedbackScore(exportedYearFeedbackScore);		
						}
							
								
				});
			if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

				// Printing using forEach
				slf4jLogger.info("********STARTING OF ARTICLE YEAR SCORING********************");
				if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

					String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
							"subscoreType1", "subscoreValue", "subScoreIndividualScore","UserAssertion" };
					exportItemLevelFeedbackScores(identity.getUid(), "Year", csvHeaders, articleJournalsMap);

				}
				slf4jLogger.info("********END OF THE ARTICLE YEAR SCORING********************\n");
			} else {
				slf4jLogger.info("********NO FEEDBACK SCORE FOR THE YEAR SECTION********************\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	// Helper method to calculate score based on given formula
    private static double calculateScore(int value, int countAccepted, int countRejected, int firstYearValue) {
        double baseScore = -((firstYearValue - value) / 10.0) * (Math.sqrt(Math.max(countAccepted + countRejected, 1)) + 1);
        System.out.println("basescore for the article Year:-> " + value + "baseScore : -> " + baseScore);
        double score =  Math.min(Math.max(baseScore, -5.0), 0.0);
        System.out.println("Final Score :-> "+ score);
        return score;
    }
}
