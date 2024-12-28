package reciter.algorithm.evidence.targetauthor.feedback.journalsubfield.strategy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	
	private Map<String, List<ReCiterArticleFeedbackScore>> feedbackJournalsSubFieldMap = new HashMap<>();
	
	Set<String> filterJournalSubFields = Stream.of("General Science & Technology")
            .collect(Collectors.toSet());
	
	ScienceMetrixService scienceMetrixService = ApplicationContextHolder.getContext()
			.getBean(ScienceMetrixService.class);
	
	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}
	

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		try {
	        slf4jLogger.info("reCiterArticles size: {}", reCiterArticles.size());

	        // Group by gold standard
	        Map<Integer, List<ReCiterArticle>> groupedByGoldStandard = reCiterArticles.stream()
	                .collect(Collectors.groupingBy(ReCiterArticle::getGoldStandard));

	        // Get accepted and rejected articles
	        List<ReCiterArticle> acceptedArticles = groupedByGoldStandard.getOrDefault(1, Collections.emptyList());
	        List<ReCiterArticle> rejectedArticles = groupedByGoldStandard.getOrDefault(-1, Collections.emptyList());

	        // Prepare counts for accepted and rejected articles
	        Map<String, Long> acceptArticlesCountByJournalSubField = countArticlesByJournalSubField(acceptedArticles);
	        Map<String, Long> rejectedArticlesCountByJournalSubField = countArticlesByJournalSubField(rejectedArticles);

	        // Process each article concurrently
	        feedbackJournalsSubFieldMap = reCiterArticles.parallelStream() // Using parallelStream for concurrent processing
	                .filter(article -> article != null && article.getJournal() != null && article.getJournal().getJournalIssn() != null && !article.getJournal().getJournalIssn().isEmpty())
	                .flatMap(article -> article.getJournal().getJournalIssn().stream()
	                        .filter(Objects::nonNull)
	                        .map(journalIssn -> processArticle(article, journalIssn, acceptArticlesCountByJournalSubField, rejectedArticlesCountByJournalSubField))
	                        .filter(Objects::nonNull)
	                )
	                .collect(Collectors.groupingBy(ReCiterArticleFeedbackScore::getFeedbackScoreType));
	        
	        
	        // Calculate total scores
	        double totalScore = feedbackJournalsSubFieldMap.values().stream()
	                .flatMap(List::stream)
	                .mapToDouble(ReCiterArticleFeedbackScore::getFeedbackScore)
	                .sum();

	        // Finalize feedback scores
	        reCiterArticles.parallelStream().forEach(article -> {
	            double articleScore = feedbackJournalsSubFieldMap.values().stream()
	                    .flatMap(List::stream)
	                    .filter(feedback -> feedback.getArticleId() == article.getArticleId())
	                    .mapToDouble(ReCiterArticleFeedbackScore::getFeedbackScore)
	                    .sum();

	            article.setJournalSubFieldFeedbackScore(articleScore);
	            article.setExportedJournalSubFieldFeedbackScore(decimalFormat.format(articleScore));
	        });

	        return totalScore;
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return 0;

	}
	private Map<String, Long> countArticlesByJournalSubField(List<ReCiterArticle> articles) {
	    return articles.parallelStream() // Use parallelStream for faster processing on large datasets
	            .filter(article -> article.getJournal() != null && article.getJournal().getJournalIssn() != null && !article.getJournal().getJournalIssn().isEmpty())
	            .flatMap(article -> article.getJournal().getJournalIssn().stream()
	                    .filter(Objects::nonNull)
	                    .map(this::retrieveJournalSubField)
	                    .filter(Objects::nonNull)
	                    .filter(subField -> !subField.isEmpty())
	            )
	            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	private ReCiterArticleFeedbackScore processArticle(ReCiterArticle article, MedlineCitationJournalISSN journalIssn,
	                                                   Map<String, Long> acceptArticlesCountByJournalSubField,
	                                                   Map<String, Long> rejectedArticlesCountByJournalSubField) {

	    String journalSubField = retrieveJournalSubField(journalIssn);
	    if (journalSubField == null || journalSubField.isEmpty() || filterJournalSubFields.contains(journalSubField)) {
	        return null; // Skip if no valid subfield
	    }

	    int countAccepted = Math.toIntExact(acceptArticlesCountByJournalSubField.getOrDefault(journalSubField, 0L));
	    int countRejected =  Math.toIntExact(rejectedArticlesCountByJournalSubField.getOrDefault(journalSubField, 0L));

	    // Compute the scores
	    double scoreAll = computeScore(countAccepted, countRejected);
	    double scoreWithout1Accepted = computeScore(countAccepted > 0 ? countAccepted - 1 : countAccepted, countRejected);
	    double scoreWithout1Rejected = computeScore(countAccepted, countRejected > 0 ? countRejected - 1 : countRejected);

	    double feedbackScore = determineFeedbackScore(article.getGoldStandard(), scoreWithout1Accepted, scoreWithout1Rejected, scoreAll);
	    String exportedFeedbackScore = decimalFormat.format(feedbackScore);

	    // Create the feedback object
	    return populateArticleFeedbackScore(
	            article.getArticleId(),
	            journalSubField,
	            Math.toIntExact(countAccepted),
	            Math.toIntExact(countRejected),
	            scoreAll,
	            scoreWithout1Accepted,
	            scoreWithout1Rejected,
	            article.getGoldStandard(),
	            feedbackScore,
	            exportedFeedbackScore,
	            "Journal SubField"
	    );
	}

	private String retrieveJournalSubField(MedlineCitationJournalISSN journalIssn) {
	    if (journalIssn == null || journalIssn.getIssn() == null || journalIssn.getIssn().isEmpty()) {
	        return null;
	    }

	    ScienceMetrix scienceMetrix = scienceMetrixService.findByIssn(journalIssn.getIssn());
	    if (scienceMetrix == null) {
	        scienceMetrix = scienceMetrixService.findByEissn(journalIssn.getIssn());
	    }

	    return (scienceMetrix != null && scienceMetrix.getScienceMetrixSubfield() != null && !scienceMetrix.getScienceMetrixSubfield().isEmpty())
	            ? scienceMetrix.getScienceMetrixSubfield()
	            : null;
	}	
}
