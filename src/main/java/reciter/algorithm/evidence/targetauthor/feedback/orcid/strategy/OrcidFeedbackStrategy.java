package reciter.algorithm.evidence.targetauthor.feedback.orcid.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.article.ReCiterFeedbackScoreArticle;
import reciter.model.identity.Identity;

public class OrcidFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(OrcidFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackOrcidMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleOrcidMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();
	List<ReCiterAuthor> listOfAuthors = null;
	private static final int ARTICLE_ACCEPTED_GOLD_STANDARD = 1;
	private static final int ARTICLE_REJECTED_GOLD_STANDARD = -1;
	

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}
	
	public static Map<String, Long> countCoAuthorsByOrcid(List<ReCiterArticle> articles, int articleStatus) {
        return articles.stream()
                .filter(article -> article!=null && article.getGoldStandard() == articleStatus)
                .map(ReCiterArticle::getArticleCoAuthors)
                .filter(Objects::nonNull) 
                .map(ReCiterArticleAuthors::getAuthors)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(author -> author != null && author.getOrcid() != null && !author.getOrcid().isEmpty() && author.isTargetAuthor())
                .collect(Collectors.groupingBy(
                        ReCiterAuthor::getOrcid,
                        Collectors.counting()
                ));
    }

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		StopWatch stopWatchforOrcidFeedback = new StopWatch("Orcid");
		stopWatchforOrcidFeedback.start("Orcid");
		try {
			slf4jLogger.info("reCiterArticles size: ", reCiterArticles.size());

			List<ReCiterArticle> filteredArticles = reCiterArticles.stream()
					.filter(article -> article.getGoldStandard() != 0)
					 .collect(Collectors.toList());
		
			//Count co-authors grouped by ORCID for accepted articles
			Map<String, Long> acceptedCounts = countCoAuthorsByOrcid(filteredArticles, ARTICLE_ACCEPTED_GOLD_STANDARD);
	        
	        // Count co-authors grouped by ORCID for rejected articles
	        Map<String, Long> rejectedCounts = countCoAuthorsByOrcid(filteredArticles, ARTICLE_REJECTED_GOLD_STANDARD);
			
	        reCiterArticles.stream()
			.filter(article-> article!=null && article.getArticleCoAuthors()!=null && article.getArticleCoAuthors().getAuthors()!=null && article.getArticleCoAuthors().getAuthors().size()>0)
					.forEach(article->{
						listOfAuthors  = article.getArticleCoAuthors().getAuthors();
						feedbackOrcidMap = new HashMap<>();
					
				listOfAuthors.stream()
							.filter(author->author != null && author.isTargetAuthor() && author.getOrcid() != null && !author.getOrcid().isEmpty() )
							.forEach(author -> {

					 int countAccepted = 0;
					 int countRejected = 0;
					 double scoreAll = 0.0;
					 double scoreWithout1Accepted = 0.0;
					 double scoreWithout1Rejected = 0.0;
					
					if(acceptedCounts!=null && acceptedCounts.size() >0 && acceptedCounts.containsKey(author.getOrcid()))
						countAccepted = Math.toIntExact(acceptedCounts.get(author.getOrcid()));
	
					if(rejectedCounts!=null && rejectedCounts.size() > 0 && rejectedCounts.containsKey(author.getOrcid()))
						countRejected = Math.toIntExact(rejectedCounts.get(author.getOrcid()));
					
							scoreAll = computeScore(countAccepted, countRejected);
							scoreWithout1Accepted = computeScore(countAccepted > 0 ? countAccepted - 1 : countAccepted,
									countRejected);
							scoreWithout1Rejected = computeScore(countAccepted,
									countRejected > 0 ? countRejected - 1 : countRejected);

							
							ReCiterArticleFeedbackScore feedbackScoreOrcid = populateArticleFeedbackScore(article.getArticleId(),author.getOrcid(),
									   countAccepted,countRejected,
									   scoreAll,scoreWithout1Accepted,
									   scoreWithout1Rejected,article.getGoldStandard(),null);	
		
							feedbackOrcidMap.computeIfAbsent(author.getOrcid(), k -> new ArrayList<>()).add(feedbackScoreOrcid);	


						//});
				});
				double totalScore = feedbackOrcidMap.values().stream().flatMap(List::stream) // Flatten the lists into a single stream of ReCiterFeedbackScoreCoAuthorName
						.mapToDouble(score-> score.getFeedbackScore()) // Extract the scores
						.sum(); // Sum all scores*/

				// Sort Map Contents before storing into another Map
				feedbackOrcidMap.entrySet().stream()
						.filter(entry -> entry.getKey() != null && entry.getValue() != null)
						.sorted(Map.Entry.comparingByKey())
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
								(oldValue, newValue) -> oldValue, // merge function
								LinkedHashMap::new // to maintain insertion order
						));
				articleOrcidMap.put(article.getArticleId(), feedbackOrcidMap);
				totalScoresByArticleMap.put(article.getArticleId(), totalScore);
				article.setOrcidFeedbackScore(totalScore);
				String exportedOrcidFeedbackScore = decimalFormat.format(totalScore);
				System.out.println("Exported OrCid CoAuthor article Score***************"+exportedOrcidFeedbackScore);
				article.setExportedOrcidFeedbackScore(exportedOrcidFeedbackScore);
			});
			if (articleOrcidMap != null && articleOrcidMap.size() > 0) {

				// Printing using forEach
				slf4jLogger.info("********STARTING OF ARTICLE ORCID SCORING********************");
				if (articleOrcidMap != null && articleOrcidMap.size() > 0) {

					String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
							"subscoreType1", "subscoreValue", "subScoreIndividualScore" };
					exportItemLevelFeedbackScores(identity.getUid(), "Orcid", csvHeaders, articleOrcidMap);

				}
				slf4jLogger.info("********END OF THE ARTICLE ORCID SCORING********************\n");
			} else {
				slf4jLogger.info("********NO FEEDBACK SCORE FOR THE ORCID SECTION********************\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		stopWatchforOrcidFeedback.stop();
		slf4jLogger.info(stopWatchforOrcidFeedback.getId() + " took "
				+ stopWatchforOrcidFeedback.getTotalTimeSeconds() + "s"); 
		return 0;
	}
}
