package reciter.algorithm.evidence.targetauthor.feedback.institution.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.feedback.targetauthor.AbstractTargetAuthorFeedbackStrategy;
import reciter.engine.EngineParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.article.ReCiterFeedbackScoreArticle;
import reciter.model.identity.Identity;

public class InstitutionFeedbackStrategy extends AbstractTargetAuthorFeedbackStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(InstitutionFeedbackStrategy.class);
	private static final List<String> targetKeywords = Arrays.asList("Center", "Centre", "Centro", "Clinic", "Colegio",
			"College", "Corporation", "Foundation", "Health System", "Hospital", "Institut", "Institute", "Institution",
			"Istituto", "Klinik", "LLC", "New York Presbyterian", "New York-Presbyterian", "NewYork Presbyterian",
			"NewYork-Presbyterian", "NYU ", "Pharmaceuticals", "School ", "Sloan Kettering", "Universidad",
			"Université", "University", "Weill");

	Map<String, List<ReCiterArticle>> feedbackInstitutionMap = new HashMap<>();
	List<ReCiterAuthor> listOfAuthors = null;
	List<ReCiterArticle> validInstitutions = null;

	@Override
	public double executeFeedbackStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		return 0.0;

	}

	private static int getMinPosition(int... positions) {
		int min = Integer.MAX_VALUE;
		for (int pos : positions) {
			if (pos != -1 && pos < min) {
				min = pos;
			}
		}
		return min == Integer.MAX_VALUE ? 600 : min;
	}

	private static boolean isValidInstitution(String component, int maxCharLength) {
		if (component.length() >= maxCharLength) {
			return false;
		}
		if (component.matches("^[0-9].*") || component.matches("and .*")) {
			return false;
		}
		if (component.startsWith("Department of") || component.startsWith("Division") || component.startsWith(")")) {
			return false;
		}
		for (String keyword : targetKeywords) {
			if (component.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double executeFeedbackStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {

		try {
			validInstitutions = new ArrayList<>();
			for (ReCiterArticle article : reCiterArticles) {
				for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
					
					if (author.isTargetAuthor() && author.getAffiliation() != null
							&& author.getAffiliation().length() < 850) {

						String affiliation = author.getAffiliation().replaceAll(EngineParameters.getRegexForStopWords(),
								"");

						affiliation = affiliation.replaceAll("^[0-9 ]+", "");
						Pattern pattern = Pattern.compile("^[A-Z]\\.[A-Z]\\.");
						Matcher matcher = pattern.matcher(affiliation);
						int capitalPatternCount = 0;
						// Iterate over all matches
						while (matcher.find()) {
							capitalPatternCount++;
						}

						int lowercaseUppercaseCount = 0;
						Pattern pattern1 = Pattern.compile("[a-z][A-Z]");
						Matcher matcher1 = pattern1.matcher(affiliation);
						while (matcher1.find()) {
							lowercaseUppercaseCount++;
						}
						if (capitalPatternCount < 4 && lowercaseUppercaseCount < 3) {
							int curPos = 0;
							int charLength = author.getAffiliation().length();
							String curLabel = author.getAffiliation();
							int maxCharLength = 120;
							while (curPos <= charLength) {
								int nextComma = curLabel.indexOf(',', curPos);
								int nextPeriod = curLabel.indexOf('.', curPos);
								int nextSemicolon = curLabel.indexOf(';', curPos);
								int nextParenthesis = curLabel.indexOf('(', curPos);

								int minPos = getMinPosition(nextComma, nextPeriod, nextSemicolon, nextParenthesis,
										charLength + 1);

								String outerArticleInstitution;
								if (minPos != charLength + 1) {
									outerArticleInstitution = curLabel.substring(curPos, minPos).trim();
									curPos = minPos + 1;
								} else {
									outerArticleInstitution = curLabel.substring(curPos).trim();
									curPos = charLength + 1;
								}

								if (isValidInstitution(outerArticleInstitution, maxCharLength)) 
								{
									ReCiterFeedbackScoreArticle feedbackScoreInstitution = new ReCiterFeedbackScoreArticle();
									//feedbackScoreInstitution.setPersonIdentifier(identity.getUid());
									//feedbackScoreInstitution.setArticleId(article.getArticleId());
									feedbackScoreInstitution.setInstitution(outerArticleInstitution);
									article.setFeedbackScoreArticle(feedbackScoreInstitution);
									validInstitutions.add(article);

								}
							}
							if (curPos > charLength) {
								break;
							}
						}

					}

				}
			}

			//for (ReCiterArticle feedbackInst : validInstitutions) {
			validInstitutions.stream()					
					.forEach(feedbackInst->{
				//int countAccepted = 0;
				final int[] countAccepted = {0};
				//int countRejected = 0;
				final int[] countRejected = {0};
				int countNull = 0;
				double scoreAll = 0.0;
				double scoreWithout1Accepted = 0.0;
				double scoreWithout1Rejected = 0.0;
				String outerInstitution = feedbackInst.getFeedbackScoreArticle().getInstitution();
				if (feedbackInstitutionMap != null && !feedbackInstitutionMap.containsKey(outerInstitution)) {
					//for (ReCiterArticle innerFeedbackInst : validInstitutions) {
					
					validInstitutions.stream()
							.filter(innerFeedbackInst->innerFeedbackInst!=null && innerFeedbackInst.getFeedbackScoreArticle()!=null && innerFeedbackInst.getFeedbackScoreArticle().getInstitution()!=null
							&& outerInstitution != null && outerInstitution.isEmpty()
							/*&& innerInstitution != null && innerInstitution.isEmpty()*/)
							.forEach(innerFeedbackInst->{

						String innerInstitution = innerFeedbackInst.getFeedbackScoreArticle().getInstitution();
						if (outerInstitution != null && !outerInstitution.equalsIgnoreCase("")
								&& innerInstitution != null && !innerInstitution.equalsIgnoreCase("")
								&& outerInstitution.equalsIgnoreCase(innerInstitution)) {
							if (innerFeedbackInst != null && innerFeedbackInst.getGoldStandard() == 1) {

								countAccepted[0] = countAccepted[0] + 1;
							} else if (innerFeedbackInst != null && innerFeedbackInst.getGoldStandard() == -1) {
								countRejected[0] = countRejected[0] + 1;
							}

						}
					});
					scoreAll = computeScore(countAccepted[0], countRejected[0]);
					scoreWithout1Accepted = computeScore(countAccepted[0] > 0 ? countAccepted[0] - 1 : countAccepted[0],
							countRejected[0]);
					scoreWithout1Rejected = computeScore(countAccepted[0],
							countRejected[0] > 0 ? countRejected[0] - 1 : countRejected[0]);

					List<ReCiterArticle> listofInstitutions = new ArrayList<>();
					ReCiterFeedbackScoreArticle feedbackScoreInstitution = new ReCiterFeedbackScoreArticle();
					//feedbackScoreInstitution.setPersonIdentifier(identity.getUid());
					//feedbackScoreInstitution.setArticleId(feedbackInst.getArticleId());
					feedbackScoreInstitution.setInstitution(outerInstitution);
					feedbackScoreInstitution.setAcceptedCount(countAccepted[0]);
					feedbackScoreInstitution.setRejectedCount(countRejected[0]);
					feedbackScoreInstitution.setCountNull(countNull);
					feedbackScoreInstitution.setScoreAll(scoreAll);
					feedbackScoreInstitution.setScoreWithout1Accepted(scoreWithout1Accepted);
					feedbackScoreInstitution.setScoreWithout1Rejected(scoreWithout1Rejected);
					//feedbackScoreInstitution.setGoldStandard(feedbackInst.getGoldStandard());
					if (feedbackInst.getGoldStandard() == 1) {
						feedbackScoreInstitution.setFeedbackScoreInstitution(scoreWithout1Accepted);
					} else if (feedbackInst.getGoldStandard() == -1) {
						feedbackScoreInstitution.setFeedbackScoreInstitution(scoreWithout1Rejected);
					} else {
						feedbackScoreInstitution.setFeedbackScoreInstitution(scoreAll);
					}
					feedbackInst.setFeedbackScoreArticle(feedbackScoreInstitution);
					listofInstitutions.add(feedbackInst);
					feedbackInstitutionMap.put(outerInstitution, listofInstitutions);
				} else {
					List<ReCiterArticle> listofInstitutions = feedbackInstitutionMap
							.get(outerInstitution);

					Optional<ReCiterArticle> existingFeedbackScoreInstitutionalOptional = Optional
							.ofNullable(listofInstitutions).filter(list -> !list.isEmpty()).map(list -> list.get(0));

					ReCiterFeedbackScoreArticle feedbackScoreInstitution = new ReCiterFeedbackScoreArticle();

					existingFeedbackScoreInstitutionalOptional.ifPresent(existingInstitution -> {
						//feedbackScoreInstitution.setPersonIdentifier(identity.getUid());
						//feedbackScoreInstitution.setArticleId(feedbackInst.getArticleId());
						feedbackScoreInstitution.setInstitution(outerInstitution);
						feedbackScoreInstitution.setAcceptedCount(existingInstitution.getFeedbackScoreArticle().getAcceptedCount());
						feedbackScoreInstitution.setRejectedCount(existingInstitution.getFeedbackScoreArticle().getRejectedCount());
						feedbackScoreInstitution.setCountNull(existingInstitution.getFeedbackScoreArticle().getCountNull());
						feedbackScoreInstitution.setScoreAll(existingInstitution.getFeedbackScoreArticle().getScoreAll());
						feedbackScoreInstitution
								.setScoreWithout1Accepted(existingInstitution.getFeedbackScoreArticle().getScoreWithout1Accepted());
						feedbackScoreInstitution
								.setScoreWithout1Rejected(existingInstitution.getFeedbackScoreArticle().getScoreWithout1Rejected());
						//feedbackScoreInstitution.setGoldStandard(feedbackInst.getGoldStandard());
						if (feedbackInst.getGoldStandard() == 1) {
							feedbackScoreInstitution
									.setFeedbackScoreInstitution(existingInstitution.getFeedbackScoreArticle().getScoreWithout1Accepted());
						} else if (feedbackInst.getGoldStandard() == -1) {
							feedbackScoreInstitution
									.setFeedbackScoreInstitution(existingInstitution.getFeedbackScoreArticle().getScoreWithout1Rejected());
						} else {
							feedbackScoreInstitution.setFeedbackScoreInstitution(existingInstitution.getFeedbackScoreArticle().getScoreAll());
						}
						feedbackInst.setFeedbackScoreArticle(feedbackScoreInstitution);
						Optional.ofNullable(listofInstitutions).ifPresent(list -> list.add(feedbackInst));
					});
				}

			});

			// Calculate the total number of values in the map
			if (feedbackInstitutionMap != null && feedbackInstitutionMap.size() > 0) {
				//int totalSize = feedbackInstitutionMap.values().stream().mapToInt(List::size).sum();

				// Sorting using Comparator.comparing
				Map<String, List<ReCiterArticle>> reCiterfeedbackInstitutionMapSortedMap = null;
				try {
					reCiterfeedbackInstitutionMapSortedMap = feedbackInstitutionMap.entrySet().stream()
							.filter(entry -> entry.getKey() != null && entry.getValue() != null)
							.sorted(Map.Entry.comparingByKey())
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
									(oldValue, newValue) -> oldValue, // merge function
									LinkedHashMap::new // to maintain insertion order
							));
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Printing using forEach
				slf4jLogger.info("********STARTING OF INSTITUTION SCORING********************");
				reCiterfeedbackInstitutionMapSortedMap.entrySet().stream().forEach(entry -> {
					entry.getValue().stream().forEach(article -> {
						String formattedString = String.format(
								"\nInstitution Title: %s\nPersonIdentifier: %s\nPMID: %s\nUser Assertion: %s\nCount Accepted:%d\nCountRejected:%d\nCountNull:%d\nFeedback Institution Accepted Score: %.3f\nFeedback Institution Rejected Score: %.3f\nFeedback Institution Score All: %.3f\nFeedback Score Institution: %.3f",
								article.getFeedbackScoreArticle().getInstitution(), identity.toString(), article.getArticleId(),
								article.getGoldStandard(), article.getFeedbackScoreArticle().getAcceptedCount(), article.getFeedbackScoreArticle().getRejectedCount(),
								article.getFeedbackScoreArticle().getCountNull(), article.getFeedbackScoreArticle().getScoreWithout1Accepted(),
								article.getFeedbackScoreArticle().getScoreWithout1Rejected(), article.getFeedbackScoreArticle().getScoreAll(),
								article.getFeedbackScoreArticle().getFeedbackScoreInstitution());
						System.out.println(formattedString + "\n");
					});
				});
				slf4jLogger.info("********END OF THE INSTITUTION PMID SCORING********************\n");
			} else {
				slf4jLogger.info("********NO FEEDBACK SCORE FOR THE INSTITUTION SECTION********************\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
}
