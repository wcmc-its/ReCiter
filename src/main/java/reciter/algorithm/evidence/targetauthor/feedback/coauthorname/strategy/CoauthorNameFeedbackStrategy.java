package reciter.algorithm.evidence.targetauthor.feedback.coauthorname.strategy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;


public class CoauthorNameFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(CoauthorNameFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> coauthorNameListMap = null;
	List<ReCiterAuthor> listOfAuthors = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleCoAuthorsMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();

	private static final Pattern PATTERN_1 = Pattern.compile("^[A-Z] [A-Z]$");
	private static final Pattern PATTERN_2 = Pattern.compile("^[A-Z] [A-Z][a-z]");

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	private static String processAuthor(ReCiterAuthor author) {
		String authorFirstname = author.getAuthorName().getFirstName();
		String authorLastname = author.getAuthorName().getLastName();

		if (PATTERN_1.matcher(authorFirstname).matches() || PATTERN_2.matcher(authorFirstname).matches()) {
			return authorFirstname + " " + authorLastname;
		} else {
			return authorFirstname.split(" ")[0] + " " + authorLastname;
		}
	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {

		StopWatch stopWatchforCoAuthorFeedback = new StopWatch("CoAuthorName");
		stopWatchforCoAuthorFeedback.start("CoAuthorName");
		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());

			// Group articles by gold standard
	        Map<Integer, List<ReCiterArticle>> groupedByGoldStandard = reCiterArticles.stream()
	            .collect(Collectors.groupingBy(ReCiterArticle::getGoldStandard));

	        // Get lists of articles based on gold standard
	        List<ReCiterArticle> acceptedArticles = groupedByGoldStandard.getOrDefault(1, Collections.emptyList());
	        List<ReCiterArticle> rejectedArticles = groupedByGoldStandard.getOrDefault(-1, Collections.emptyList());

	        Map<String, Long> acceptArticlesCountByCoAuthor =  acceptedArticles.stream()
            .flatMap(article -> article.getArticleCoAuthors().getAuthors().stream())
            .filter(author-> !author.isTargetAuthor())
            .map(author->processAuthor(author))
            .collect(Collectors.groupingBy(
                Function.identity(),
                Collectors.counting()
            ));
	        System.out.println("Accepted Articles size:"+acceptArticlesCountByCoAuthor.size());
	        //acceptArticlesCountByCoAuthor.forEach((key, value) -> System.out.println("Accepted and Rejected Articles:"+ key + ": " + value));
	        
       
	        Map<String, Long> rejectedArticlesCountByCoAuthor =  rejectedArticles.stream()
	                .flatMap(article -> article.getArticleCoAuthors().getAuthors().stream())
	                .filter(author-> !author.isTargetAuthor())
	                .map(author->processAuthor(author))
	                .collect(Collectors.groupingBy(
	                    Function.identity(),
	                    Collectors.counting()
	                ));
	        //rejectedArticlesCountByCoAuthor.forEach((key, value) -> System.out.println("Rejected Articles:"+ key + ": " + value));
	        System.out.println("Rejected Articles size:"+rejectedArticlesCountByCoAuthor.size());
	        
	        Map<Long, Integer> nonTargetAuthorCountsByArticle = reCiterArticles.stream()
	                .collect(Collectors.toMap(
	                		ReCiterArticle::getArticleId, // Key: Article ID
	                    article -> (int) article.getArticleCoAuthors().getAuthors().stream()
	                        .filter(author -> !author.isTargetAuthor()) // Filter non-target authors
	                        .count() // Count non-target authors
	                ));
	        
	        reCiterArticles.stream()
	        	.filter(article -> article != null)
	        	.forEach(article-> {
	        		
	        	listOfAuthors= article.getArticleCoAuthors().getAuthors();
				coauthorNameListMap = new HashMap<>();
				Long articleId = article.getArticleId();
				
				
				
				listOfAuthors.stream()
				.filter(author -> author != null && !author.isTargetAuthor())
				.map(author -> processAuthor(author))        // Process each author to get the name
				.filter(coAuthorName -> coAuthorName != null && !coAuthorName.isEmpty()) 
				.forEach(coAuthorName -> {
					
						 
						 int countAccepted = 0;
						 int countRejected = 0; 
						 double sumAccepted = 0.0;
						 double sumRejected = 0.0;
						 double itemScore = 0.0;
						int updatedCountAccepted = 0;
						int updatedCountRejected = 0; 
						String exportedFeedbackScore =null; 
						
						
						if(acceptArticlesCountByCoAuthor!=null && acceptArticlesCountByCoAuthor.size() > 0)
						{	
							if(acceptArticlesCountByCoAuthor.containsKey(coAuthorName))
							{	
								countAccepted = Math.toIntExact(acceptArticlesCountByCoAuthor.get(coAuthorName));
							}
						}
						if(rejectedArticlesCountByCoAuthor!=null && rejectedArticlesCountByCoAuthor.size() > 0)
						{	
							if(rejectedArticlesCountByCoAuthor.containsKey(coAuthorName))
							{		
								countRejected = Math.toIntExact(rejectedArticlesCountByCoAuthor.get(coAuthorName));
							}
						}
						if(countAccepted > 0 || countRejected > 0)
						{	
							if(article.getGoldStandard() == 1)
							{
								updatedCountAccepted = countAccepted - 1;
							}
							if(article.getGoldStandard() == -1)
							{
								updatedCountRejected = countRejected-1;
							}
							System.out.println("updatedCountAccepted  and updatedCountRejected"+ updatedCountAccepted + "-" +  updatedCountRejected);
							itemScore = computeScore(updatedCountAccepted , updatedCountRejected);
							System.out.println("itemScore"+ decimalFormat.format(itemScore));
						}
						//Divide item score by count of the coAuthor of the interested article
						if(nonTargetAuthorCountsByArticle !=null && nonTargetAuthorCountsByArticle.size() > 0)
						{
							if(nonTargetAuthorCountsByArticle.containsKey(articleId) 
									&& nonTargetAuthorCountsByArticle.get(articleId) > 0)
							{
								int coAuthorsCount = nonTargetAuthorCountsByArticle.get(articleId);
								System.out.println("article Id and Count and CoAuthorName itemScore "+ articleId + " - " + coAuthorsCount + " - "+ coAuthorName +" - "+  decimalFormat.format(itemScore));
								if(article.getGoldStandard() == 1 && coAuthorsCount > 0)
								{
									sumAccepted = itemScore / coAuthorsCount;
									exportedFeedbackScore =  decimalFormat.format(sumAccepted);
									System.out.println("exportedFeedbackScore :-> "+  exportedFeedbackScore);
									//sumAccepted = Double.parseDouble(String.format("%.3f", sumAccepted));
									System.out.println("before conversion sumAccepted :-> "+  sumAccepted);
									System.out.println("After conversion sumAccepted :-> "+  decimalFormat.format(sumAccepted));
								}
								if(article.getGoldStandard() == -1 && coAuthorsCount > 0)
								{
									sumRejected = itemScore / coAuthorsCount;
									exportedFeedbackScore =  decimalFormat.format(sumRejected);
									System.out.println("exportedFeedbackScore :-> "+  exportedFeedbackScore);
									//sumRejected = Double.parseDouble(String.format("%.3f", sumRejected));
									System.out.println("before conversion sumRejected :-> "+  sumRejected);
									System.out.println("After conversion sumRejected :-> "+  decimalFormat.format(sumRejected));
								}
								
							}
							
						}
						ReCiterArticleFeedbackScore coAuthorNameArticle = populateArticleFeedbackScore(article.getArticleId(),coAuthorName,countAccepted,countRejected,
								   																	itemScore,sumAccepted,
								   																	sumRejected,article.getGoldStandard(),exportedFeedbackScore);
						
						System.out.println(" ReCiterArticleFeedbackScore  details -> :  " + coAuthorNameArticle.toString());
							coauthorNameListMap.computeIfAbsent(coAuthorName, k-> new ArrayList<>()).add(coAuthorNameArticle);
				});
				coauthorNameListMap.forEach((key,value) -> {
					System.out.println("Article Id -> : "+ articleId + "CoAuthorName : - > " +   key);
					value.forEach(feedbackArticle -> System.out.println("FeedbackScore : -> " + feedbackArticle.getFeedbackScore()));
					
				});
				
				// Calculate the sum of all scores
				double totalScore = coauthorNameListMap.values().stream().flatMap(List::stream) // Flatten the lists into a single stream of ReCiterFeedbackScoreCoAuthorName
						.mapToDouble(score -> {
							
							 double feedbackScore = score.getFeedbackScore();
						        return Double.isNaN(feedbackScore) ? 0 : feedbackScore;
							
						}) // Extract the scores
						.sum(); // Sum all scores
				
				
				// Sort Map Contents before storing into another Map
				coauthorNameListMap.entrySet().stream()
						.filter(entry -> entry.getKey() != null && entry.getValue() != null)
						.sorted(Map.Entry.comparingByKey())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
								(oldValue, newValue) -> oldValue, // merge function
								LinkedHashMap::new // to maintain insertion order
						));
				articleCoAuthorsMap.put(article.getArticleId(), coauthorNameListMap);
				totalScoresByArticleMap.put(article.getArticleId(), totalScore);
				System.out.println("article Id : -> " + articleId +" totalScore : ->" + totalScore);
				article.setCoAuthorNameFeedbackScore(totalScore);
				String exportedCoAuthorNameFeedbackScore = decimalFormat.format(totalScore); 
				System.out.println("Exported CoAuthor article Score***************"+exportedCoAuthorNameFeedbackScore);
				article.setExportedCoAuthorNameFeedbackScore(exportedCoAuthorNameFeedbackScore);
				
	        });
	        if (coauthorNameListMap != null && coauthorNameListMap.size() > 0) {

				// Printing using forEach
				slf4jLogger.info("********STARTING OF ARTICLE COAUTHOR NAME SCORING********************");
				if (articleCoAuthorsMap != null && articleCoAuthorsMap.size() > 0) {

					String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
							"subscoreType1", "subscoreValue", "subScoreIndividualScore","UserAssertion"  };
					exportItemLevelFeedbackScores(identity.getUid(), "CoAuthorName", csvHeaders, articleCoAuthorsMap);

				}
					slf4jLogger.info("********END OF THE ARTICLE COAUTHOR NAME SCORING********************\n");
			} else {
				slf4jLogger.info("********NO FEEDBACK SCORE FOR THE COAUTHOR NAME SECTION********************\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		stopWatchforCoAuthorFeedback.stop();
		slf4jLogger.info(stopWatchforCoAuthorFeedback.getId() + " took "
				+ stopWatchforCoAuthorFeedback.getTotalTimeSeconds() + "s");

		return 0;
	}

}
