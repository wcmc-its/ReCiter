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
	List<ReCiterAuthor> listOfAuthors = null;
	

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	
	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {

		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());
			
	     // Process the articles to count non-target author ORCIDs per PMID
	        Map<Long, Integer> nonTargetAuthorOrcidCountPerPmid = reCiterArticles.stream()
	            .collect(Collectors.toMap(
	                ReCiterArticle::getArticleId,
	                article -> (int)article.getArticleCoAuthors().getAuthors().stream()
	                    .filter(author -> !author.isTargetAuthor() && author.getOrcid()!=null && !author.getOrcid().isEmpty())
	                    .map(ReCiterAuthor::getOrcid)
	                    .count()
	            ));
	        
	        nonTargetAuthorOrcidCountPerPmid.forEach((key, value) -> {
	        	slf4jLogger.info("nonTargetAuthorOrcidCountPerPmid : " + key + " = " + value);
	        });
	        
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
	        
	        nonTargetAuthororcidCountsByArticleStatus.forEach((outerKey, innerMap) -> {
	        	slf4jLogger.info("nonTargetAuthororcidCountsByArticleStatus : " + outerKey);
	            innerMap.forEach((innerKey, value) -> {
	            	slf4jLogger.info("nonTargetAuthororcidCountsByArticleStatus innerMap  :  " + innerKey + " = " + value);
	            });
	        });
	        
	        reCiterArticles.stream()
	        		.filter(article->article!=null && article.getArticleCoAuthors()!=null && article.getArticleCoAuthors().getAuthors()!=null)
	        		.forEach(article->{
				ReCiterArticleAuthors coAuthors = article.getArticleCoAuthors();
				listOfAuthors = coAuthors.getAuthors();
				
				feedbackOrcidCoAuthorMap = new HashMap<>();
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
							 
							 slf4jLogger.info("artice GoldStadard : "+ article.getGoldStandard());
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
								else
								{
									int orcidCountAccepted=0;
									if(nonTargetAuthorOrcidCountPerPmid !=null && nonTargetAuthorOrcidCountPerPmid.size() > 0)
									{	
										if(nonTargetAuthorOrcidCountPerPmid.containsKey(article.getArticleId()))
										{	
											orcidCountAccepted  = nonTargetAuthorOrcidCountPerPmid.get(article.getArticleId());
											if(orcidCountAccepted > 0)
											{	
												weightageScore = 1.0 / (double)orcidCountAccepted; 
												
											}
										}
										if(nonTargetAuthororcidCountsByArticleStatus !=null && nonTargetAuthororcidCountsByArticleStatus.size() >0
												&& nonTargetAuthororcidCountsByArticleStatus.containsKey(author.getOrcid()) &&
												nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).containsKey(ACCEPTED))
											countAccepted = Math.toIntExact(nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).get(ACCEPTED));
										sumAccepted = weightageScore * countAccepted;
									}
									
									int orcidCountRejected=0;
									if(nonTargetAuthorOrcidCountPerPmid !=null && nonTargetAuthorOrcidCountPerPmid.size() > 0)
									{	
										if(nonTargetAuthorOrcidCountPerPmid.containsKey(article.getArticleId()))
										{
											orcidCountRejected  = nonTargetAuthorOrcidCountPerPmid.get(article.getArticleId());
											if(orcidCountRejected > 0)
											{	
												weightageScore = 1.0 / (double)orcidCountRejected; 
												
											}
										}
									}
									if(nonTargetAuthororcidCountsByArticleStatus !=null && nonTargetAuthororcidCountsByArticleStatus.size() >0
											&& nonTargetAuthororcidCountsByArticleStatus.containsKey(author.getOrcid()) &&
											nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).containsKey(REJECTED))
										countRejected = Math.toIntExact(nonTargetAuthororcidCountsByArticleStatus.get(author.getOrcid()).get(REJECTED));
									
									sumRejected =  weightageScore * countRejected;
									scoreAll = computeScore(sumAccepted, sumRejected);
								}
								
							slf4jLogger.info("sumAccepted :" + sumAccepted + "\nSumRejected : " + sumRejected +"\n ScoreAll :" + scoreAll);
							double feedbackScore= determineFeedbackScore(0,0.0,0.0, scoreAll);
							slf4jLogger.info("Feedback Score:"+feedbackScore);
							String exportedFeedbackScore = decimalFormat.format(feedbackScore);
							slf4jLogger.info("exportedFeedbackScore:"+exportedFeedbackScore);
							ReCiterArticleFeedbackScore feedbackOrcid = populateArticleFeedbackScore(article.getArticleId(),author.getOrcid(),
									   countAccepted,countRejected,
									   scoreAll,0.0,
									   0.0,article.getGoldStandard(),feedbackScore,exportedFeedbackScore,"OrcidCoAuthor");
							
							feedbackOrcidCoAuthorMap.computeIfAbsent(Long.toString(article.getArticleId()), k -> new ArrayList<>()).add(feedbackOrcid);
							
						
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
				article.addArticleFeedbackScoresMap(feedbackOrcidCoAuthorMap);
				article.setOrcidCoAuthorFeedbackScore(totalScore);
				String exportedOrcidCoAuthorFeedbackScore = decimalFormat.format(totalScore);
				article.setExportedOrcidCoAuthorFeedbackScore(exportedOrcidCoAuthorFeedbackScore);
			});
	        
	       
			
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
