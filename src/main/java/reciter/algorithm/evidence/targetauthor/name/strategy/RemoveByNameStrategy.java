package reciter.algorithm.evidence.targetauthor.name.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.article.AbstractRemoveReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.string.ReCiterStringUtil;

public class RemoveByNameStrategy extends AbstractRemoveReCiterArticleStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(RemoveByNameStrategy.class);

	private String firstNameFieldVar;

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		boolean shouldRemove = false;
		boolean foundAuthorWithSameFirstName = false;
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();

		// TODO: Optimize: Move this out of the for-each loop because it's the same.
		String targetAuthorFirstName = targetAuthor.getAuthorName().getFirstName();
		String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();
		String targetAuthorMiddleName = targetAuthor.getAuthorName().getMiddleName();

		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String firstName = author.getAuthorName().getFirstName();
				String lastName = author.getAuthorName().getLastName();
				String middleName = author.getAuthorName().getMiddleName();

				// Check whether last name matches.
				if (StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(lastName),  ReCiterStringUtil.deAccent(targetAuthorLastName))) {

					// TODO: Optimize: do not check. can never be null. always empty string.
					firstName = firstName.trim();
					targetAuthorFirstName = targetAuthorFirstName.trim();

					// Check if first name is a full name (not an initial).
					if (firstName.length() > 1 && targetAuthorFirstName.length() > 1) {

						// First name doesn't match! Should remove the article from the selected cluster.
						if (!StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {

							firstNameFieldVar = firstName;
							shouldRemove = true;

							// Check rc_identity_directory. If any of the alias' first name matches the
							// article author's first name, un-do the operation (removal from the selected cluster).
							// So this article should still be in the selected cluster.

							if (shouldRemove) {
								List<AuthorName> aliasList = targetAuthor.getAliasList();
								if (aliasList != null) {
									for (AuthorName authorName : aliasList) {
										String givenName = authorName.getFirstName();
										if (StringUtils.equalsIgnoreCase(givenName, firstName)) {
											shouldRemove = false;
											break;
										}
									}
								}
							}

							// Check first name with dashes removed. (Case: "Juan-miguel" in article and "Juan Miguel"
							// in rc_identity).
							if (shouldRemove) {
								String articleAuthorFirstNameDashRemoved = firstName.replace("-", " ");
								String targetAuthorFirstNameDashRemoved = targetAuthorFirstName.replace("-", " ");
								if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameDashRemoved, articleAuthorFirstNameDashRemoved)) {
									shouldRemove = false;
								}
							}

							// Case: "J-m" in article, and "Juan Miguel" in rc_identity.
							if (shouldRemove) {
								String[] targetAuthorFirstNameArr = targetAuthorFirstName.split("\\s+");
								if (targetAuthorFirstNameArr.length == 2) {
									String firstPartInitial = targetAuthorFirstNameArr[0].substring(0, 1);
									String secondPartInitial = targetAuthorFirstNameArr[1].substring(0, 1);
									String combineWithDash = firstPartInitial + "-" + secondPartInitial;

									if (StringUtils.equalsIgnoreCase(firstName, combineWithDash)) {
										shouldRemove = false;
									}
								}
							}

							// Case: "Bisen" in article and "Bi-Sen" in rc_identity. Remove dash from "Bi-Sen".
							if (shouldRemove) {
								if (targetAuthorFirstName.contains("-")) {
									String targetAuthorFirstNameDashRemoved = targetAuthorFirstName.replace("-", "");
									if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameDashRemoved, firstName)) {
										shouldRemove = false;
									}
								}
							}

							// Case: "B-s" in article and "Bi-Sen" in rc_identity.
							if (shouldRemove) {
								if (targetAuthorFirstName.contains("-") && firstName.contains("-")) {
									String[] targetAuthorFirstNameArr = targetAuthorFirstName.split("-");
									String[] articleAuthorFirstNameArr = firstName.split("-");
									if (targetAuthorFirstNameArr.length == 2 && articleAuthorFirstNameArr.length == 2) {
										String firstPartInitial = targetAuthorFirstNameArr[0].substring(0, 1);
										String secondPartInitial = targetAuthorFirstNameArr[1].substring(0, 1);
										String combineWithDash = firstPartInitial + "-" + secondPartInitial;
										if (StringUtils.equalsIgnoreCase(combineWithDash, firstName)) {
											shouldRemove = false;
										}
									}
								}
							}

							// Check the Levenshtein distance between the target author's first name and the article
							// author's first name, if the distance <= 1 and affiliation score is greater than 0, 
							// un-do the removal operation.
							// Cases:
							// 1. Removed article id=[19061285] with cwid=[nkaltork] and name in article=[Nassar] In gold standard=[1]
							// 2. Removed article id=[18343782] with cwid=[nkaltork] and name in article=[Nassar] In gold standard=[1]
							if (shouldRemove && reCiterArticle.getAffiliationScore() > 0) {
								int levenshteinDistance = ReCiterStringUtil.levenshteinDistance(firstName, targetAuthorFirstName);
								if (levenshteinDistance <= 1) {
									shouldRemove = false;
								}
							}

							// Check the first three characters of each of the first names and compare affiliation score.
							// Case:
							// 1. Removed article id=[19101229] with cwid=[mszulc] and name in article=[Massimo] In gold standard=[1]
							if (shouldRemove && reCiterArticle.getAffiliationScore() > 0) {
								if (targetAuthorFirstName.length() > 4 && firstName.length() > 4) {
									String targetAuthorFirstName3Chars = targetAuthorFirstName.substring(0, 3);
									String articleAuthorFirstName3Chars = firstName.substring(0, 3);
									if (StringUtils.equalsIgnoreCase(targetAuthorFirstName3Chars, articleAuthorFirstName3Chars)) {
										shouldRemove = false;
									}
								}
							}

							if (shouldRemove) {
								String targetAuthorFirstNameMiddleNameCombined = targetAuthorFirstName + targetAuthorMiddleName;
								if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameMiddleNameCombined, firstName)) {
									shouldRemove = false;
								}
							}

							// Case: "Joan h f". Split the first name by white space and use the 0th element (should
							// it exist to check whether the first names match. If they do, un-do the removal operation.
							if (shouldRemove) {
								String[] firstNameArray = firstName.split("\\s+");
								if (firstNameArray.length > 1) {
									firstName = firstNameArray[0];
									if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {
										shouldRemove = false;
									}
								}

								// Check middle name and if they match, un-do removal operation. Case: "Joan h f" in article
								// and "H. F." in db. Compare "hf" with "hf".
								if (shouldRemove) {
									if (firstNameArray.length > 1) {

										// Get h f and combine into single string.
										String authorMiddleNameConcatenated = "";
										for (int i = 1; i < firstNameArray.length; i++) {
											authorMiddleNameConcatenated += firstNameArray[i];
										}

										// Get H. F. from target author and remove '.', '-' and space.
										String targetAuthorMiddleNameConcatenated = "";
										if (targetAuthorMiddleName != null) {
											targetAuthorMiddleNameConcatenated = targetAuthorMiddleName.replaceAll("[.\\-\\s+]", "");
										}

										if (StringUtils.equalsIgnoreCase(authorMiddleNameConcatenated, targetAuthorMiddleNameConcatenated)) {
											shouldRemove = false;
										}
									}
								}
							}
						} else {
							// Handle the case where there are multiple authors with the same last name.
							foundAuthorWithSameFirstName = true;
						}
					} else {
						// check middle name.
						// Case: False Positive List: [2]: [12814220, 21740463] for Anna Bender.
						// Remove this article because middle name exist in article, but not in rc_identity.
						if (middleName.length() > 0 && targetAuthorMiddleName.length() == 0) {
							if (!StringUtils.equalsIgnoreCase(middleName, targetAuthorMiddleName) && reCiterArticle.getAffiliationScore() == 0) {
								shouldRemove = true;
							}
						}

						// case: pmid=11467038, cwid = ajmarcus
						// Middle name doesn't match. Mark for removal.
						if (middleName.length() > 0 && targetAuthorMiddleName.length() > 0) {
							String middleInitial = author.getAuthorName().getMiddleInitial();
							String targetAuthorMiddleInitial = targetAuthor.getAuthorName().getMiddleInitial();

							if (!StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) && reCiterArticle.getAffiliationScore() == 0) {
								shouldRemove = true;
							}
						}
					}
				}
			}
		}

		if (shouldRemove && !foundAuthorWithSameFirstName) {
			slf4jLogger.info("Removed article id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
					targetAuthor.getCwid() + "] and name in article=[" + firstNameFieldVar + "]" +
					" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		return 0;
	}
}
