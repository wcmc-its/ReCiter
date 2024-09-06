package reciter.algorithm.evidence.targetauthor.feedback.orcidcoauthor.strategy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class OrcidCoauthorFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(OrcidCoauthorFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackOrcidCoAuthorMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleOrcidsMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();
	List<ReCiterAuthor> listOfAuthors = null;
	

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	
	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {

		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());
			
			// Initialize a map to store results
	      //  Map<Long, Integer> pmidOrcidCounts = new HashMap<>();
	        
	     // Process the articles to count non-target author ORCIDs per PMID
	        Map<Long, Integer> nonTargetAuthorOrcidCountPerPmid = reCiterArticles.stream()
	            .collect(Collectors.toMap(
	                ReCiterArticle::getArticleId,
	                article -> (int)article.getArticleCoAuthors().getAuthors().stream()
	                    .filter(author -> !author.isTargetAuthor() && author.getOrcid()!=null && !author.getOrcid().isEmpty())
	                    .map(ReCiterAuthor::getOrcid)
	                    .count()
	            ));
	        
	        // Print results
	        nonTargetAuthorOrcidCountPerPmid.forEach((pmid, count) ->
	            System.out.println("PMID: " + pmid + ", Non-target Author ORCID Count: " + count)
	        );
	        
	        Map<String, Map<Integer, Long>> nonTargetAuthororcidCountsByArticleStatus = reCiterArticles.stream()
	        		 .filter(article -> article!=null && article.getArticleCoAuthors() !=null && article.getArticleCoAuthors().getAuthors()!=null && article.getArticleCoAuthors().getAuthors().size() > 0)
	                .flatMap(article -> article.getArticleCoAuthors().getAuthors().stream()
	                	 .filter(author-> author!=null && author.getOrcid()!=null && !author.getOrcid().isEmpty() && !author.isTargetAuthor())	
	                	 .flatMap(author -> {
	                    	String orcid = author.getOrcid();
	                    	List<String> orcidList = new ArrayList<>();
	                        orcidList.add(orcid);
	                        
		                        // Create a stream of (keyword, status) pairs
	                        return orcidList.stream()
	                            .map(orcidEntry -> new AbstractMap.SimpleEntry<>(orcidEntry, article.getGoldStandard()));
	                    })
	                )
	                // Count occurrences of each (keyword, status) pair
	                .collect(Collectors.groupingBy(
	                    Map.Entry::getKey,
	                    Collectors.groupingBy(
	                        Map.Entry::getValue,
	                        Collectors.counting()
	                    )
	                ));
	        
	        // Print results
	       /* nonTargetAuthororcidCountsByArticleStatus.forEach((orcid, counts) -> {
	                //int countAccepted = Math.toIntExact(emailCountsByArticleStatus.get(email).get(ACCEPTED));
	                //int countRejected = Math.toIntExact(emailCountsByArticleStatus.get(email).get(REJECTED));
	                System.out.println("Orcid in a Map: " + orcid);// + "-" + countAccepted +" -" + countRejected);
		               
	                counts.forEach((status, count) ->
	                    System.out.println("Artcle Status " + status + ": Articles Count " + count)
	                );
	            });*/
	      
	        reCiterArticles.stream()
	        		.filter(article->article!=null && article.getArticleCoAuthors()!=null && article.getArticleCoAuthors().getAuthors()!=null)
	        		.forEach(article->{
				ReCiterArticleAuthors coAuthors = article.getArticleCoAuthors();
				listOfAuthors = coAuthors.getAuthors();
				
				feedbackOrcidCoAuthorMap = new HashMap<>();
				//for (ReCiterAuthor author : listOfAuthors) {
				listOfAuthors.stream()
							.filter(author-> author!=null
							 && author.getOrcid()!=null && !author.getOrcid().isEmpty()
							 && !author.isTargetAuthor())
							.forEach(author->{
								
					double weightageScore = 0.0;
					double scoreAll = 0.0;
					double sumAccepted = 0.0;
					double sumRejected = 0.0;
					
							 int countAccepted = 0;
							 int countRejected = 0;
			
								if(article!=null && article.getGoldStandard()==1)
								{
									int orcidCount=0;
									if(nonTargetAuthorOrcidCountPerPmid !=null && nonTargetAuthorOrcidCountPerPmid.size() > 0)
									{	
										if(nonTargetAuthorOrcidCountPerPmid.containsKey(article.getArticleId()))
										{	
											orcidCount  = nonTargetAuthorOrcidCountPerPmid.get(article.getArticleId());
											if(orcidCount > 0)
											{	
												weightageScore = 1.0 / (double)orcidCount; 
												
											}
										}
										if(nonTargetAuthororcidCountsByArticleStatus !=null && nonTargetAuthororcidCountsByArticleStatus.size() >0
												&& nonTargetAuthororcidCountsByArticleStatus.containsKey(author.getOrcid()) &&
												nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).containsKey(ACCEPTED))
											countAccepted = Math.toIntExact(nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).get(ACCEPTED));
										sumAccepted = weightageScore * countAccepted;
									}
									
								}
								else if(article!=null && article.getGoldStandard() == -1)
								{
									int orcidCount=0;
									if(nonTargetAuthorOrcidCountPerPmid !=null && nonTargetAuthorOrcidCountPerPmid.size() > 0)
									{	
										if(nonTargetAuthorOrcidCountPerPmid.containsKey(article.getArticleId()))
										{
											orcidCount  = nonTargetAuthorOrcidCountPerPmid.get(article.getArticleId());
											if(orcidCount > 0)
											{	
												weightageScore = 1.0 / (double)orcidCount; 
												
											}
										}
									}
									if(nonTargetAuthororcidCountsByArticleStatus !=null && nonTargetAuthororcidCountsByArticleStatus.size() >0
											&& nonTargetAuthororcidCountsByArticleStatus.containsKey(author.getOrcid()) &&
											nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).containsKey(REJECTED))
										countRejected = Math.toIntExact(nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).get(REJECTED));
									
									sumRejected =  weightageScore * countRejected;
									
									
								}
							scoreAll = computeScore(sumAccepted, sumRejected);
				
							ReCiterArticleFeedbackScore feedbackOrcid = populateArticleFeedbackScore(article.getArticleId(),author.getOrcid(),
									   countAccepted,countRejected,
									   scoreAll,0.0,
									   0.0,article.getGoldStandard(),null);
							
							feedbackOrcidCoAuthorMap.computeIfAbsent(Long.toString(article.getArticleId()), k -> new ArrayList<>()).add(feedbackOrcid);
							
						
				//}

				});
				 double totalScore = feedbackOrcidCoAuthorMap.values().stream().flatMap(List::stream) // Flatten the lists into a single stream of ReCiterFeedbackScoreCoAuthorName
							.mapToDouble(score -> score.getFeedbackScore()) // Extract the scores
							.sum(); // Sum all scores
				
				feedbackOrcidCoAuthorMap.entrySet().stream()
				.filter(entry -> entry.getKey() != null && entry.getValue() != null)
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, // merge function
						LinkedHashMap::new // to maintain insertion order
				));
				articleOrcidsMap.put(article.getArticleId(), feedbackOrcidCoAuthorMap);
				totalScoresByArticleMap.put(article.getArticleId(), totalScore);
				article.setOrcidCoAuthorFeedbackScore(totalScore);
				String exportedOrcidCoAuthorFeedbackScore = decimalFormat.format(totalScore);
				System.out.println("Exported OrCid CoAuthor article Score***************"+exportedOrcidCoAuthorFeedbackScore);
				article.setExportedOrcidCoAuthorFeedbackScore(exportedOrcidCoAuthorFeedbackScore);
			});
	        
	        if (articleOrcidsMap != null && articleOrcidsMap.size() > 0) {
				
				// Printing using forEach
				slf4jLogger.info("********STARTING OF ARTICLE ORCID COAUTHOR SCORING********************");
				if (articleOrcidsMap != null && articleOrcidsMap.size() > 0) {

					String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
							"subscoreType1", "subscoreValue", "subScoreIndividualScore","UserAssertion"  };
					exportItemLevelFeedbackScores(identity.getUid(), "OrcidCoAuthor", csvHeaders, articleOrcidsMap);

				}
				

				slf4jLogger.info("********END OF THE ARTICLE ORCID COAUTHOR SCORING********************\n");
			} else {
				slf4jLogger.info("********NO FEEDBACK SCORE FOR THE ORCID COAUTHOR SECTION********************\n");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	// Helper method to compute score
		protected double computeScore(double countAccepted, double countRejected) {
			return (1 / (1 + Math.exp(-(countAccepted - countRejected) / (Math.sqrt(countAccepted + countRejected) + 1))))- 0.5;
		}
}
