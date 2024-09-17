package reciter.algorithm.evidence.targetauthor.feedback.journalfield.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.ApplicationContextHolder;
import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.identity.Identity;
import reciter.service.ScienceMetrixService;

public class JournalFieldFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(JournalFieldFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackJournalFieldListMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleJournalsMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();


	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	@SuppressWarnings("unused")
	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());
			ScienceMetrixService scienceMetrixService = ApplicationContextHolder.getContext()
					.getBean(ScienceMetrixService.class);

			 reCiterArticles.stream()
			 			   .filter(article -> article!=null && article.getJournal()!=null && article.getJournal().getJournalIssn()!=null)
			 			   .forEach(article -> {
						final int[] countAccepted = {0};
						final int[] countRejected = {0};
		
						final double[] scoreAll = {0.0};
						final double[] scoreWithout1Accepted = {0.0};
						final double[] scoreWithout1Rejected = {0.0};
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
		
								reCiterArticles.stream()
											.filter(innerArticle -> innerArticle !=null && innerArticle.getJournal()!=null && innerArticle.getJournal().getJournalIssn()!=null 
															&& innerArticle.getJournal().getJournalIssn().size() > 0)
											.forEach(innerArticle -> {
												
												innerArticle.getJournal().getJournalIssn().stream()
																						  .filter(innerMedCitJournalISSN -> innerMedCitJournalISSN !=null && innerMedCitJournalISSN.getIssn()!=null 
																								  && !innerMedCitJournalISSN.getIssn().isEmpty())
																						  .forEach(innerMedCitJournalISSN -> {
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
									article.setJournalFeedackScore(feedbackJournal.getFeedbackScore());	
								}
							} 
						});
			});
			 if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

					// Printing using forEach
					slf4jLogger.info("********STARTING OF ARTICLE JOURNAL FIELD SCORING********************");
					/*if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

						String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
								"subscoreType1", "subscoreValue", "subScoreIndividualScore" };
						exportItemLevelFeedbackScores(identity.getUid(), "Journal Field", csvHeaders, articleJournalsMap);

					}*/
					

					slf4jLogger.info("********END OF THE ARTICLE JOURNAL FIELD NAME SCORING********************\n");
				} else {
					slf4jLogger.info("********NO FEEDBACK SCORE FOR THE JOURNAL FIELD NAME SECTION********************\n");
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
