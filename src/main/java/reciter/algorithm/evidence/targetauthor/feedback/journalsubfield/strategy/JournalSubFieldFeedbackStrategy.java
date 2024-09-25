package reciter.algorithm.evidence.targetauthor.feedback.journalsubfield.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.ApplicationContextHolder;
import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.identity.Identity;
import reciter.model.pubmed.MedlineCitationJournalISSN;
import reciter.service.ScienceMetrixService;

public class JournalSubFieldFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(JournalSubFieldFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackJournalsSubFieldMap = null;
	//Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleJournalsMap = new HashMap<>();
	//Map<Long, Double> totalScoresByArticleMap = new HashMap<>();
	
	/*String[] ignoreScienceMetricSubFieldValues = {"Clinical Medicine", "Biomedical Research", "General Science & Technology","General & Internal Medicine",
			"General Clinical Medicine","General Chemistry","General Physics","General Arts, Humanities & Social Sciences"};*/
	
	ScienceMetrixService scienceMetrixService = ApplicationContextHolder.getContext()
			.getBean(ScienceMetrixService.class);
	
	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}
	
	private String retrieveJournalSubField(MedlineCitationJournalISSN medlineCitationJournalIssn) {
		 
		System.out.println("Coming here**************************"+medlineCitationJournalIssn.getIssn());
		if(medlineCitationJournalIssn!=null && medlineCitationJournalIssn.getIssn()!=null && !medlineCitationJournalIssn.getIssn().isEmpty())
		{
			System.out.println("Coming here1**************************"+medlineCitationJournalIssn.getIssn());
			ScienceMetrix scienceMetrix = scienceMetrixService.findByIssn(medlineCitationJournalIssn.getIssn());
			System.out.println("Coming here2**************************"+scienceMetrix);
			if (scienceMetrix == null)
				scienceMetrix = scienceMetrixService.findByEissn(medlineCitationJournalIssn.getIssn());
			if (scienceMetrix != null && scienceMetrix.getScienceMetrixSubfield()!=null &&
					!scienceMetrix.getScienceMetrixSubfield().equalsIgnoreCase("") 
					/*&& Arrays.stream(ignoreScienceMetricSubFieldValues).noneMatch(scienceMetrix.getScienceMetrixSubfield()::equals)*/)
				 return scienceMetrix.getScienceMetrixSubfield();
		}
		return null;
	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());
			
			
			
			Map<Integer, List<ReCiterArticle>> groupedByGoldStandard = reCiterArticles.stream()
		            .collect(Collectors.groupingBy(ReCiterArticle::getGoldStandard));

		        // Get lists of articles based on gold standard
		        List<ReCiterArticle> acceptedArticles = groupedByGoldStandard.getOrDefault(1, Collections.emptyList());
		        List<ReCiterArticle> rejectedArticles = groupedByGoldStandard.getOrDefault(-1, Collections.emptyList());

		        Map<String, Long> acceptArticlesCountByJournalSubField =  acceptedArticles.stream()
		        .filter(article-> article!=null && article.getJournal()!=null && article.getJournal().getJournalIssn()!=null && article.getJournal().getJournalIssn().size() > 0)		
	            .flatMap(article -> article.getJournal().getJournalIssn().stream())
	            .filter(journalIssn-> journalIssn!=null)
	            .map(journalIssn-> retrieveJournalSubField(journalIssn))
	            .filter(journalSubField -> journalSubField!=null && !journalSubField.isEmpty()) // Filter out empty strings
	            .collect(Collectors.groupingBy(
	                Function.identity(),
	                Collectors.counting()
	            ));
		        System.out.println("Accepted Articles size:"+acceptArticlesCountByJournalSubField.size());
		        acceptArticlesCountByJournalSubField.forEach((key, value) -> System.out.println("Accepted Article Journal SubField:"+ key + ": " + value));
		        
	       
		        Map<String, Long> rejectedArticlesCountByJournalSubField =  rejectedArticles.stream()
		        		.filter(article-> article!=null && article.getJournal()!=null && article.getJournal().getJournalIssn()!=null && article.getJournal().getJournalIssn().size() > 0)
		        		.flatMap(article -> article.getJournal().getJournalIssn().stream())
			            .filter(journalIssn-> journalIssn!=null)
			            .map(journalIssn-> retrieveJournalSubField(journalIssn))
			            .filter(journalSubField -> journalSubField!=null && !journalSubField.isEmpty()) // Filter out empty strings
			            .collect(Collectors.groupingBy(
			                Function.identity(),
			                Collectors.counting()
			            ));
		        rejectedArticlesCountByJournalSubField.forEach((key, value) -> System.out.println("Rejected Articles:"+ key + ": " + value));
		        System.out.println("Rejected Articles size:"+rejectedArticlesCountByJournalSubField.size());
			
		        reCiterArticles.stream()
				   .filter(article-> article!=null && article.getJournal()!=null && article.getJournal().getJournalIssn()!=null && article.getJournal().getJournalIssn().size()> 0)
				   .forEach(article -> {
											
										System.out.println("Article Id and Gold Standard*****************"+ article.getArticleId() + "-" + article.getGoldStandard()
												+ " Journal Count -> : " + article.getJournal().getJournalIssn().size());	
										
										feedbackJournalsSubFieldMap = new HashMap<>();   
										
										article.getJournal().getJournalIssn().stream()
											.filter(journalIssn -> journalIssn!=null )
											.forEach(journalIssn -> {
												
												 int countAccepted = 0;
												 int countRejected = 0;
												 double scoreAll = 0.0;
												 double scoreWithout1Accepted = 0.0;
												 double scoreWithout1Rejected = 0.0;
												 
												 System.out.println(" JournalSubField **** "+ journalIssn.getIssn());
												 
												 String journalSubField = retrieveJournalSubField(journalIssn);
												
												if(journalSubField!=null && !journalSubField.isEmpty())
												{	
													System.out.println(" Extracted Keyword :" + journalSubField);
													//retrieve accepted PMIDs for this email
													if(acceptArticlesCountByJournalSubField!=null && acceptArticlesCountByJournalSubField.size() > 0)
													{	
														if(acceptArticlesCountByJournalSubField.containsKey(journalSubField))
														{	
															countAccepted = Math.toIntExact(acceptArticlesCountByJournalSubField.get(journalSubField));
														}
													}
													if(rejectedArticlesCountByJournalSubField!=null && rejectedArticlesCountByJournalSubField.size() > 0)
													{	
														if(rejectedArticlesCountByJournalSubField.containsKey(journalSubField))
														{		
															countRejected = Math.toIntExact(rejectedArticlesCountByJournalSubField.get(journalSubField));
														}
													}
													
													
													
													if(countAccepted > 0 || countRejected > 0)
													{	
														
														System.out.println(" JournalSubField : " + journalSubField + " CountAccepted :" +countAccepted + " CountRejected :"+ countRejected);
														scoreAll = computeScore(countAccepted , countRejected);
														scoreWithout1Accepted = computeScore(countAccepted > 0 ? countAccepted - 1 : countAccepted,
																countRejected);
														scoreWithout1Rejected = computeScore(countAccepted,
																countRejected > 0 ? countRejected - 1 : countRejected);
														System.out.println("itemScore ************"+ scoreAll);
														System.out.println(" scoreWithout1Accepted ****************"+ scoreWithout1Accepted);
														System.out.println(" scoreWithout1Rejected ********************" + scoreWithout1Rejected);
														
														double feedbackScore= determineFeedbackScore(article.getGoldStandard(),scoreWithout1Accepted, scoreWithout1Rejected, scoreAll);
														String exportedFeedbackScore = decimalFormat.format(feedbackScore);
														
														
														ReCiterArticleFeedbackScore feedbackEmail = populateArticleFeedbackScore(article.getArticleId(),journalSubField,
																   countAccepted,countRejected,
																   scoreAll,scoreWithout1Accepted,
																   scoreWithout1Rejected,article.getGoldStandard(),feedbackScore,exportedFeedbackScore,"Journal SubField");
														
														feedbackJournalsSubFieldMap.computeIfAbsent(journalSubField, k -> new ArrayList<>()).add(feedbackEmail);
														
													}
												
												}
											});
									  // System.out.println("FeedbackEmail Map Keys Size***************"+ feedbackEmailMap.size());
										feedbackJournalsSubFieldMap.forEach((key,value) -> {
	         								
	         								System.out.println("FeedbackEmail Map Key***************"+key);
	         								System.out.println("Feedback Map List contents are************");
	         									value.forEach(System.out::println);
	         							});
									   
									   double totalScore = feedbackJournalsSubFieldMap.values().stream().flatMap(List::stream) // Flatten the lists into a single stream of ReCiterFeedbackScoreCoAuthorName
												.mapToDouble(score-> score.getFeedbackScore()) // Extract the scores
												.sum(); // Sum all scores
										
										
										// Sort Map Contents before storing into another Map
									   feedbackJournalsSubFieldMap.entrySet().stream()
												.filter(entry -> entry.getKey() != null && entry.getValue() != null)
												.sorted(Map.Entry.comparingByKey())
												.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
														(oldValue, newValue) -> oldValue, // merge function
														LinkedHashMap::new // to maintain insertion order
												));
									  // articleJournalsMap.put(article.getArticleId(), feedbackJournalsSubFieldMap);
									   article.addArticleFeedbackScoresMap(feedbackJournalsSubFieldMap);
										//totalScoresByArticleMap.put(article.getArticleId(), totalScore);
										article.setJournalSubFieldFeedbackScore(totalScore);
										String exportedJournalSubFieldFeedbackScore = decimalFormat.format(totalScore); 
										System.out.println("Exported Journal SubField article Score***************"+exportedJournalSubFieldFeedbackScore);
										article.setExportedJournalSubFieldFeedbackScore(exportedJournalSubFieldFeedbackScore);
				   				});
		        
		        
			
			/*if (feedbackJournalsSubFieldMap != null && feedbackJournalsSubFieldMap.size() > 0) {

				// Printing using forEach
				slf4jLogger.info("********STARTING OF JOURNAL SUBFIELD SCORING********************");
				

					String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
							"subscoreType1", "subscoreValue", "subScoreIndividualScore" };
					exportArticleItemLevelFeedbackScores(identity.getUid(), "Journal SubField", csvHeaders, feedbackJournalsSubFieldMap);
	
				slf4jLogger.info("********END OF THE JOURNAL SUBFIELD SCORING********************\n");
			} else {
				slf4jLogger.info("********NO FEEDBACK SCORE FOR THE JOURNAL SUBFIELD SECTION********************\n");
			}*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
