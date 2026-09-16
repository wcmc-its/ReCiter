package reciter.algorithm.evidence.targetauthor.feedback.targetauthorname.strategy;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.engine.analysis.evidence.AuthorNameEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterArticleFeedbackScore;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

public class TargetAuthorNameFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(TargetAuthorNameFeedbackStrategy.class);
	Map<String, List<ReCiterArticleFeedbackScore>> feedbackTargetAuthorNameMap = null;
	List<ReCiterAuthor> listOfAuthors = null;

	private static final Pattern PATTERN1 = Pattern.compile("^[A-Z] [A-Z]$");
	private static final Pattern PATTERN2 = Pattern.compile("^[A-Z] [A-Z][a-z]");

	private static final String FULL_EXACT = "full-exact";

	/**
	 * Off by default. When enabled, a byline that full-exact matches a registered
	 * Identity name with no curation history is treated as absence of evidence
	 * (score 0.0) rather than evidence of absence (informed absence penalty).
	 */
	private boolean skipUncuratedRegisteredName = false;

	public void setSkipUncuratedRegisteredName(boolean skipUncuratedRegisteredName) {
		this.skipUncuratedRegisteredName = skipUncuratedRegisteredName;
	}

	/**
	 * True when the byline being scored is a name the institution has already
	 * REGISTERED for this person - primaryName or one of alternateNames - and
	 * which simply has not been curated yet.
	 *
	 * <p>This reuses the name match ReCiter has already computed upstream in
	 * ScoreByNameStrategy, which stores its verdict on the article as
	 * AuthorNameEvidence.nameMatchFirstType; it is not re-derived here. The
	 * registered-name list is then checked to confirm the byline is a name the
	 * institution knows about rather than an incidental exact match.
	 *
	 * <p>A newly registered publishing name has zero accepted and zero rejected
	 * articles for the same reason a brand new account has no history: nobody has
	 * curated it yet. That is not the same signal as a genuinely unfamiliar
	 * journal or coauthor set, which is why this guard is deliberately scoped to
	 * the target author name only.
	 */
	private boolean isUncuratedRegisteredName(ReCiterArticle article, Identity identity, ReCiterAuthor author) {
		if (article == null || identity == null || author == null || author.getAuthorName() == null) {
			return false;
		}
		AuthorNameEvidence nameEvidence = article.getAuthorNameEvidence();
		if (nameEvidence == null || !FULL_EXACT.equalsIgnoreCase(nameEvidence.getNameMatchFirstType())) {
			return false;
		}
		String bylineFirstName = author.getAuthorName().getFirstName();
		String bylineLastName = author.getAuthorName().getLastName();
		if (bylineFirstName == null || bylineFirstName.trim().isEmpty()
				|| bylineLastName == null || bylineLastName.trim().isEmpty()) {
			return false;
		}
		if (isSameRegisteredName(identity.getPrimaryName(), bylineFirstName, bylineLastName)) {
			return true;
		}
		if (identity.getAlternateNames() != null) {
			for (AuthorName alternateName : identity.getAlternateNames()) {
				if (isSameRegisteredName(alternateName, bylineFirstName, bylineLastName)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isSameRegisteredName(AuthorName registeredName, String bylineFirstName, String bylineLastName) {
		if (registeredName == null || registeredName.getFirstName() == null || registeredName.getLastName() == null) {
			return false;
		}
		return registeredName.getFirstName().trim().equalsIgnoreCase(bylineFirstName.trim())
				&& registeredName.getLastName().trim().equalsIgnoreCase(bylineLastName.trim());
	}

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

			// Compute total accepted articles for informed absence penalty
			final int totalAccepted = (int) reCiterArticles.stream()
				.filter(a -> a != null && a.getGoldStandard() == ACCEPTED)
				.count();

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

								// Informed absence: if this author name has never appeared in any
								// accepted or rejected article, but the researcher has substantial
								// acceptance history, apply a negative penalty instead of 0.0
								if (countAccepted == 0 && countRejected == 0 && totalAccepted > 0) {
									if (skipUncuratedRegisteredName
											&& isUncuratedRegisteredName(article, identity, author)) {
										// A registered publishing name that nobody has curated yet is
										// absence of evidence, not evidence of absence. Leave the
										// scores as computed (0.0) rather than penalising the person
										// for the institution having just added the name.
										slf4jLogger.debug(
											"Informed absence skipped for registered but uncurated byline '{}' on article {}",
											authorName, article.getArticleId());
									} else {
										double penalty = computeInformedAbsencePenalty(totalAccepted);
										scoreAll = penalty;
										scoreWithout1Accepted = penalty;
										scoreWithout1Rejected = penalty;
									}
								}

								double feedbackScore= determineFeedbackScore(article.getGoldStandard(),scoreWithout1Accepted, scoreWithout1Rejected, scoreAll);
								String exportedFeedbackScore = decimalFormat.format(feedbackScore);
								
								ReCiterArticleFeedbackScore feedbackTargetAuthorName = populateArticleFeedbackScore(article.getArticleId(),authorName,
										   countAccepted,countRejected,
										   scoreAll,scoreWithout1Accepted,scoreWithout1Rejected,
										   article.getGoldStandard(),feedbackScore,exportedFeedbackScore, "TargetAuthorName");

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
				article.addArticleFeedbackScoresMap(feedbackTargetAuthorNameMap);
				article.setTargetAuthorNameFeedbackScore(totalScore);
				String exportedTargetAuthorNameFeedbackScore = decimalFormat.format(totalScore);
				article.setExportedTargetAuthorNameFeedbackScore(exportedTargetAuthorNameFeedbackScore);
			});

		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
