package reciter.algorithm.evidence.targetauthor.feedback.cites.strategy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;



public class CitesFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(CitesFeedbackStrategy.class);
	//Map<String, List<ReCiterArticleFeedbackScore>> feedbackCitesMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleCitesMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();
	//List<ReCiterAuthor> listOfAuthors = null;

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}
	
	public static Map<Long, Integer> countArticlesByStandard(Map<Long, List<ReCiterArticle>> articlesMap) {
        return articlesMap.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> countArticles(entry.getValue())
                ));
    }

    private static int countArticles(List<ReCiterArticle> articles) {
        return articles.stream()
                .mapToInt(ReCiterArticle::getGoldStandard) // assuming standardValue is int
                .filter(value -> value == 1 || value == -1) // filtering based on the standard values
                .map(value -> 1) // map each valid value to 1
                .sum(); // sum up the counts
    }

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		StopWatch stopWatchforCitesFeedback = new StopWatch("Cites");
		stopWatchforCitesFeedback.start("Cites");
		try {
			slf4jLogger.info("reCiterArticles size in cites: ", reCiterArticles.size());
			
			reCiterArticles.forEach(article-> System.out.println(" reciter articles  articleId -> "+ article.getArticleId() +" GoldStandard -> "+ article.getGoldStandard() + " commentCorrectionIds -> "+ article.getCommentsCorrectionsPmids()));

			
			 List<ReCiterArticle> filteredArticles = reCiterArticles.stream()
			 														.filter(article -> article.getGoldStandard() != 0)
			 														 .collect(Collectors.toList());
				
			 filteredArticles.forEach(article-> System.out.println("articleId -> "+ article.getArticleId() +" GoldStandard -> "+ article.getGoldStandard() + " commentCorrectionIds -> "+ article.getCommentsCorrectionsPmids()));

			 
			 Map<Long, ReCiterArticle> articleMap = filteredArticles.stream()
		                .collect(Collectors.toMap(ReCiterArticle::getArticleId, Function.identity()));
			 
			 
			
						 
			 // Citing article Id Map with the group by status of the Cited Articles 
	    	 Map<Long, Map<Integer, List<ReCiterArticle>>> citingArticlesGroupedByStatus = filteredArticles.stream()
	    	            .collect(Collectors.toMap(
	    	            		ReCiterArticle::getArticleId, // Citing article ID
	    	                article -> article.getCommentsCorrectionsPmids().stream()
	    	                    .map(correctionId -> articleMap.get(correctionId)) // Get cited articles
	    	                    .filter(Objects::nonNull) // Filter out nulls in case of invalid IDs
	    	                    .collect(Collectors.groupingBy(
	    	                    		ReCiterArticle::getGoldStandard, // Group by status of cited articles
	    	                        Collectors.toList()
	    	                    )),
	    	                (existing, replacement) -> existing // handle merging if necessary
	    	            ));
	    	 
	  /*  	 citingArticlesGroupedByStatus.forEach((key,value) -> {
	    		 
	    		 System.out.println("CitingFeedback Article Key**************************" + key);
	    		 value.forEach((key1,value1) ->{
	    			System.out.println("Cited article status *************"+key1); 
	    			 value1.forEach(citedArticle-> System.out.println("Cited Article Id***" + citedArticle.getArticleId() + " Gold Standard********"+citedArticle.getGoldStandard()));
	    		 });
	    		 
	    	 });*/
	    	 
	    	 //List of Cited Articles per Citing Article
	    	 
	    	// Create a set of article IDs for quick lookup
	         Set<Long> articleIds = reCiterArticles.stream()
	                                         .map(ReCiterArticle::getArticleId)
	                                         .collect(Collectors.toSet());
	         
	    	 // Create the map
	         Map<Long, Map<Integer, List<ReCiterArticle>>> citedArticlesGroupByStatus = filteredArticles.stream()
	                 .flatMap(article -> article.getCommentsCorrectionsPmids().stream()
	                		 .filter(refId -> articleIds.contains(refId)) // Filter referenced IDs that exist in the articles list
	                         .map(referenceId -> new AbstractMap.SimpleEntry<>(referenceId, article)))
	                 		 .filter(Objects::nonNull)	
	                 .collect(Collectors.groupingBy(
	                         Map.Entry::getKey, // Group by commentCommercial articleId
	                         Collectors.groupingBy(
	                                 entry -> entry.getValue().getGoldStandard(), // Group by status
	                                 Collectors.mapping(Map.Entry::getValue, Collectors.toList()) // Collect articles
	                         )
	                 ));
	    	 
	         // Print the resulting map
	        /* System.out.println("Cited Articles by Status:");
	         citedArticlesGroupByStatus.forEach((refArticleId, statusMap) -> {
	             System.out.println("Cited Article ID: " + refArticleId);
	             statusMap.forEach((status, articleList) -> {
	                 System.out.println("  Citing Article Status: " + status);
	                 articleList.forEach(article -> System.out.println("    Citing Article ID: " + article.getArticleId()));
	             });
	         });*/
	    	 
	    	
	    	 
	         //Count Accepted and Rejected articles per cited and citing article
			
	         reCiterArticles.stream().filter(article -> article!=null /*&& article.getCommentsCorrectionsPmids()!=null && !article.getCommentsCorrectionsPmids().isEmpty()*/)
	         						  .forEach(article ->{
	         							  
	         							 Map<String,List<ReCiterArticleFeedbackScore>> feedbackCitesMap = new HashMap<>();
	         							 
	         							 //Citing Articles
	         							 Map<Integer, List<ReCiterArticle>> citingArticlesStatusMap = citingArticlesGroupedByStatus.get(article.getArticleId());
	         							  
	         							 //Accepted articles
	         							List<ReCiterArticle> citingAcceptedArticles = null;
	         							List<ReCiterArticle> citingRejectedArticles =null;
	         							int[] citingArticlesCountAccepted = {0};
	         							int[] citingArticlesCountRejected = {0};
         							    if(citingArticlesStatusMap!=null && citingArticlesStatusMap.size() > 0)
         							    {	
         							    	
         							    	/*citingArticlesStatusMap.forEach((key,value) -> {
    	         								
    	         								System.out.println("Citing Article Status : -> " + key);
    	         								value.forEach(citingArticle -> System.out.println("Cited Article Id and Status :-> " + citingArticle.getArticleId() + " status-> "+ citingArticle.getGoldStandard()));
    	         							});*/
         							    	
         							    	citingAcceptedArticles = citingArticlesStatusMap.getOrDefault(1, new ArrayList<ReCiterArticle>());
         							    	//citingAcceptedArticles.forEach(acceptedArticle -> System.out.println("Citing Accepted ArticleId -> "+acceptedArticle.getArticleId() + "Citing GoldStandard -> " + acceptedArticle.getGoldStandard()));
         							    	//citingArticlesCountAccepted[0] = citingAcceptedArticles.size();
		         							 // System.out.println("Citing countAccpted-> ****"+citingArticlesCountAccepted[0]);
		         						 
		         							 citingRejectedArticles = citingArticlesStatusMap.getOrDefault(-1, new ArrayList<ReCiterArticle>());
		         							 //citingRejectedArticles.forEach(rejectedArticle -> System.out.println("Citing rejected ArticleId -> "+rejectedArticle.getArticleId() + "Citing GoldStandard -> " + rejectedArticle.getGoldStandard()));
		         							//citingArticlesCountRejected[0] = citingRejectedArticles.size();
		         							// System.out.println("Citing countRejected-> ****"+citingArticlesCountRejected[0]);
	         							}
         							   //Cited Articles 
         							   Map<Integer, List<ReCiterArticle>> citedArticlesSatusMap = citedArticlesGroupByStatus.get(article.getArticleId());
         							 // System.out.println("Cited Article Id ************************* : -> " + article.getArticleId());
         							 
	         							  
	         							 //Accepted articles
	         							List<ReCiterArticle> citedAcceptedArticles = null;
	         							List<ReCiterArticle> citedRejectedArticles =null;
	         							int[] citedArticlesCountAccepted = {0};
	         							int[] citedArticlesCountRejected = {0};
	       							    if(citedArticlesSatusMap!=null && citedArticlesSatusMap.size() > 0)
	       							    {	  
		       							   /*  citedArticlesSatusMap.forEach((key,value) -> {
			         								
			         								System.out.println("Cited Article Status : -> " + key);
			         								value.forEach(citingArticle -> System.out.println("Citing Article Id and Status :-> " + citingArticle.getArticleId() + " status -> "+ citingArticle.getGoldStandard()));
			         							});*/
	       							    	
	       							    		citedAcceptedArticles = citedArticlesSatusMap.getOrDefault(1, new ArrayList<ReCiterArticle>());
	       							    		//citedAcceptedArticles.forEach(acceptedArticle -> System.out.println("Cited Accepted ArticleId -> "+acceptedArticle.getArticleId() + "Cited GoldStandard -> " + acceptedArticle.getGoldStandard()));
	       							    		//citedArticlesCountAccepted[0] = citedAcceptedArticles.size();
			         							 // System.out.println("Cited countAccpted-> ****"+citedArticlesCountAccepted[0]);
			         						 
			         							 citedRejectedArticles = citedArticlesSatusMap.getOrDefault(-1, new ArrayList<ReCiterArticle>());
			         							//citedRejectedArticles.forEach(rejectedArticle -> System.out.println("Cited rejected ArticleId -> "+rejectedArticle.getArticleId() + "Cited GoldStandard -> " + rejectedArticle.getGoldStandard()));
			         							//citedArticlesCountRejected[0] = citedRejectedArticles.size();
			         							// System.out.println("Cited countRejected-> ****"+citedArticlesCountRejected[0]);
		         						}
	         							
	       							    int[] overallCountRejected = {citingArticlesCountRejected[0] + citedArticlesCountRejected[0]};
	       							    int[] overallCountAccepted = {citingArticlesCountAccepted[0] + citedArticlesCountAccepted[0]};
	       							    
	       							   // System.out.println("Article Id : " + article.getArticleId() + " Overall Count Accepted : " + overallCountAccepted[0] + " Overall Count Rejected : " + overallCountRejected[0]);
         							     
	         							double scoreAll = computeScore(overallCountAccepted[0], overallCountRejected[0]);
										//double scoreWithout1Accepted = computeScore(overallCountAccepted[0] > 0 ? overallCountAccepted[0] - 1 : overallCountAccepted[0],
											//	overallCountRejected[0]);
										//double scoreWithout1Rejected = computeScore(overallCountAccepted[0],
											//	overallCountRejected[0] > 0 ? overallCountRejected[0] - 1 : overallCountRejected[0]);
	         							 
	         							 //Club the Rejected Articles and Accepted Articles
	         							 List<ReCiterArticle> mergedList = Stream.of(
	         									 (citingAcceptedArticles != null && !citingAcceptedArticles.isEmpty()) ? citingAcceptedArticles : new ArrayList<ReCiterArticle>(), 
	         									 (citingRejectedArticles != null && !citingRejectedArticles.isEmpty()) ? citingRejectedArticles : new ArrayList<ReCiterArticle>(), 
	         									 (citedAcceptedArticles != null && !citedAcceptedArticles.isEmpty()) ? citedAcceptedArticles : new ArrayList<ReCiterArticle>(),
	         									 (citedRejectedArticles != null && !citedRejectedArticles.isEmpty()) ? citedRejectedArticles : new ArrayList<ReCiterArticle>())
	         					                .flatMap(Collection::stream) // Flatten the streams
	         					                .distinct()
	         					                .collect(Collectors.toList()); 
	         							
										Optional.ofNullable(mergedList).ifPresent(citredArticleList -> citredArticleList.forEach(mergedArticle -> {
	
											
											// System.out.println("mergedArticle : -> "+ mergedArticle.getArticleId() + " Gold Standard " + mergedArticle.getGoldStandard());
		         								//Citing article Mapping
		         								ReCiterArticleFeedbackScore citingArticleFeedbackScore = populateArticleFeedbackScore(article.getArticleId(),Long.toString(mergedArticle.getArticleId()),
		         										overallCountAccepted[0],overallCountRejected[0],
														   scoreAll,0.0,
														   0.0,0,null);	
		         								
		         								
												feedbackCitesMap.merge(Long.toString(article.getArticleId()), new ArrayList<>(Arrays.asList(citingArticleFeedbackScore)), (existingList, newList) -> {
									                existingList.addAll(newList);
									                return existingList;
											});
	        							}));
	         							/*feedbackCitesMap.forEach((key,value) -> {
	         								
	         								System.out.println("FeedbackCites Map Key***************"+key);
	         								System.out.println("Feedback Map List contents are************");
	         									value.forEach(System.out::println);
	         							});*/
	         							
	         							double totalScore = feedbackCitesMap.values().stream().flatMap(List::stream) // Flatten the lists into a single stream of ReCiterFeedbackScoreCoAuthorName
	        									.mapToDouble(score-> score.getFeedbackScore()) // Extract the scores
	        									.sum();
	        							
	         							articleCitesMap.put(article.getArticleId(),feedbackCitesMap);
	         							totalScoresByArticleMap.put(article.getArticleId(), totalScore);
	         							article.setCitesFeedbackScore(totalScore);
	         							String exportedCitesFeedbackScore = decimalFormat.format(totalScore);
	         							System.out.println("Exported Cites article Score***************"+exportedCitesFeedbackScore);
	         							article.setExportedCitesFeedbackScore(exportedCitesFeedbackScore);
	         							  
	         						  });
	         						    /*articleCitesMap.forEach((key,value)->{
	         						    	System.out.println("ArticleId************************"+key);
	         						    	value.forEach((key1,value1)-> {
	         						    	 	System.out.println(" innerMap ArticleId************************"+key1);
	     	         						    value1.forEach(key2 -> System.out.println("ArticleId*****************"+key2.toString()));
	         						    	});
	         						    	
	         						    });	*/	
	         
	         
								      // Printing using forEach
											slf4jLogger.info("********STARTING OF ARTICLE CITES SCORING********************");
											if (articleCitesMap != null && articleCitesMap.size() > 0) {
							
												String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
														"subscoreType1", "subscoreValue", "subScoreIndividualScore" };
												exportItemLevelFeedbackScores(identity.getUid(), "Cites", csvHeaders, articleCitesMap);
							
											slf4jLogger.info("********END OF THE CITES SCORING********************\n");
										} else {
											slf4jLogger.info("********NO FEEDBACK SCORE FOR THE CITES SECTION********************\n");
										}	

		} catch (Exception e) {
			e.printStackTrace();
		}
		stopWatchforCitesFeedback.stop();
		slf4jLogger.info(stopWatchforCitesFeedback.getId() + " took "
				+ stopWatchforCitesFeedback.getTotalTimeSeconds() + "s"); 
		return 0;
	}

}
