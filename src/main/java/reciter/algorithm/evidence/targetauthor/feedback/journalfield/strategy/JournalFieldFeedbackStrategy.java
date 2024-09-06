package reciter.algorithm.evidence.targetauthor.feedback.journalfield.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.ApplicationContextHolder;
import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterFeedbackScoreArticle;
import reciter.model.identity.Identity;
import reciter.model.pubmed.MedlineCitationJournalISSN;
import reciter.service.ScienceMetrixService;

public class JournalFieldFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(JournalFieldFeedbackStrategy.class);
//	Map<String, List<ReCiterArticle>> feedbackJournalsFieldMap = new HashMap<>();
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackJournalFieldListMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleJournalsMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();


	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());
			ScienceMetrixService scienceMetrixService = ApplicationContextHolder.getContext()
					.getBean(ScienceMetrixService.class);

			//for (ReCiterArticle article : reCiterArticles) {
			 reCiterArticles.stream()
			 			   .filter(article -> article!=null && article.getJournal()!=null && article.getJournal().getJournalIssn()!=null)
			 			   .forEach(article -> {
						final int[] countAccepted = {0};
						final int[] countRejected = {0};
		
						final double[] scoreAll = {0.0};
						final double[] scoreWithout1Accepted = {0.0};
						final double[] scoreWithout1Rejected = {0.0};
						//for (MedlineCitationJournalISSN medCitJournalISSN : article.getJournal().getJournalIssn()) {
							article.getJournal().getJournalIssn().stream()
																.filter(medCitJournalISSN -> medCitJournalISSN!=null && medCitJournalISSN.getIssn()!=null && !medCitJournalISSN.getIssn().isEmpty())
																.forEach(medCitJournalISSN -> {
							final String[] outerScienceMetrixField = {null};
							final ScienceMetrix[] scienceMetrix = {null};
							scienceMetrix[0] = scienceMetrixService.findByIssn(medCitJournalISSN.getIssn());
							if (scienceMetrix == null)
								scienceMetrix[0] = scienceMetrixService.findByEissn(medCitJournalISSN.getIssn());
							if (scienceMetrix != null)
								outerScienceMetrixField[0] = scienceMetrix[0].getScienceMetrixField();
		
							if (outerScienceMetrixField[0] != null && !outerScienceMetrixField[0].equalsIgnoreCase(""))
							{
		
								//for (ReCiterArticle innerArticle : reCiterArticles) {
									
								reCiterArticles.stream()
											.filter(innerArticle -> innerArticle !=null && innerArticle.getJournal()!=null && innerArticle.getJournal().getJournalIssn()!=null 
															&& innerArticle.getJournal().getJournalIssn().size() > 0)
											.forEach(innerArticle -> {
												
												innerArticle.getJournal().getJournalIssn().stream()
																						  .filter(innerMedCitJournalISSN -> innerMedCitJournalISSN !=null && innerMedCitJournalISSN.getIssn()!=null 
																								  && !innerMedCitJournalISSN.getIssn().isEmpty())
																						  .forEach(innerMedCitJournalISSN -> {
												//for (MedlineCitationJournalISSN innerMedCitJournalISSN : innerArticle.getJournal().getJournalIssn()) {
													String innerScienceMetrixField = "";
													ScienceMetrix innerScienceMetrix = scienceMetrixService
															.findByIssn(innerMedCitJournalISSN.getIssn());
													if (innerScienceMetrix == null)
														innerScienceMetrix = scienceMetrixService
																.findByEissn(innerMedCitJournalISSN.getIssn());
													if (innerScienceMetrix != null)
														innerScienceMetrixField = scienceMetrix[0].getScienceMetrixField();
					
													if (outerScienceMetrixField != null && !outerScienceMetrixField[0].equalsIgnoreCase("")
															&& innerScienceMetrixField != null
															&& !innerScienceMetrixField.equalsIgnoreCase("")
															&& outerScienceMetrixField[0].equalsIgnoreCase(innerScienceMetrixField)) {
														if (innerArticle.getGoldStandard() == 1) {
															countAccepted[0] = countAccepted[0] + 1;
														} else if (innerArticle.getGoldStandard() == -1) {
															countRejected[0] = countRejected[0] + 1;
														}
					
													}
												});
								});
								if(countAccepted[0] > 0 || countRejected[0] > 0)
								{	
									scoreAll[0] = computeScore(countAccepted[0], countRejected[0]);
									scoreWithout1Accepted[0] = computeScore(countAccepted[0] > 0 ? countAccepted[0] - 1 : countAccepted[0],
											countRejected[0]);
									scoreWithout1Rejected[0] = computeScore(countAccepted[0],
											countRejected[0] > 0 ? countRejected[0] - 1 : countRejected[0]);
									
									ReCiterArticleFeedbackScore feedbackJournal = new ReCiterArticleFeedbackScore();
									feedbackJournal.setArticleId(article.getArticleId());
									feedbackJournal.setFeedbackScoreFieldValue(article.getJournal().getJournalTitle());
									feedbackJournal.setAcceptedCount(countAccepted[0]);
									feedbackJournal.setRejectedCount(countRejected[0]);
									feedbackJournal.setScoreAll(scoreAll[0]);
									feedbackJournal.setScoreWithout1Accepted(scoreWithout1Accepted[0]);
									feedbackJournal.setScoreWithout1Rejected(scoreWithout1Rejected[0]);
									feedbackJournal.setFeedbackScore(determineFeedbackScore(article.getGoldStandard(),
											scoreWithout1Accepted[0], scoreWithout1Rejected[0], scoreAll[0]));
				
									feedbackJournalFieldListMap.computeIfAbsent(article.getJournal().getJournalTitle(), k -> new ArrayList<>()).add(feedbackJournal);
									
							
									feedbackJournalFieldListMap.entrySet().stream()
											.filter(entry -> entry.getKey() != null && entry.getValue() != null)
											.sorted(Map.Entry.comparingByKey())
											.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
													(oldValue, newValue) -> oldValue, // merge function
													LinkedHashMap::new // to maintain insertion order
											));
									articleJournalsMap.put(article.getArticleId(), feedbackJournalFieldListMap);
									totalScoresByArticleMap.putIfAbsent(article.getArticleId(), feedbackJournal.getFeedbackScore());
									article.setJournalFeedackScore(feedbackJournal.getFeedbackScore());	
								}
		
								/*List<ReCiterArticle> listofJournals = new ArrayList<>();
								ReCiterFeedbackScoreArticle feedbackJournal = new ReCiterFeedbackScoreArticle();
								//feedbackJournal.setPersonIdentifier(identity.getUid());
								//feedbackJournal.setArticleId(article.getArticleId());
								feedbackJournal.setJournalField(outerScienceMetrixField);
								feedbackJournal.setAcceptedCount(countAccepted[0]);
								feedbackJournal.setRejectedCount(countRejected[0]);
								feedbackJournal.setScoreAll(scoreAll);
								feedbackJournal.setScoreWithout1Accepted(scoreWithout1Accepted);
								feedbackJournal.setScoreWithout1Rejected(scoreWithout1Rejected);
								//feedbackJournal.setGoldStandard(article.getGoldStandard());
								if (article.getGoldStandard() == 1) {
									feedbackJournal.setFeedbackScoreJournalField(scoreWithout1Accepted);
									//article.setFeedbackScoreJournalField(scoreWithout1Accepted);
								} else if (article.getGoldStandard() == -1) {
									feedbackJournal.setFeedbackScoreJournalField(scoreWithout1Rejected);
									//article.setFeedbackScoreJournalField(scoreWithout1Rejected);
								} else {
									feedbackJournal.setFeedbackScoreJournalField(scoreAll);
									//article.setFeedbackScoreJournalField(scoreAll);
								}
								article.setFeedbackScoreArticle(feedbackJournal);
								listofJournals.add(article);
								feedbackJournalsFieldMap.put(outerScienceMetrixField, listofJournals);*/
								
		
							} 
						});
			});
			 if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

					// Printing using forEach
					slf4jLogger.info("********STARTING OF ARTICLE COAUTHOR NAME SCORING********************");
					if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

						String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
								"subscoreType1", "subscoreValue", "subScoreIndividualScore" };
						exportItemLevelFeedbackScores(identity.getUid(), "Journal Field", csvHeaders, articleJournalsMap);

					}
					// printing all the article lookup PMID and CoAuthors associated with other
					// PMIDs.
					String[] csvHeaders = { "PersonIdentifier", "Pmid", "subscoreType1", "subscoreTotal" };
					exportConsolidatedFeedbackScores(identity.getUid(), "Journal Field", csvHeaders,
							totalScoresByArticleMap);

					slf4jLogger.info("********END OF THE ARTICLE COAUTHOR NAME SCORING********************\n");
				} else {
					slf4jLogger.info("********NO FEEDBACK SCORE FOR THE COAUTHOR NAME SECTION********************\n");
				}
			// Calculate the total number of values in the map
			/*if (feedbackJournalsFieldMap != null && feedbackJournalsFieldMap.size() > 0) {
				int totalSize = feedbackJournalsFieldMap.values().stream().mapToInt(List::size).sum();
				slf4jLogger.info("Total number of  feedbackJournalsDomainMap values: ", totalSize);

				// Sorting using Comparator.comparing
				Map<String, List<ReCiterArticle>> feedbackJournalsFieldSortedMap = feedbackJournalsFieldMap
						.entrySet().stream().filter(entry -> entry.getKey() != null && entry.getValue() != null)
						.sorted(Map.Entry.comparingByKey())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
								(oldValue, newValue) -> oldValue, // merge function
								LinkedHashMap::new // to maintain insertion order
						));

				// Printing using forEach
				slf4jLogger.info("********STARTING OF JOURNAL FIELD SCORING********************");
				feedbackJournalsFieldSortedMap.entrySet().stream().forEach(entry -> {
					entry.getValue().stream().forEach(article -> {
						String formattedString = String.format(
								"\nJournal Field Title: %s\nPersonIdentifier: %s\nPMID: %s\nUser Assertion: %s\nCount Accepted:%d\nCountRejected:%d\nCountNull:%d\nFeedback Journal Field Accepted Score: %.3f\nFeedback Journal Field Rejected Score: %.3f\nFeedback Journal Field Score All: %.3f\nFeedback Score Journal Field: %.3f",
								article.getFeedbackScoreArticle().getJournalField(), identity.getUid(), article.getArticleId(),
								article.getGoldStandard(), article.getFeedbackScoreArticle().getAcceptedCount(), article.getFeedbackScoreArticle().getRejectedCount(),
								article.getFeedbackScoreArticle().getCountNull(), article.getFeedbackScoreArticle().getScoreWithout1Accepted(),
								article.getFeedbackScoreArticle().getScoreWithout1Rejected(), article.getFeedbackScoreArticle().getScoreAll(),
								article.getFeedbackScoreArticle().getFeedbackScoreJournalField());
						slf4jLogger.info(formattedString + "\n");
					});
				});
				slf4jLogger.info("********END OF THE JOURNAL FIELD SCORING********************\n");
			} else {
				slf4jLogger.info("********NO FEEDBACK SCORE FOR THE JOURNAL FIELD SECTION********************\n");
			}*/
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
