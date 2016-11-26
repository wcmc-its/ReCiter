package reciter.algorithm.evidence.targetauthor.name.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.article.AbstractRemoveReCiterArticleStrategy;
import reciter.database.mongo.model.Identity;
import reciter.engine.erroranalysis.AnalysisObjectAuthor;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.utils.ReCiterStringUtil;

public class RemoveByNameStrategy extends AbstractRemoveReCiterArticleStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(RemoveByNameStrategy.class);

	private String firstNameFieldVar;
	private String middleNameFieldVar;

	private ReCiterAuthor getCorrectAuthor(ReCiterArticle reCiterArticle, Identity identity) {
		String targetAuthorLastName = identity.getAuthorName().getLastName();
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		ReCiterAuthor correctAuthor = null;
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String lastName = author.getAuthorName().getLastName();
				if (StringUtils.equalsIgnoreCase(targetAuthorLastName, lastName)) {
					String firstInitial = author.getAuthorName().getFirstInitial();
					if (StringUtils.equalsIgnoreCase(firstInitial, identity.getAuthorName().getFirstInitial())) {
						correctAuthor = author;
					}
				}
			}
		}
		return correctAuthor;
	}

	private boolean isMultipleAuthorsWithSameLastNameAsTargetAuthor(ReCiterArticle reCiterArticle, Identity identity) {
		int count = 0;
		String targetAuthorLastName = identity.getAuthorName().getLastName();
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String lastName = author.getAuthorName().getLastName();
				if (StringUtils.equalsIgnoreCase(targetAuthorLastName, lastName)) {
					count++;
				}
			}
		}

		return count > 1;
	}

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {

		List<AnalysisObjectAuthor> analysisObjectAuthors = new ArrayList<AnalysisObjectAuthor>();
		
		boolean shouldRemove = false;
		boolean foundAuthorWithSameFirstName = false;
		boolean foundMatchingAuthor = false; // found matching author with the same last name and first and middle initial as target author.

		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();

		String targetAuthorFirstName = identity.getAuthorName().getFirstName();
		String targetAuthorFirstNameInitial = identity.getAuthorName().getFirstInitial();
		String targetAuthorLastName = identity.getAuthorName().getLastName();
		String targetAuthorMiddleName = identity.getAuthorName().getMiddleName();
		String targetAuthorMiddleNameInitial = identity.getAuthorName().getMiddleInitial();

		boolean isMultipleAuthorsWithSameLastNameAsTargetAuthor = isMultipleAuthorsWithSameLastNameAsTargetAuthor(
				reCiterArticle, identity);

		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {

				AnalysisObjectAuthor analysisObjectAuthor = new AnalysisObjectAuthor();
				analysisObjectAuthor.setAuthor(author);
				
				// Update author with the correct author.
				if (isMultipleAuthorsWithSameLastNameAsTargetAuthor) {
					author = getCorrectAuthor(reCiterArticle, identity);
				}

				if (author != null) {
					foundMatchingAuthor = true;
					String firstName = author.getAuthorName().getFirstName();
					String firstNameInitial = author.getAuthorName().getFirstInitial();
					String lastName = author.getAuthorName().getLastName();
					String middleName = author.getAuthorName().getMiddleName();
					String middleNameInitial = author.getAuthorName().getMiddleInitial();

					// Check whether last name matches.
					if (StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(lastName),  ReCiterStringUtil.deAccent(targetAuthorLastName))) {
						analysisObjectAuthor.setLastNameMatchTargetAuthor(true);
						
						firstNameFieldVar = firstName;
						middleNameFieldVar = middleName;

						// Check if first name is a full name (not an initial).
						if (firstName.length() > 1 && targetAuthorFirstName.length() > 1) {
							analysisObjectAuthor.setFirstNameFullName(true);
							
							// First name doesn't match! Should remove the article from the selected cluster.
							if (!StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {

								shouldRemove = true;

								// Check rc_identity_directory. If any of the alias' first name matches the
								// article author's first name, un-do the operation (removal from the selected cluster).
								// So this article should still be in the selected cluster.

								if (shouldRemove) {
									List<AuthorName> aliasList = identity.getAliases();
									if (aliasList != null) {
										for (AuthorName authorName : aliasList) {
											String givenName = authorName.getFirstName();
											String aliasMiddleNameInitial = authorName.getMiddleInitial();
											if (StringUtils.equalsIgnoreCase(givenName, firstName)) {
												// Need to compare middle initial in cases of bsg2001. pmid = 16961803.
												if (middleNameInitial.length() > 0) {
													boolean isMatch = StringUtils.equalsIgnoreCase(aliasMiddleNameInitial, middleNameInitial);
													if (isMatch) {
														analysisObjectAuthor.setAliasNameMatch(true);
														shouldRemove = false;
														break;
													}
												} else {
													analysisObjectAuthor.setAliasNameMatch(true);
													shouldRemove = false;
													break;
												}
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
										analysisObjectAuthor.setFirstNameDashRemovedMatch(true);
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
											analysisObjectAuthor.setFirstInitialDashRemovedMatch(true);
											shouldRemove = false;
										}
									}
								}

								// Case: "Bisen" in article and "Bi-Sen" in rc_identity. Remove dash from "Bi-Sen".
								if (shouldRemove) {
									if (targetAuthorFirstName.contains("-")) {
										String targetAuthorFirstNameDashRemoved = targetAuthorFirstName.replace("-", "");
										if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameDashRemoved, firstName)) {
											analysisObjectAuthor.setFirstNameDashAddedMatch(true);
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
												analysisObjectAuthor.setFirstNameDashInitialMatch(true);
												shouldRemove = false;
											}
										}
									}
								}

								// Case: PMID=10234135, author name in PubMed = "Mj Roman". First initial and middle initial
								// are concatenated.
								if (shouldRemove) {
									if (firstName.length() == 2) {
										if (StringUtils.equalsIgnoreCase(firstNameInitial, targetAuthorFirstNameInitial) &&
												StringUtils.equalsIgnoreCase(middleNameInitial, targetAuthorMiddleNameInitial)) {
											analysisObjectAuthor.setFirstInitialMiddleInitialConcatenatedMatch(true);
											shouldRemove = false;
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
										analysisObjectAuthor.setLevenshteinDistanceMatch(true);
										shouldRemove = false;
									}
								}

								// Check the first three characters of each of the first names and compare affiliation score.
								// Case:
								// 1. Removed article id=[19101229] with cwid=[mszulc] and name in article=[Massimo] In gold standard=[1]
								if (shouldRemove && reCiterArticle.getAffiliationScore() > 0) {
									if (targetAuthorFirstName.length() > 5 && firstName.length() > 5) {
										String targetAuthorFirstName5Chars = targetAuthorFirstName.substring(0, 5);
										String articleAuthorFirstName5Chars = firstName.substring(0, 5);
										if (StringUtils.equalsIgnoreCase(targetAuthorFirstName5Chars, articleAuthorFirstName5Chars)) {
											analysisObjectAuthor.setFirstThreeCharAndAffiliationScoreMatch(true);
											shouldRemove = false;
										}
									}
								}

								// Check if target author's first name + middle name = article's first name
								if (shouldRemove) {
									String targetAuthorFirstNameMiddleNameCombined = targetAuthorFirstName + targetAuthorMiddleName;
									if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameMiddleNameCombined, firstName)) {
										analysisObjectAuthor.setTargetAuthorFirstAndMiddleNameConcatenatedMatch(true);
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
											analysisObjectAuthor.setFirstPartOfNameMatch(true);
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
												analysisObjectAuthor.setCheckMiddleNameMatch(true);
												shouldRemove = false;
											}
										}
									}
								}

								// Check target author's list of name variants from the names fetched by email addresses.
//								if (shouldRemove) {
//									if (identity.getAuthorNamesFromEmailFetch() != null) {
//										for (AuthorName authorNameFromEmail : identity.getAuthorNamesFromEmailFetch()) {
//											// check whether last name and first initial matches.
//											if (StringUtils.equalsIgnoreCase(lastName, authorNameFromEmail.getLastName()) &&
//													StringUtils.equalsIgnoreCase(firstName.substring(0, 1), authorNameFromEmail.getFirstInitial())) {
//												shouldRemove = false;
//												break;
//											}
//										}
//									}
//								}

								if (shouldRemove) {
									// Case pmid = 10651632, first name in article is Clay (from Scopus), name in db is w. clay. cwid = wcb2001.
									// Match Clay to w. clay.
									String[] targetAuthorFirstNameParts = targetAuthorFirstName.split("\\s+");
									if (targetAuthorFirstNameParts.length > 1) {
										String firstPart = targetAuthorFirstNameParts[0];
										String secondPart = targetAuthorFirstNameParts[1];
										if (StringUtils.equalsIgnoreCase(firstName, firstPart) ||
											StringUtils.equalsIgnoreCase(firstName, secondPart)) {
											shouldRemove = false;
											
											// Case: 26336036, name in article is David K Warren, cwid = jdw2003, name
											// in db is "J David Warren", check middle initial.
											if (middleName.length() > 0) {
												if (!StringUtils.equalsIgnoreCase(middleNameInitial, targetAuthorMiddleNameInitial)) {
													analysisObjectAuthor.setScopusFirstNameMatch(true);
													shouldRemove = true;
												}
											}
										}
									}
								}
							} else {
								analysisObjectAuthor.setFirstNameMatch(true);
								
								// Handle the case where there are multiple authors with the same last name.
								foundAuthorWithSameFirstName = true;

								// case: pmid=23045697, cwid = mlg2007
								// First name, last name matches, but middle name and affiliation doesn't match. Mark for removal.
								if (middleName.length() > 0 && targetAuthorMiddleName.length() > 0) {
									String middleInitial = author.getAuthorName().getMiddleInitial();
									String targetAuthorMiddleInitial = identity.getAuthorName().getMiddleInitial();

									if (!StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) && reCiterArticle.getAffiliationScore() == 0) {
										shouldRemove = true;
										foundAuthorWithSameFirstName = false; // middle name differs.
										analysisObjectAuthor.setMultipleAuthorMatchButMiddleNameDiffer(true);
									}
								}
							}
						} else {
							
							// Case:  For jdw2003 - 2913152 (and other PMIDs)… If our target  person has a first name 
							// with an initial, it needs to be in the correct order. In other words, “DJ Warren” won’t cut it.
							if (!StringUtils.equalsIgnoreCase(firstNameInitial, targetAuthorFirstNameInitial)) {
								if (middleName.length() > 0 && targetAuthorMiddleName.length() > 0) {
									if (!StringUtils.equalsIgnoreCase(middleNameInitial, targetAuthorMiddleNameInitial)) {
										analysisObjectAuthor.setInitialInCorrectOrder(true);
										shouldRemove = true;
									}
								}
							}
							
							// check middle name.
							// Case: False Positive List: [2]: [12814220, 21740463] for Anna Bender.
							// Remove this article because middle name exist in article, but not in rc_identity.
							if (middleName.length() > 0 && targetAuthorMiddleName.length() == 0) {
								if (!StringUtils.equalsIgnoreCase(middleName, targetAuthorMiddleName) && reCiterArticle.getAffiliationScore() == 0) {
									analysisObjectAuthor.setMiddleNameExistInArticleButNotInDb(true);
									shouldRemove = true;
								}
							}

							// case: pmid=11467038, cwid = ajmarcus
							// Middle name doesn't match. Mark for removal.
							if (middleName.length() > 0 && targetAuthorMiddleName.length() > 0) {
								String middleInitial = author.getAuthorName().getMiddleInitial();
								String targetAuthorMiddleInitial = identity.getAuthorName().getMiddleInitial();

								if (!StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) && reCiterArticle.getAffiliationScore() == 0) {
									analysisObjectAuthor.setMiddleNameMatch(true);
									shouldRemove = true;
								}
							}

							// case: pmid=17943945, cwid = wcb2001
							// Name in article: W Clay Bracken, name in rc_identity = W. clay Bracken
							// Name in article becomes firstname = W, middle initial = Clay.
							if (shouldRemove) {
								String firstNameMiddleName = firstName + " " + middleName;
								String targetAuthorFirstNameRemovedPeriod = identity.getAuthorName().getFirstName().replace(".", "");
								if (StringUtils.equalsIgnoreCase(firstNameMiddleName, targetAuthorFirstNameRemovedPeriod)) {
									analysisObjectAuthor.setRemovePeriodMatch(true);
									shouldRemove = false;
								}
							}
						}
					}
				}
			}
		}

		reCiterArticle.setShouldRemoveValue(shouldRemove);
		reCiterArticle.setFoundAuthorWithSameFirstNameValue(foundAuthorWithSameFirstName);
		reCiterArticle.setFoundMatchingAuthorValue(foundMatchingAuthor);
		
		if ((shouldRemove && !foundAuthorWithSameFirstName) || !foundMatchingAuthor) {
			slf4jLogger.info("Removed article id=[" + reCiterArticle.getArticleId() + "] with cwid=[" + 
					identity.getCwid() + "] and name in article=[" + firstNameFieldVar + ", " + middleNameFieldVar + "]" +
					" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		return 0;
	}
}
