package reciter.algorithm.evidence.targetauthor.feedback.targetauthorname.strategy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.article.ReCiterFeedbackScoreArticle;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

public class TargetAuthorNameFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(TargetAuthorNameFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackTargetAuthorNameMap = null;
	Map<Long, Map<String, List<ReCiterArticleFeedbackScore>>> articleTargetAuthorNameMap = new HashMap<>();
	Map<Long, Double> totalScoresByArticleMap = new HashMap<>();
	List<ReCiterAuthor> listOfAuthors = null;

	private static final Pattern PATTERN1 = Pattern.compile("^[A-Z] [A-Z]$");
	private static final Pattern PATTERN2 = Pattern.compile("^[A-Z] [A-Z][a-z]");

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	private static String formatAuthorName(String firstName, String lastName) {
		if (PATTERN1.matcher(firstName).matches() || PATTERN2.matcher(firstName).matches()) {
			return firstName + " " + lastName;
		} else {
			return firstName + " " + lastName;
		}
	}

	
	
	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {

		try {
			slf4jLogger.info("reCiterArticles size: " + reCiterArticles.size());
			
			
			Map<String, Map<Integer, Long>> targetAuthorNameCountsByArticleStatus = reCiterArticles.stream()
				    .filter(article -> article != null &&
				                       article.getArticleCoAuthors() != null &&
				                       article.getArticleCoAuthors().getAuthors() != null &&
				                       !article.getArticleCoAuthors().getAuthors().isEmpty())
				    .flatMap(article -> article.getArticleCoAuthors().getAuthors().stream()
				        .filter(author -> author != null &&
				                          author.getAuthorName() != null &&
				                          !author.getAuthorName().getFirstName().isEmpty() &&  !author.getAuthorName().getLastName().isEmpty() &&
				                          author.isTargetAuthor())
				        .map(author -> new AbstractMap.SimpleEntry<>(
				            formatAuthorName(author.getAuthorName().getFirstName(), author.getAuthorName().getLastName()),
				            article.getGoldStandard()
				        ))
				    )
				    .collect(Collectors.groupingBy(
				        Map.Entry::getKey,
				        Collectors.groupingBy(
				            Map.Entry::getValue,
				            Collectors.counting()
				        )
				    ));
			
			// Print results
			/*targetAuthorNameCountsByArticleStatus.forEach((orcid, counts) -> {
		                //int countAccepted = Math.toIntExact(emailCountsByArticleStatus.get(email).get(ACCEPTED));
		                //int countRejected = Math.toIntExact(emailCountsByArticleStatus.get(email).get(REJECTED));
		                System.out.println("Author Name in a Map: " + orcid);// + "-" + countAccepted +" -" + countRejected);
			               
		                counts.forEach((status, count) ->
		                    System.out.println("Artcle Status " + status + ": Articles Count " + count)
		                );
		            });*/

			reCiterArticles.stream()
				.filter(article->article!=null && article.getArticleCoAuthors()!=null && article.getArticleCoAuthors().getAuthors()!=null && article.getArticleCoAuthors().getAuthors().size() > 0)
				.forEach(article->{
					
				ReCiterArticleAuthors coAuthors = article.getArticleCoAuthors();
				listOfAuthors = coAuthors.getAuthors();

				feedbackTargetAuthorNameMap = new HashMap<>();
				
				listOfAuthors.stream()
					.filter(author->author!=null && author.isTargetAuthor())
					.forEach(author->{

					int countAccepted = 0;
					int countRejected = 0;
					double scoreAll = 0.0;
					double scoreWithout1Accepted = 0.0;
					double scoreWithout1Rejected = 0.0;

						String authorName = formatAuthorName(author.getAuthorName().getFirstName(),
								author.getAuthorName().getLastName());

						if (author.getAuthorName().getFirstName() != null
								&& !author.getAuthorName().getFirstName().isEmpty()
								&& author.getAuthorName().getLastName() != null
								&& !author.getAuthorName().getLastName().isEmpty()
								) 
						{
							
								if(targetAuthorNameCountsByArticleStatus!=null && targetAuthorNameCountsByArticleStatus.size() > 0)
								{
									if(targetAuthorNameCountsByArticleStatus.containsKey(authorName))
									{
										if(targetAuthorNameCountsByArticleStatus.get(authorName).containsKey(ACCEPTED))
										{
											countAccepted = Math.toIntExact(targetAuthorNameCountsByArticleStatus.get(authorName).get(ACCEPTED));
										}
										if(targetAuthorNameCountsByArticleStatus.get(authorName).containsKey(REJECTED))
										{
											countRejected = Math.toIntExact(targetAuthorNameCountsByArticleStatus.get(authorName).get(REJECTED));
										}
									}
								}
							
								scoreAll = computeScore(countAccepted, countRejected);
								scoreWithout1Accepted = computeScore(
										countAccepted > 0 ? countAccepted - 1 : countAccepted, countRejected);
								scoreWithout1Rejected = computeScore(countAccepted,
										countRejected > 0 ? countRejected - 1 : countRejected);
								
								
								ReCiterArticleFeedbackScore feedbackTargetAuthorName = populateArticleFeedbackScore(article.getArticleId(),authorName,
										   countAccepted,countRejected,
										   scoreAll,scoreWithout1Accepted,scoreWithout1Rejected,
										   article.getGoldStandard(),null);

								feedbackTargetAuthorNameMap.computeIfAbsent(Long.toString(article.getArticleId()), k -> new ArrayList<>()).add(feedbackTargetAuthorName);
					}
				});
				
				 double totalScore = feedbackTargetAuthorNameMap.values().stream().flatMap(List::stream) // Flatten the lists into a single stream of ReCiterFeedbackScoreCoAuthorName
							.mapToDouble(score-> score.getFeedbackScore()) // Extract the scores
							.sum(); // Sum all scores
				
				 feedbackTargetAuthorNameMap.entrySet().stream()
				.filter(entry -> entry.getKey() != null && entry.getValue() != null)
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, // merge function
						LinkedHashMap::new // to maintain insertion order
				));
				articleTargetAuthorNameMap.put(article.getArticleId(), feedbackTargetAuthorNameMap);
				totalScoresByArticleMap.put(article.getArticleId(), totalScore);
				article.setTargetAuthorNameFeedbackScore(totalScore);
				String exportedTargetAuthorNameFeedbackScore = decimalFormat.format(totalScore);
				System.out.println("Exported OrCid CoAuthor article Score***************"+exportedTargetAuthorNameFeedbackScore);
				article.setExportedTargetAuthorNameFeedbackScore(exportedTargetAuthorNameFeedbackScore);
			});

			 if (articleTargetAuthorNameMap != null && articleTargetAuthorNameMap.size() > 0) {
					
					// Printing using forEach
					slf4jLogger.info("********STARTING OF ARTICLE TARGET AUTHOR NAME SCORING********************");
					if (articleTargetAuthorNameMap != null && articleTargetAuthorNameMap.size() > 0) {

						String[] csvHeaders = { "PersonIdentifier", "Pmid", "CountAccepted", "CountRejected",
								"subscoreType1", "subscoreValue", "subScoreIndividualScore","UserAssertion" };
						exportItemLevelFeedbackScores(identity.getUid(), "TargetAuthorName", csvHeaders, articleTargetAuthorNameMap);

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

}
