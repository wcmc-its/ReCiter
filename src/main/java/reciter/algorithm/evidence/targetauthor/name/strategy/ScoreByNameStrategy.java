package reciter.algorithm.evidence.targetauthor.name.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.algorithm.util.ReCiterStringUtil;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.AuthorNameEvidence;
import reciter.engine.erroranalysis.AnalysisObjectAuthor;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

/**
 * @author szd2013
 *
 */
public class ScoreByNameStrategy extends AbstractTargetAuthorStrategy {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScoreByNameStrategy.class);

	private String firstNameFieldVar;
	private String middleNameFieldVar;

	private boolean isMultipleAuthorsWithSameLastNameAsTargetAuthor(ReCiterArticle reCiterArticle, Identity identity) {
		int count = 0;
		String targetAuthorLastName = identity.getPrimaryName().getLastName();
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
		
		Set<AuthorName> sanitizedIdentityAuthor = new HashSet<AuthorName>();
		Set<AuthorName> sanitizedTargetAuthor = new HashSet<AuthorName>();
		AuthorNameEvidence authorNameEvidence;
		
		
		double score = 0;
		boolean shouldRemove = false;
		boolean foundAuthorWithSameFirstName = false;
		boolean foundMatchingAuthor = false; // found matching author with the same last name and first and middle initial as target author.

		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();

		String targetAuthorFirstName = identity.getPrimaryName().getFirstName();
		String targetAuthorFirstNameInitial = identity.getPrimaryName().getFirstInitial();
		String targetAuthorLastName = identity.getPrimaryName().getLastName();
		String targetAuthorMiddleName = identity.getPrimaryName().getMiddleName();
		String targetAuthorMiddleNameInitial = identity.getPrimaryName().getMiddleInitial();
		
		int targetAuthorCount = getTargetAuthorCount(reCiterArticle);
		if(identity != null) { 
			sanitizeIdentityAuthorNames(identity, sanitizedIdentityAuthor);
		}
		List<AuthorNameEvidence> authorNameEvidences = new ArrayList<AuthorNameEvidence>(sanitizedIdentityAuthor.size());
		if(targetAuthorCount >=1) {			
			sanitizeTargetAuthorNames(reCiterArticle, sanitizedTargetAuthor);
			for(AuthorName identityAuthorName: sanitizedIdentityAuthor) {
				authorNameEvidence = new AuthorNameEvidence();
				scoreLastName(identityAuthorName, sanitizedTargetAuthor, authorNameEvidence);
				if(!isNotNullIdentityMiddleName(sanitizedIdentityAuthor)) {
					scoreFirstNameMiddleNameNull(identityAuthorName, sanitizedTargetAuthor, authorNameEvidence);
				}
				else {
					scoreFirstNameMiddleName(identityAuthorName, sanitizedTargetAuthor, authorNameEvidence);
				}
				authorNameEvidences.add(authorNameEvidence);
			}
			authorNameEvidence = calculateHighestScore(authorNameEvidences);
		}
		else {
			authorNameEvidence = new AuthorNameEvidence();
			authorNameEvidence.setInstitutionalAuthorName(identity.getPrimaryName());
			authorNameEvidence.setNameMatchFirstType("nullTargetAuthor-MatchNotAttempted");
			authorNameEvidence.setNameMatchLastType("nullTargetAuthor-MatchNotAttempted");
			authorNameEvidence.setNameMatchMiddleType("nullTargetAuthor-MatchNotAttempted");
		}
		
		reCiterArticle.setAuthorNameEvidence(authorNameEvidence);
		
		slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + authorNameEvidence.toString());
		
