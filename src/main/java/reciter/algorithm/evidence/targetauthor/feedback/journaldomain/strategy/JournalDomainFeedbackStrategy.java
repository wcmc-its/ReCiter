package reciter.algorithm.evidence.targetauthor.feedback.journaldomain.strategy;

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

import reciter.ApplicationContextHolder;
import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterFeedbackScoreArticle;
import reciter.model.identity.Identity;
import reciter.model.pubmed.MedlineCitationJournalISSN;
import reciter.service.ScienceMetrixService;

public class JournalDomainFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(JournalDomainFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackJournalDomainListMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleJournalsMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		
		StopWatch stopWatchforCoAuthorFeedback = new StopWatch("Journal Domain");
		stopWatchforCoAuthorFeedback.start("Journal Domain");
		
		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());

			ScienceMetrixService scienceMetrixService = ApplicationContextHolder.getContext()
					.getBean(ScienceMetrixService.class);
			//for (ReCiterArticle article : reCiterArticles) {
			reCiterArticles.stream()
				.filter(article -> article.getJournal()!=null && article.getJournal().getJournalIssn().size() > 0)
				.forEach(article -> {
					int[] countAccepted = {0};
					int[] countRejected = {0};
					
					double[] scoreAll = {0.0};
					double[] scoreWithout1Accepted = {0.0};
					double[] scoreWithout1Rejected = {0.0};
					//for (MedlineCitationJournalISSN medCitJournalISSN : article.getJournal().getJournalIssn()) {
					article.getJournal().getJournalIssn().stream()
						.filter(medCitJournalISSN -> medCitJournalISSN!=null && medCitJournalISSN.getIssn()!=null)
						.forEach(medCitJournalISSN -> {
							String[] outerScienceMetrixDomain = {null};
							final ScienceMetrix[] scienceMetrix = {null};
							scienceMetrix[0] = scienceMetrixService.findByIssn(medCitJournalISSN.getIssn());
							if (scienceMetrix[0] == null)
								scienceMetrix[0] = scienceMetrixService.findByEissn(medCitJournalISSN.getIssn());
							if (scienceMetrix[0] != null)
								outerScienceMetrixDomain[0] = scienceMetrix[0].getScienceMetrixDomain();
								  
								reCiterArticles.stream()
								  		.filter(innerArticle -> innerArticle!=null && innerArticle.getJournal()!=null && innerArticle.getJournal()
												.getJournalIssn()!=null)	
								  		.forEach(innerArticle -> {
									/*for (MedlineCitationJournalISSN innerMedCitJournalISSN : innerArticle.getJournal()
											.getJournalIssn()) {*/
								  		innerArticle.getJournal().getJournalIssn().stream()
								  			.filter(innerMedCitJournalISSN -> innerMedCitJournalISSN!=null && innerMedCitJournalISSN.getIssn()!=null)
								  			.forEach(innerMedCitJournalISSN -> {
											  		String innerScienceMetrixDomain = "";
													ScienceMetrix innerScienceMetrix = scienceMetrixService
															.findByIssn(innerMedCitJournalISSN.getIssn());
													if (innerScienceMetrix == null)
														innerScienceMetrix = scienceMetrixService
																.findByEissn(innerMedCitJournalISSN.getIssn());
													if (innerScienceMetrix != null)
														innerScienceMetrixDomain = scienceMetrix[0].getScienceMetrixDomain();
					
													if (outerScienceMetrixDomain != null && outerScienceMetrixDomain.length >0 && !outerScienceMetrixDomain[0].equalsIgnoreCase("")
															&& innerScienceMetrixDomain != null
															&& !innerScienceMetrixDomain.equalsIgnoreCase("")
															&& outerScienceMetrixDomain[0].equalsIgnoreCase(innerScienceMetrixDomain)) {
														if (innerArticle.getGoldStandard() == 1) {
															countAccepted[0] = countAccepted[0] + 1;
														} else if (innerArticle.getGoldStandard() == -1) {
															countRejected[0] = countRejected[0] + 1;
														} 
													}
									});
								});
		
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
								
								feedbackJournalDomainListMap.computeIfAbsent(outerScienceMetrixDomain[0],  k -> new ArrayList<>()).add(feedbackJournal);
					});
					double totalScore = feedbackJournalDomainListMap.values().stream().flatMap(List::stream) // Flatten the lists into a single stream of ReCiterFeedbackScoreCoAuthorName
							.mapToDouble(score-> score.getFeedbackScore()) // Extract the scores
							.sum(); // Sum all scores

					// Sort Map Contents before storing into another Map
					feedbackJournalDomainListMap.entrySet().stream()
							.filter(entry -> entry.getKey() != null && entry.getValue() != null)
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
									(oldValue, newValue) -> oldValue, // merge function
									LinkedHashMap::new // to maintain insertion order
							));
					articleJournalsMap.put(article.getArticleId(), feedbackJournalDomainListMap);
					totalScoresByArticleMap.put(article.getArticleId(), totalScore);
					article.setJournalFeedackScore(totalScore);	
			});
			if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

				// Printing using forEach
				slf4jLogger.info("********STARTING OF ARTICLE COAUTHOR NAME SCORING********************");
				if (articleJournalsMap != null && articleJournalsMap.size() > 0) {

					String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
							"subscoreType1", "subscoreValue", "subScoreIndividualScore" };
					exportItemLevelFeedbackScores(identity.getUid(), "Journal", csvHeaders, articleJournalsMap);

				}
				// printing all the article lookup PMID and CoAuthors associated with other
				// PMIDs.
				String[] csvHeaders = { "PersonIdentifier", "Pmid", "subscoreType1", "subscoreTotal" };
				exportConsolidatedFeedbackScores(identity.getUid(), "Journal", csvHeaders,
						totalScoresByArticleMap);

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
		return 0.0;
	}
}