/*		slf4jLogger.info("SanitizedIdentityNames");
		if(sanitizedIdentityAuthor.size() ==0 && sanitizedTargetAuthor.size() ==0) {
			slf4jLogger.info(reCiterArticle.getArticleId() + "No target author");
		}
		for(AuthorName authName: sanitizedIdentityAuthor) {
			slf4jLogger.info(authName.toString());
		}
		
		slf4jLogger.info("SanitizedTargetAuthorNames");
		for(AuthorName authName: sanitizedTargetAuthor) {
			slf4jLogger.info(authName.toString());
		}*/
		

		//boolean isMultipleAuthorsWithSameLastNameAsTargetAuthor = isMultipleAuthorsWithSameLastNameAsTargetAuthor(
		//		reCiterArticle, identity);

		/*if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {

				AnalysisObjectAuthor analysisObjectAuthor = new AnalysisObjectAuthor();
				analysisObjectAuthor.setAuthor(author);
				
				// Update author with the correct author.
				if (isMultipleAuthorsWithSameLastNameAsTargetAuthor) {
					author = getCorrectAuthor(reCiterArticle, identity);
				}

				if (author != null && author.isTargetAuthor()) {
					foundMatchingAuthor = true;
					String firstName = author.getAuthorName().getFirstName();
					String firstNameInitial = author.getAuthorName().getFirstInitial();
					String lastName = author.getAuthorName().getLastName();
					String middleName = author.getAuthorName().getMiddleName();
					String middleNameInitial = author.getAuthorName().getMiddleInitial();
					
					AuthorNameEvidence authorNameEvidence = new AuthorNameEvidence();
					
					//nameMatchFrist verbose e.g. Curtis vs Curtis Points 2
					if (StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(lastName),  ReCiterStringUtil.deAccent(targetAuthorLastName))) {
						analysisObjectAuthor.setLastNameMatchTargetAuthor(true);
						score = score + 2;
					}

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

								// Check db identity_directory. If any of the alias' first name matches the
								// article author's first name, un-do the operation (removal from the selected cluster).
								// So this article should still be in the selected cluster.

								if (shouldRemove) {
									List<AuthorName> aliasList = identity.getAlternateNames();
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
								// in db identity).
								if (shouldRemove) {
									String articleAuthorFirstNameDashRemoved = firstName.replace("-", " ");
									String targetAuthorFirstNameDashRemoved = targetAuthorFirstName.replace("-", " ");
									if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameDashRemoved, articleAuthorFirstNameDashRemoved)) {
										analysisObjectAuthor.setFirstNameDashRemovedMatch(true);
										shouldRemove = false;
										slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because dash added full names match");
									}
								}

								// Case: "J-m" in article, and "Juan Miguel" in db identity.
								if (shouldRemove) {
									String[] targetAuthorFirstNameArr = targetAuthorFirstName.split("\\s+");
									if (targetAuthorFirstNameArr.length == 2) {
										String firstPartInitial = targetAuthorFirstNameArr[0].substring(0, 1);
										String secondPartInitial = targetAuthorFirstNameArr[1].substring(0, 1);
										String combineWithDash = firstPartInitial + "-" + secondPartInitial;

										if (StringUtils.equalsIgnoreCase(firstName, combineWithDash)) {
											analysisObjectAuthor.setFirstInitialDashRemovedMatch(true);
											shouldRemove = false;
											slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because dash added names match");
										}
									}
								}

								// Case: "Bisen" in article and "Bi-Sen" in db identity. Remove dash from "Bi-Sen".
								if (shouldRemove) {
									if (targetAuthorFirstName.contains("-")) {
										String targetAuthorFirstNameDashRemoved = targetAuthorFirstName.replace("-", "");
										if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameDashRemoved, firstName)) {
											analysisObjectAuthor.setFirstNameDashAddedMatch(true);
											shouldRemove = false;
											slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because dashed removed names match");
										}
									}
								}

								// Case: "B-s" in article and "Bi-Sen" in db identity.
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
												slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because dashed initial match");
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
											slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because first initial and middle "
													+ "initial are concatenated.");
										}
									}
								}

								// Check the Levenshtein distance between the target author's first name and the article
								// author's first name, if the distance <= 1 and affiliation score is greater than 0, 
								// un-do the removal operation.
								// Cases:
								// 1. Removed article id=[19061285] with uid=[nkaltork] and name in article=[Nassar] In gold standard=[1]
								// 2. Removed article id=[18343782] with uid=[nkaltork] and name in article=[Nassar] In gold standard=[1]
								if (shouldRemove && reCiterArticle.getAffiliationScore() > 0) {
									int levenshteinDistance = ReCiterStringUtil.levenshteinDistance(firstName, targetAuthorFirstName);
									if (levenshteinDistance <= 1) {
										analysisObjectAuthor.setLevenshteinDistanceMatch(true);
										shouldRemove = false;
										slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because levenshtein dist match");
									}
								}

								// Check the first three characters of each of the first names and compare affiliation score.
								// Case:
								// 1. Removed article id=[19101229] with uid=[mszulc] and name in article=[Massimo] In gold standard=[1]
								if (shouldRemove && reCiterArticle.getAffiliationScore() > 0) {
									if (targetAuthorFirstName.length() > 5 && firstName.length() > 5) {
										String targetAuthorFirstName5Chars = targetAuthorFirstName.substring(0, 5);
										String articleAuthorFirstName5Chars = firstName.substring(0, 5);
										if (StringUtils.equalsIgnoreCase(targetAuthorFirstName5Chars, articleAuthorFirstName5Chars)) {
											analysisObjectAuthor.setFirstThreeCharAndAffiliationScoreMatch(true);
											shouldRemove = false;
											slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because first three characters "
													+ "of each of the first names and affilition score > 0.");
										}
									}
								}

								// Check if target author's first name + middle name = article's first name
								if (shouldRemove) {
									String targetAuthorFirstNameMiddleNameCombined = targetAuthorFirstName + targetAuthorMiddleName;
									if (StringUtils.equalsIgnoreCase(targetAuthorFirstNameMiddleNameCombined, firstName)) {
										analysisObjectAuthor.setTargetAuthorFirstAndMiddleNameConcatenatedMatch(true);
										shouldRemove = false;
										slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because target author's first name "
												+ "+ middle name = article's author's first name.");
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
											slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because split by first name match.");
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
												slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because middle names match");
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
									// Case pmid = 10651632, first name in article is Clay (from Scopus), name in db is w. clay. uid = wcb2001.
									// Match Clay to w. clay.
									String[] targetAuthorFirstNameParts = targetAuthorFirstName.split("\\s+");
									if (targetAuthorFirstNameParts.length > 1) {
										String firstPart = targetAuthorFirstNameParts[0];
										String secondPart = targetAuthorFirstNameParts[1];
										if (StringUtils.equalsIgnoreCase(firstName, firstPart) ||
											StringUtils.equalsIgnoreCase(firstName, secondPart)) {
											shouldRemove = false;
											
											// Case: 26336036, name in article is David K Warren, uid = jdw2003, name
											// in db is "J David Warren", check middle initial.
											if (middleName.length() > 0) {
												if (!StringUtils.equalsIgnoreCase(middleNameInitial, targetAuthorMiddleNameInitial)) {
													analysisObjectAuthor.setScopusFirstNameMatch(true);
													shouldRemove = true;
													slf4jLogger.info("Marked article id=[" + reCiterArticle.getArticleId() + "] for removal because " +
															"middle initials do not match.");
												}
											}
										}
									}
								}
							} else {
								analysisObjectAuthor.setFirstNameMatch(true);
								
								// Handle the case where there are multiple authors with the same last name.
								foundAuthorWithSameFirstName = true;

								// case: pmid=23045697, uid = mlg2007
								// First name, last name matches, but middle name and affiliation doesn't match. Mark for removal.
								if (middleName != null && targetAuthorMiddleName != null && middleName.length() > 0 && targetAuthorMiddleName.length() > 0) {
									String middleInitial = author.getAuthorName().getMiddleInitial();
									String targetAuthorMiddleInitial = identity.getPrimaryName().getMiddleInitial();

									if (!StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) && reCiterArticle.getAffiliationScore() == 0) {
										shouldRemove = true;
										slf4jLogger.info("Marked article id=[" + reCiterArticle.getArticleId() + "] for removal because middle name and " +
												"affiliation do not match.");
										foundAuthorWithSameFirstName = false; // middle name differs.
										analysisObjectAuthor.setMultipleAuthorMatchButMiddleNameDiffer(true);
									}
								}
							}
						} else {
							
							// Case:  For jdw2003 - 2913152 (and other PMIDs)� If our target  person has a first name 
							// with an initial, it needs to be in the correct order. In other words, �DJ Warren� won�t cut it.
							if (!StringUtils.equalsIgnoreCase(firstNameInitial, targetAuthorFirstNameInitial)) {
								if (middleName.length() > 0 && targetAuthorMiddleName.length() > 0) {
									if (!StringUtils.equalsIgnoreCase(middleNameInitial, targetAuthorMiddleNameInitial)) {
										analysisObjectAuthor.setInitialInCorrectOrder(true);
										slf4jLogger.info("Marked article id=[" + reCiterArticle.getArticleId() + "] for removal because first name and " +
												"middle name are not in the correct order");
										shouldRemove = true;
									}
								}
							}
							
							// check middle name.
							// Case: False Positive List: [2]: [12814220, 21740463] for Anna Bender.
							// Remove this article because middle name exist in article, but not in db identity.
							if (middleName != null && middleName.length() > 0 && targetAuthorMiddleName != null && targetAuthorMiddleName.length() == 0) {
								boolean foundEqualMiddleInitialInAlternateNames = false;
								for (AuthorName alternateNames : identity.getAlternateNames()) {
									// need to check for alternate names as well.
									// case: 9447707 ljgudas - middle initial appears in article, but not in the primary name.
									if (alternateNames.getMiddleName().length() != 0 && StringUtils.equalsIgnoreCase(middleNameInitial, alternateNames.getMiddleInitial())) {
										foundEqualMiddleInitialInAlternateNames = true;
										break;
									}
								}
								if (!foundEqualMiddleInitialInAlternateNames && reCiterArticle.getAffiliationScore() == 0) {
									analysisObjectAuthor.setMiddleNameExistInArticleButNotInDb(true);
									slf4jLogger.info("Marked article id=[" + reCiterArticle.getArticleId() + "] "
											+ "for removal because middle name=[" + middleName + "] exist " +
											"in article but not in db.");
									shouldRemove = true;
								}
							}

							// case: pmid=11467038, uid = ajmarcus
							// Middle name doesn't match. Mark for removal.
							if (middleName != null && middleName.length() > 0 && targetAuthorMiddleName != null && targetAuthorMiddleName.length() > 0) {
								String middleInitial = author.getAuthorName().getMiddleInitial();
								String targetAuthorMiddleInitial = identity.getPrimaryName().getMiddleInitial();

								if (!StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) && reCiterArticle.getAffiliationScore() == 0) {
									analysisObjectAuthor.setMiddleNameMatch(true);
									shouldRemove = true;
									slf4jLogger.info("Marked article id=[" + reCiterArticle.getArticleId() + "] for removal because middle initial of " +
											"author in article is=[" + middleInitial + "] and middle initial in db is=[" + targetAuthorMiddleInitial + "] are different");
								}
							}

							// case: pmid=17943945, uid = wcb2001
							// Name in article: W Clay Bracken, name in db identity = W. clay Bracken
							// Name in article becomes firstname = W, middle initial = Clay.
							if (shouldRemove) {
								String firstNameMiddleName = firstName + " " + middleName;
								String targetAuthorFirstNameRemovedPeriod = identity.getPrimaryName().getFirstName().replace(".", "");
								if (StringUtils.equalsIgnoreCase(firstNameMiddleName, targetAuthorFirstNameRemovedPeriod)) {
									analysisObjectAuthor.setRemovePeriodMatch(true);
									slf4jLogger.info("Unmarked article id=[" + reCiterArticle.getArticleId() + "] because first name + middle name " +
											"in article=[" + firstNameMiddleName + "] and author name in db with '.' removed in first name=[" + 
											targetAuthorFirstNameRemovedPeriod + "] are equal.");
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
			slf4jLogger.info("Removed article id=[" + reCiterArticle.getArticleId() + "] with uid=[" + 
					identity.getUid() + "] and name in article=[" + firstNameFieldVar + ", " + middleNameFieldVar + "]" +
					" In gold standard=[" + reCiterArticle.getGoldStandard() + "]");
			return 1;
		} else {
			return 0;
		}*/
		return 0;
	}
	
	private void scoreLastName(AuthorName identityAuthor, Set<AuthorName> articleAuthorNames, AuthorNameEvidence authorNameEvidence) {
		if(articleAuthorNames.size() > 0) {
			AuthorName articleAuthorName = articleAuthorNames.iterator().next();
			if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getLastName()), ReCiterStringUtil.deAccent(articleAuthorName.getLastName()))) {
				authorNameEvidence.setNameMatchLastType("full-exact");
				authorNameEvidence.setNameMatchLastScore(2);
			}
			else if(identityAuthor.getMiddleName() != null && StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getLastName() + identityAuthor.getMiddleName()), ReCiterStringUtil.deAccent(articleAuthorName.getLastName()))) {
				authorNameEvidence.setNameMatchLastType("full-exact");
				authorNameEvidence.setNameMatchLastScore(2);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
				authorNameEvidence.setNameMatchModifier("combinedMiddleNameLastName");
				authorNameEvidence.setNameMatchModifierScore(1);
			}
			else if(identityAuthor.getMiddleName() != null && ReCiterStringUtil.deAccent(identityAuthor.getLastName()).contains(ReCiterStringUtil.deAccent(articleAuthorName.getLastName()))) {
				authorNameEvidence.setNameMatchLastType("full-exact");
				authorNameEvidence.setNameMatchLastScore(2);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-lastName");
				authorNameEvidence.setNameMatchModifierScore(-2);
			}
			else if(identityAuthor.getLastName().length() >= 4 && ReCiterStringUtil.levenshteinDistance(ReCiterStringUtil.deAccent(identityAuthor.getLastName()), ReCiterStringUtil.deAccent(articleAuthorName.getLastName())) <= 1) {
				authorNameEvidence.setNameMatchLastType("full-fuzzy");
				authorNameEvidence.setNameMatchLastScore(1);
			}
			else {
				authorNameEvidence.setNameMatchLastType("full-conflictingEntirely");
				authorNameEvidence.setNameMatchLastScore(-3);
			}
			authorNameEvidence.setInstitutionalAuthorName(identityAuthor);
			authorNameEvidence.setArticleAuthorName(articleAuthorName);
			authorNameEvidence.setTotalScore(authorNameEvidence.getNameMatchFirstScore() + authorNameEvidence.getNameMatchMiddleScore() + authorNameEvidence.getNameMatchLastScore() + authorNameEvidence.getNameMatchModifierScore());
		}
	}
	
	private void scoreFirstNameMiddleNameNull(AuthorName identityAuthor, Set<AuthorName> articleAuthorNames, AuthorNameEvidence authorNameEvidence) {
		if(articleAuthorNames.size() > 0) {
			AuthorName articleAuthorName = articleAuthorNames.iterator().next();
			if(identityAuthor.getFirstName() != null && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("identityNull-MatchNotAttempted");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			else if(identityAuthor.getFirstName() != null && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).startsWith(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()))) { //Example: Paul (identity.firstName) = PaulJames (article.firstName)
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("identityNull-MatchNotAttempted");
				authorNameEvidence.setNameMatchMiddleScore(0);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstName");
				authorNameEvidence.setNameMatchModifierScore(-1);
			}
			else if(identityAuthor.getFirstName() != null && 
					ReCiterStringUtil.deAccent(identityAuthor.getFirstName()).startsWith(ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) { //Example: Paul (identity.firstName) = P (article.firstName)
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("identityNull-MatchNotAttempted");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			else if(identityAuthor.getFirstName() != null && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName().substring(0, 2)),ReCiterStringUtil.deAccent(articleAuthorName.getFirstName().substring(0, 2)))) { //Example: Paul (identity.firstName) = P (article.firstName)
				authorNameEvidence.setNameMatchFirstType("full-fuzzy");
				authorNameEvidence.setNameMatchFirstScore(0);
				authorNameEvidence.setNameMatchMiddleType("identityNull-MatchNotAttempted");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			else if(identityAuthor.getFirstName() != null &&
					identityAuthor.getFirstName().length() >= 4 && ReCiterStringUtil.levenshteinDistance(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName())) == 1) {
				authorNameEvidence.setNameMatchFirstType("full-fuzzy");
				authorNameEvidence.setNameMatchFirstScore(0);
				authorNameEvidence.setNameMatchMiddleType("identityNull-MatchNotAttempted");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			else if(identityAuthor.getFirstName() != null &&
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstInitial()),ReCiterStringUtil.deAccent(articleAuthorName.getFirstInitial()))) {
				authorNameEvidence.setNameMatchFirstType("full-conflictingAllButInitials");
				authorNameEvidence.setNameMatchFirstScore(-2);
				authorNameEvidence.setNameMatchMiddleType("identityNull-MatchNotAttempted");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			else {
				authorNameEvidence.setNameMatchLastType("full-conflictingEntirely");
				authorNameEvidence.setNameMatchLastScore(-3);
				authorNameEvidence.setNameMatchMiddleType("identityNull-MatchNotAttempted");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			authorNameEvidence.setInstitutionalAuthorName(identityAuthor);
			authorNameEvidence.setArticleAuthorName(articleAuthorName);
			authorNameEvidence.setTotalScore(authorNameEvidence.getNameMatchFirstScore() + authorNameEvidence.getNameMatchMiddleScore() + authorNameEvidence.getNameMatchLastScore() + authorNameEvidence.getNameMatchModifierScore());
		}
	}
	
	private void scoreFirstNameMiddleName(AuthorName identityAuthor, Set<AuthorName> articleAuthorNames, AuthorNameEvidence authorNameEvidence) {
		if(articleAuthorNames.size() > 0) {
			AuthorName articleAuthorName = articleAuthorNames.iterator().next();
			if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName() + identityAuthor.getMiddleName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
			}
			else if(identityAuthor.getFirstName() != null  && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()) + "(.*)" + ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()))) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstMiddleName");
				authorNameEvidence.setNameMatchModifierScore(1);
			}
			else if(identityAuthor.getFirstName() != null  && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName() + identityAuthor.getMiddleInitial()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("inferredInitials-exact");
				authorNameEvidence.setNameMatchMiddleScore(1);
			}
			else if(identityAuthor.getFirstName() != null  && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()) + "(.*)" + ReCiterStringUtil.deAccent(identityAuthor.getMiddleInitial()))) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("inferredInitials-exact");
				authorNameEvidence.setNameMatchMiddleScore(1);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstMiddleName");
				authorNameEvidence.setNameMatchModifierScore(1);
			}
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstInitial() + identityAuthor.getMiddleInitial()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("inferredInitials-exact");
				authorNameEvidence.setNameMatchMiddleScore(1);
			}
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstInitial() + identityAuthor.getMiddleName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
			}
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()) + ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()) + "(.*)")) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstMiddleName");
				authorNameEvidence.setNameMatchModifierScore(1);
			}
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()) + ReCiterStringUtil.deAccent(identityAuthor.getMiddleInitial()) + "(.*)")) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("inferredInitials-exact");
				authorNameEvidence.setNameMatchMiddleScore(1);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstMiddleName");
				authorNameEvidence.setNameMatchModifierScore(1);
			}
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getMiddleInitial() + identityAuthor.getFirstInitial()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("inferredInitials-exact");
				authorNameEvidence.setNameMatchMiddleScore(1);
				authorNameEvidence.setNameMatchModifier("incorrectOrder");
				authorNameEvidence.setNameMatchModifierScore(-1);
			}
			//If there's more than one capital letter in identity.firstName or identity.middleName, attempt match where any capitals in identity.firstName + any capital letters in identity.middleName = article.firstName
			//Example: KS (identity.initialsInFirstName) + C (identity.initialsInMiddleName) = KSC (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					(identityAuthor.getFirstName().codePoints().filter(c-> c>='A' && c<='Z').count() > 1 || identityAuthor.getMiddleName().codePoints().filter(c-> c>='A' && c<='Z').count() > 1) && //check if there is more than 1 capital letters
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()).chars().filter(Character::isUpperCase)
	                           .mapToObj(c -> Character.toString((char)c))
	                           .collect(Collectors.joining()) + 
	                           ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()).chars().filter(Character::isUpperCase)
	                           .mapToObj(c -> Character.toString((char)c))
	                           .collect(Collectors.joining()), 
							ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("inferredInitials-exact");
				authorNameEvidence.setNameMatchMiddleScore(1);
			}
			//If there's more than one capital letter in identity.firstName, attempt match where any capitals in identity.firstName = article.firstName
			//Example: KS (identity.initialsInFirstName) = KS (article.firstName)
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  && 
					identityAuthor.getFirstName().codePoints().filter(c-> c>='A' && c<='Z').count() > 1 && //check if there is more than 1 capital letters
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()).chars().filter(Character::isUpperCase)
	                           .mapToObj(c -> Character.toString((char)c))
	                           .collect(Collectors.joining()) , 
							ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			//If there's more than one capital letter in identity.firstName, attempt match where any capitals in identity.firstName + identity.middleName = article.firstName
			//Example: KS (identity.initialsInFirstName) + Clifford (identity.middleName) = KSClifford (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					identityAuthor.getFirstName().codePoints().filter(c-> c>='A' && c<='Z').count() > 1 && //check if there is more than 1 capital letters
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()).chars().filter(Character::isUpperCase)
	                           .mapToObj(c -> Character.toString((char)c))
	                           .collect(Collectors.joining()) + identityAuthor.getMiddleName() , 
							ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
			}
			//Attempt match where identity.firstName + "%" = article.firstName
			//Example: Robert (identity.firstName) = RobertR (article.firstName)
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()) + "(.*)")) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstName");
				authorNameEvidence.setNameMatchModifierScore(-1);
			}
			//Attempt match where "%" + identity.firstName = article.firstName
			//Example: Cary (identity.firstName) = MCary (article.firstName)
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches("(.*)" + ReCiterStringUtil.deAccent(identityAuthor.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("full-exact");
				authorNameEvidence.setNameMatchFirstScore(2);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstName");
				authorNameEvidence.setNameMatchModifierScore(-1);
			}
			//Attempt match where identity.middleName = article.firstName
			//Example: Clifford (identity.middleName) = Clifford (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("noMatch");
				authorNameEvidence.setNameMatchFirstScore(-1);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
			}
			//Attempt match where identity.middleName + "%" = article.firstName
			//Example: Clifford (identity.middleName) = CliffordKS (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches(ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()) + "(.*)")) {
				authorNameEvidence.setNameMatchFirstType("noMatch");
				authorNameEvidence.setNameMatchFirstScore(-1);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-middleName");
				authorNameEvidence.setNameMatchModifierScore(-1);
			}
			//Attempt match where "%" + identity.middleName = article.firstName
			//Example: Clifford (identity.middleName) = KunSungClifford (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches("(.*)" + ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()))) {
				authorNameEvidence.setNameMatchFirstType("noMatch");
				authorNameEvidence.setNameMatchFirstScore(-1);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-middleName");
				authorNameEvidence.setNameMatchModifierScore(-1);
			}
			//Attempt match where levenshteinDistance between identity.firstName + identity.middleName and article.firstName is <=2.
			//Example: Manney (identity.firstName) + Carrington (identity.middleName) = MannyCarrington (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.levenshteinDistance(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()) + ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName())) <= 2) {
				authorNameEvidence.setNameMatchFirstType("full-fuzzy");
				authorNameEvidence.setNameMatchFirstScore(0);
				authorNameEvidence.setNameMatchMiddleType("full-fuzzy");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			//Attempt match where identity.firstName >= 4 characters and levenshteinDistance between identity.firstName and article.firstName is <=1.
			//Example: Nassar (identity.firstName) = Nasser (article.firstName)
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  &&
					identityAuthor.getFirstName().length() >= 4 && 
					ReCiterStringUtil.levenshteinDistance(ReCiterStringUtil.deAccent(identityAuthor.getFirstName()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName())) <= 1) {
				authorNameEvidence.setNameMatchFirstType("full-fuzzy");
				authorNameEvidence.setNameMatchFirstScore(0);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			//Attempt match where first three characters of identity.firstName = first three characters of identity.firstName.
			//Example: Massimiliano (identity.firstName) = Massimo (article.firstName)
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  &&
					identityAuthor.getFirstName().length() >=3 && articleAuthorName.getFirstName().length() >=3 &&
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName().substring(0, 3)),ReCiterStringUtil.deAccent(articleAuthorName.getFirstName().substring(0, 3)))) {
				authorNameEvidence.setNameMatchFirstType("full-fuzzy");
				authorNameEvidence.setNameMatchFirstScore(0);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			//Attempt match where identity.firstInitial + "%" + identity.middleName = article.firstName
			//Example: M (identity.firstInitial) + Carrington (identity.middleName) = MannyCarrington (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()).matches(ReCiterStringUtil.deAccent(identityAuthor.getFirstInitial()) + "(.*)" + ReCiterStringUtil.deAccent(identityAuthor.getMiddleName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
				authorNameEvidence.setNameMatchModifier("identitySubstringOfArticle-firstMiddleName");
				authorNameEvidence.setNameMatchModifierScore(1);
			}
			//Attempt match where identity.middleName + identity.firstInitial = article.firstName
			//Example: Carrington (identity.middleName) + M (identity.firstInitial) = CarringtonM (article.firstName)
			else if(identityAuthor.getFirstName() != null && identityAuthor.getMiddleName() != null && articleAuthorName.getFirstName() != null  && 
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getMiddleName() + identityAuthor.getFirstInitial()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("inferredInitials-exact");
				authorNameEvidence.setNameMatchFirstScore(1);
				authorNameEvidence.setNameMatchMiddleType("full-exact");
				authorNameEvidence.setNameMatchMiddleScore(2);
				authorNameEvidence.setNameMatchModifier("incorrectOrder");
				authorNameEvidence.setNameMatchModifierScore(-1);
			}
			//Attempt match where article.firstName is only one character and identity.firstName = first character of article.firstName.
			//Example: Jessica (identity.firstName) = J (article.firstName)
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  &&
					articleAuthorName.getFirstName().length() == 1 &&
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstInitial()), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName()))) {
				authorNameEvidence.setNameMatchFirstType("full-conflictingAllButInitials");
				authorNameEvidence.setNameMatchFirstScore(-2);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			//Attempt match where first character of identity.firstName = first character of identity.firstName.
			//Example: Jessica (identity.firstName) = Jochen (article.firstName)
			else if(identityAuthor.getFirstName() != null && articleAuthorName.getFirstName() != null  &&
					StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(identityAuthor.getFirstName().substring(0, 1)), ReCiterStringUtil.deAccent(articleAuthorName.getFirstName().substring(0, 1)))) {
				authorNameEvidence.setNameMatchFirstType("full-conflictingAllButInitials");
				authorNameEvidence.setNameMatchFirstScore(-2);
				authorNameEvidence.setNameMatchMiddleType("noMatch");
				authorNameEvidence.setNameMatchMiddleScore(0);
			}
			//Else, we have no match of any kind.
			//Example: Pascale vs. Curtis
			else {
				authorNameEvidence.setNameMatchFirstType("full-conflictingEntirely");
				authorNameEvidence.setNameMatchFirstScore(-3);
				authorNameEvidence.setNameMatchMiddleType("full-conflictingEntirely");
				authorNameEvidence.setNameMatchMiddleScore(-2);
			}
			authorNameEvidence.setInstitutionalAuthorName(identityAuthor);
			authorNameEvidence.setArticleAuthorName(articleAuthorName);
			authorNameEvidence.setTotalScore(authorNameEvidence.getNameMatchFirstScore() + authorNameEvidence.getNameMatchMiddleScore() + authorNameEvidence.getNameMatchLastScore() + authorNameEvidence.getNameMatchModifierScore());
		}
	}
	
	private int getTargetAuthorCount(ReCiterArticle reCiterArticle) {
		int targetAuthorCount = 0;
		
		for(ReCiterAuthor targetAuthorName: reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if(targetAuthorName.isTargetAuthor()) {
				targetAuthorCount++;
			}
		}
		return targetAuthorCount;
	}
	
	/**
	 * @author szd2013
	 * 	Preprocess identity.firstName, identity.middleName, and article.firstName
		If any names are in quotes or parentheses in identity.firstName or identity.middleName, pull them out so they can be matched against.
		Wing Tak "Jack" --> add "Jack" to identity.firstName, add "Wing Tak" to identity.firstName
		Qihui (Jim) --> add Jim to identity.firstName, add Qihui to identity.firstName
		Remove any periods, spaces, or dashes from both identity.firstName, identity.middleName, and article.firstName. For example:
		"Chi-chao" --> "Chichao"
		"Minh-Nhut Yvonne" --> "MinhNhutYvonne"
		"Eliot A." --> "EliotA"
		Disregard any cases where one variant of identity.firstName is a substring of another case of identity.firstName. Disregard any cases where one variant of identity.middleName is a substring of another case of identity.middleName. For example:
		"Cary" (keep) vs "C" (disregard)
		Null for middle name should only be included as possible value if none of the names or aliases contain a middle name
		A given target author might have N different first names and M different middle names.
		Go to D
		Retrieve article.lastName where targetAuthor=TRUE and all distinct cases of identity.lastName for our target author from identity. Preprocess identity.lastName and article.lastName.
		
		Remove any periods from both identity.lastName and article.lastName
		Remove any of the following endings from identity.lastName:
		", Jr"
		", MD PhD"
		", MD-PhD"
		", PhD"
		", MD"
		", III"
		", II"
		", Sr"
		" Jr"
		" MD PhD"
		" MD-PhD"
		" PhD"
		" MD"
		" III"
		" II"
		" Sr"
		Remove any periods, spaces, dashes, or quotes.
		For example:
		"Del Cole" --> "Delcole"
		"Garcia-Marquez" --> ""GarciaMarquez"
		"Capetillo Gonzalez de Zarate" --> "CapetilloGonzalezdeZarate"
	 *
	 */
	private void sanitizeIdentityAuthorNames(Identity identity, Set<AuthorName> sanitizedIdentityAuthorName) {
		AuthorName identityPrimaryName = new AuthorName();
		AuthorName additionalName = new AuthorName();
		String firstName = null;
		String lastName = null;
		String middleName = null;
		//Sanitize Identity Names
		if(identity.getPrimaryName() != null) {
			
			if(identity.getPrimaryName().getFirstName() != null) {
				if(identity.getPrimaryName().getFirstName().contains("\"") || (identity.getPrimaryName().getFirstName().contains("(") && identity.getPrimaryName().getFirstName().contains(")"))) {
					firstName = identity.getPrimaryName().getFirstName().replaceAll("[-.,()\\s]", "").replaceAll("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g", "");
					if(firstName !=null) {
						additionalName.setFirstName(firstName);
					}
					Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g");
					Matcher matcher = pattern.matcher(identity.getPrimaryName().getFirstName());
					while(matcher.find()) {
						identityPrimaryName.setFirstName(matcher.group().replaceAll("\"", ""));
					}
				}
				else {
					identityPrimaryName.setFirstName(identity.getPrimaryName().getFirstName().replaceAll("[-.,()\\s]", ""));
				}
			}
			if(identity.getPrimaryName().getMiddleName() != null) {
				if(identity.getPrimaryName().getMiddleName().contains("\"") || (identity.getPrimaryName().getMiddleName().contains("(") && identity.getPrimaryName().getMiddleName().contains(")"))) {
					middleName = identity.getPrimaryName().getMiddleName().replaceAll("[-.,()\\s]", "").replaceAll("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g", "");
					if(middleName !=null) {
						additionalName.setMiddleName(middleName);
					}
					Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\"([^\"]*)\")|(([a-z]*))/i/g");
					Matcher matcher = pattern.matcher(identity.getPrimaryName().getMiddleName());
					while(matcher.find()) {
						identityPrimaryName.setMiddleName(matcher.group().replaceAll("\"", ""));
					}
				}
				else {
					identityPrimaryName.setMiddleName(identity.getPrimaryName().getMiddleName().replaceAll("[-.,()\\s]", ""));
				}
			}
			if(identity.getPrimaryName().getLastName() != null) {
				lastName = identity.getPrimaryName().getLastName().replaceAll("[-.,,()\\s]|(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", "");
				identityPrimaryName.setLastName(lastName);
				if(additionalName.getFirstName() != null) {
					additionalName.setLastName(lastName);
				}
			}
			if(identityPrimaryName.getLastName() != null) {
				sanitizedIdentityAuthorName.add(identityPrimaryName);
			}
		}
		
		if(identity.getAlternateNames() != null && identity.getAlternateNames().size() > 0) {
			for(AuthorName aliasAuthorName: identity.getAlternateNames()) {
				AuthorName identityAliasAuthorName = new AuthorName();
				if(aliasAuthorName.getFirstName() != null) {
					identityAliasAuthorName.setFirstName(aliasAuthorName.getFirstName().replaceAll("[-.\",()\\s]", ""));
				}
				if(aliasAuthorName.getMiddleName() != null) {
					identityAliasAuthorName.setMiddleName(aliasAuthorName.getMiddleName().replaceAll("[-.\",()\\s]", ""));
				}
				if(aliasAuthorName.getLastName() != null) {
					identityAliasAuthorName.setLastName(aliasAuthorName.getLastName().replaceAll("[-.\",()\\s]|(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", ""));
				}
				
				if(identityAliasAuthorName.getLastName() != null) {
					sanitizedIdentityAuthorName.add(identityAliasAuthorName);
				}
			}
		}
		if(additionalName != null && additionalName.getLastName() != null) {
			sanitizedIdentityAuthorName.add(additionalName);
		}
		
		
		
		
	}
	
	private void sanitizeTargetAuthorNames(ReCiterArticle reCiterArticle, Set<AuthorName> sanitizedAuthorName) {
		
		//Sanitize targetAuthorName
				for(ReCiterAuthor targetAuthorName: reCiterArticle.getArticleCoAuthors().getAuthors()) {
					if(targetAuthorName.isTargetAuthor()) {
						AuthorName targetAuthor = new AuthorName();
						if(targetAuthorName.getAuthorName().getFirstName() != null) {
							targetAuthor.setFirstName(targetAuthorName.getAuthorName().getFirstName().replaceAll("[-.\"() ]", ""));
						}
						if(targetAuthorName.getAuthorName().getLastName() != null) {
							targetAuthor.setLastName(targetAuthorName.getAuthorName().getLastName().replaceAll("[-.\",()\\s]|(,Jr|, Jr|, MD PhD|,MD PhD|, MD-PhD|,MD-PhD|, PhD|,PhD|, MD|,MD|, III|,III|, II|,II|, Sr|,Sr|Jr|MD PhD|MD-PhD|PhD|MD|III|II|Sr)$", ""));
						}
						sanitizedAuthorName.add(targetAuthor);
					}
				}
	}
	
	/**
	 * This function check if middle name is null in all variants of identity name. If there is null return false
	 */
	private boolean isNotNullIdentityMiddleName(Set<AuthorName> idenityAuthorName) {
		boolean middleNameNull = false;
		for(AuthorName authorName: idenityAuthorName) {
			if(authorName.getMiddleName() != null) {
				middleNameNull = true;
				return middleNameNull;
			}
		}
		return middleNameNull;
		
	}
	
	private AuthorNameEvidence calculateHighestScore(List<AuthorNameEvidence> authorNameEvidences) {
		return authorNameEvidences.stream().max(Comparator.comparing(AuthorNameEvidence::getTotalScore)).orElseThrow(NoSuchElementException::new);
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}

}
