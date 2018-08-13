package reciter.algorithm.evidence.targetauthor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.util.ReCiterStringUtil;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class TargetAuthorSelection {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(TargetAuthorSelection.class);
	
	/**
	 * This function identifies target author for articles.
	 * @see <a href="https://github.com/wcmc-its/ReCiter/issues/185">Details</a>
	 * @param reciterArticles
	 * @param identity
	 */
	public void identifyTargetAuthor(List<ReCiterArticle> reciterArticles, Identity identity) {
		
		for(ReCiterArticle reciterArticle: reciterArticles) {
			
			if(reciterArticle.getArticleId() == 29603699) {
				slf4jLogger.info("here");
			}
			ReCiterArticleAuthors authors = reciterArticle.getArticleCoAuthors();
			Set<ReCiterAuthor> multipleMarkedTargetAuthor = new HashSet<ReCiterAuthor>();
			if (authors != null) {
				int emailMatchcount = 0;
				int lastMiddleFirstMatchCount = 0;
				int lastNameMiddleInitialFirstMatchCount = 0;
				int lastNameFirstNameMatchCount = 0;
				int lastNameFirstNameSubstringIdentityMatchCount = 0;
				int lastNameFirstNameIdentitySubstringMatchCount = 0;
				int lastNameFirstInitialMatchCount = 0;
				int middleToFirstInitialAndFirstInitialToMiddleMatchCount = 0;
				int lastNamePartialFirstInitialMatchCount = 0;
				int lastNameMatchCount = 0;
				int firstNameMatchCount = 0;
				int fullLastNameToIdentityPartialMatchCount = 0;
				
	            emailMatchcount = checkEmailMatch(authors.getAuthors(), identity, emailMatchcount, multipleMarkedTargetAuthor);
	            if(emailMatchcount == 0 || emailMatchcount > 1)
	            	lastMiddleFirstMatchCount = checkExactLastMiddleFirstNameMatch(authors.getAuthors(), identity, emailMatchcount, multipleMarkedTargetAuthor);
	            if(emailMatchcount == 1) {	
	            	slf4jLogger.info("Email Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastMiddleFirstMatchCount == 0 || lastMiddleFirstMatchCount > 1)
	            	lastNameMiddleInitialFirstMatchCount = checkExactLastMiddleInitialFirstNameMatch(authors.getAuthors(), identity, lastMiddleFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastMiddleFirstMatchCount ==1) {
	            	slf4jLogger.info("Exact Last Name, Middle Name and First Name Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameMiddleInitialFirstMatchCount == 0 || lastNameMiddleInitialFirstMatchCount > 1)
	            	lastNameFirstNameMatchCount = checkExactLastFirstNameMatch(authors.getAuthors(), identity, lastNameMiddleInitialFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameMiddleInitialFirstMatchCount == 1) {
	            	slf4jLogger.info("Last Name Middle Initial and First Name Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstNameMatchCount == 0 || lastNameFirstNameMatchCount > 1)
	            	lastNameFirstNameSubstringIdentityMatchCount = checkExactLastFirstNamePartialSubstringIdentityMatch(authors.getAuthors(), identity, lastNameFirstNameMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameFirstNameMatchCount == 1) {
	            	slf4jLogger.info("Last Name First Name exact Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstNameSubstringIdentityMatchCount == 0 || lastNameFirstNameSubstringIdentityMatchCount > 1)
	            	lastNameFirstNameIdentitySubstringMatchCount = checkExactLastFirstNamePartialIdentityPartialSubstringMatch(authors.getAuthors(), identity, lastNameMiddleInitialFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameFirstNameSubstringIdentityMatchCount == 1) {
	            	slf4jLogger.info("Last Name First Name partial match of Identity Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstNameIdentitySubstringMatchCount == 0 || lastNameFirstNameIdentitySubstringMatchCount > 1)
	            	lastNameFirstInitialMatchCount = checkExactLastFirstInitialNameMatch(authors.getAuthors(), identity, lastNameMiddleInitialFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameFirstNameIdentitySubstringMatchCount == 1) {
	            	slf4jLogger.info("Last Name Identity First name partial of Article Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstInitialMatchCount == 0 || lastNameFirstInitialMatchCount > 1)
	            	middleToFirstInitialAndFirstInitialToMiddleMatchCount = checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(authors.getAuthors(), identity, lastNameFirstInitialMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameFirstInitialMatchCount == 1) {
	            	slf4jLogger.info("Last Name First Initial exact Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(middleToFirstInitialAndFirstInitialToMiddleMatchCount == 0 || middleToFirstInitialAndFirstInitialToMiddleMatchCount > 1)
	            	lastNamePartialFirstInitialMatchCount = checkPartialLastNameFirstInitialMatch(authors.getAuthors(), identity, middleToFirstInitialAndFirstInitialToMiddleMatchCount, multipleMarkedTargetAuthor);
	            if(middleToFirstInitialAndFirstInitialToMiddleMatchCount == 1) {
	            	slf4jLogger.info("Middle intial to first initial and first intial to middle initial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNamePartialFirstInitialMatchCount == 0 || lastNamePartialFirstInitialMatchCount > 1)
	            	lastNameMatchCount = checkLastNameExactMatch(authors.getAuthors(), identity, lastNamePartialFirstInitialMatchCount, multipleMarkedTargetAuthor);
	            if(lastNamePartialFirstInitialMatchCount == 1) {
	            	slf4jLogger.info("Last Name Partial First Initial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameMatchCount == 0 || lastNameMatchCount > 1)
	            	firstNameMatchCount = checkFirstNameExactMatch(authors.getAuthors(), identity, lastNameMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameMatchCount == 1) {
	            	slf4jLogger.info("Exact First name match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(firstNameMatchCount == 0 || firstNameMatchCount > 1)
	            	fullLastNameToIdentityPartialMatchCount = checkLastNameFullArticleToIdentityPartialMatch(authors.getAuthors(), identity, firstNameMatchCount, multipleMarkedTargetAuthor);
	            if(firstNameMatchCount == 1) {
	            	slf4jLogger.info("Exact First name match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(fullLastNameToIdentityPartialMatchCount == 0) {
	            	slf4jLogger.info("There was no target author found for " + reciterArticle.getArticleId());
	            	assignTargetAuthorFalse(authors.getAuthors());
	            }
	            else if(fullLastNameToIdentityPartialMatchCount > 1) {
	            	slf4jLogger.info(fullLastNameToIdentityPartialMatchCount + " authors were marked as target author for article " + reciterArticle.getArticleId());
	            }
	            if(fullLastNameToIdentityPartialMatchCount == 1) {
	            	slf4jLogger.info("Full Last Name match to partial Identity Last Name: " + reciterArticle.getArticleId());
	            	continue;
	            }
            	
            		
	        }
		}
		
	}
	
	//Step 1 : attempt email match if match then automatically its a target author
	public int checkEmailMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		String affiliation = null;
		for (ReCiterAuthor author : authors) {
			if (author.getAffiliation() != null && !author.getAffiliation().isEmpty()) {
				affiliation = author.getAffiliation();
				if(!identity.getEmails().isEmpty() && affiliation != null) {
					for(String email: identity.getEmails()) {
						if(affiliation.contains(email)) {
							author.setTargetAuthor(true);
							matchCount++;
							multipleMarkedTargetAuthor.add(author);
						}
					}
				}
			}
			else
				author.setTargetAuthor(false);
		}
		return matchCount;
	}
	
	//Step 2 : Attempt strict last name, strict middle name, and strict first name match
	public int checkExactLastMiddleFirstNameMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null && author.getAuthorName().getMiddleName() != null &&
					identity.getPrimaryName().getFirstName() != null && identity.getPrimaryName().getLastName() != null && identity.getPrimaryName().getMiddleName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleName().trim())) &&
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getMiddleName() != null && alternateName.getFirstName() != null && alternateName.getLastName() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleName().trim()), 
									ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleName().trim())) &&
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getMiddleName() != null && alternateName.getFirstName() != null && alternateName.getLastName() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	//Step 3 : Attempt strict last name, middle initial, and strict first name match
	public int checkExactLastMiddleInitialFirstNameMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null && author.getAuthorName().getMiddleInitial() != null &&
					identity.getPrimaryName().getFirstName() != null && identity.getPrimaryName().getLastName() != null && identity.getPrimaryName().getMiddleInitial() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) &&
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && alternateName.getMiddleInitial() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) &&
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && alternateName.getMiddleInitial() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
		
	//Step 4 : Attempt strict last name and strict first name match
	public int checkExactLastFirstNameMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null &&
					identity.getPrimaryName().getFirstName() != null && identity.getPrimaryName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	//Attempt strict last name and partial first name match, in which article is substring of identity
	public int checkExactLastFirstNamePartialSubstringIdentityMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null &&
					identity.getPrimaryName().getFirstName() != null && identity.getPrimaryName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()), ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName()), ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()), ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName()), ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	//Attempt strict last name and partial first name match, in which identity is substring of article.
	public int checkExactLastFirstNamePartialIdentityPartialSubstringMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null &&
					identity.getPrimaryName().getFirstName() != null && identity.getPrimaryName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()), ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null && alternateName.getLastName() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()), ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	//Step 5 : Attempt strict last name and first initial match
	public int checkExactLastFirstInitialNameMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null &&
					identity.getPrimaryName().getFirstInitial() != null && identity.getPrimaryName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstInitial() != null && alternateName.getLastName() != null &&
									ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) && 
							StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim()))) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstInitial() != null && alternateName.getLastName() != null &&
									ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()))
									&& ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	//Step 6:Attempt firstInitial to middleInitial, and middleInitial to firstInitial match with strict lastname match
	public int checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null && author.getAuthorName().getMiddleInitial() != null && 
					identity.getPrimaryName().getFirstInitial() != null && identity.getPrimaryName().getLastName() != null && identity.getPrimaryName().getMiddleInitial() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getFirstInitial().length() > 0 && 
							author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 && 
							identity.getPrimaryName().getMiddleInitial() != null && identity.getPrimaryName().getMiddleInitial().length() > 0 &&
							identity.getPrimaryName().getFirstInitial() != null && identity.getPrimaryName().getFirstInitial().length() > 0 &&
							(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getMiddleInitial() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial())))) //FirstInitial to MiddleInitial
						&&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
								identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstInitial() != null && 
								ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//MiddleInitial to First Initial
					) &&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
								identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null && 
								ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))//Exact lastname match
					)) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
				else if(matchCount == 0) {
					if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getFirstInitial().length() > 0 && 
							author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 && 
							identity.getPrimaryName().getMiddleInitial() != null && identity.getPrimaryName().getMiddleInitial().length() > 0 &&
							identity.getPrimaryName().getFirstInitial() != null && identity.getPrimaryName().getFirstInitial().length() > 0 &&
							(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getMiddleInitial() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial())))) //FirstInitial to MiddleInitial
						&&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
								identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstInitial() != null && 
								ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//MiddleInitial to First Initial
					) &&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
								identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null && 
								ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))//Exact lastname match
					)) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	//Step 7: Attempt partial last name and exact first initial match
	public int checkPartialLastNameFirstInitialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
        int count = 0;
        if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null &&
					identity.getPrimaryName().getFirstInitial() != null && identity.getPrimaryName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if((ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()).contains(ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null && 
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).contains(ReCiterStringUtil.deAccent(alternateName.getLastName())))) //Last Name partial Match
						&&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
								identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstInitial() != null && 
								ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//First Initial match
					)) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
				else if(matchCount == 0) {
					if((ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()).contains(ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null && 
							author.getAuthorName().getLastName().contains(alternateName.getLastName()))) //Last Name partial Match
						&&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
								identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstInitial() != null &&  
								ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//First Initial match
					)) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	
	//Step 8: attempt lastName match from article to identity using all name aliases in identity table
	public int checkLastNameExactMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getLastName() != null && identity.getPrimaryName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	//Step 9: attempt strict firstname match
	public int checkFirstNameExactMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstName() != null && identity.getPrimaryName().getFirstName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
					
					
				}
				else if(matchCount == 0) {
					if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getFirstName() != null &&
							ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	//Step 10: Attempt full last name match from article to partial last name from identity. (e.g., Somersan-Karakaya)
	public int checkLastNameFullArticleToIdentityPartialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount, Set<ReCiterAuthor> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new ArrayList<>(multipleMarkedTargetAuthor);
			multipleMarkedTargetAuthor.clear();
		}
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getLastName() != null && identity.getPrimaryName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim()), ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim())) 
							||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null &&
									StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()), ReCiterStringUtil.deAccent(author.getAuthorName().getLastName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
				else if(matchCount == 0) {
					if(StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim()), ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim())) 
							||
							identity.getAlternateNames().stream().anyMatch(alternateName -> alternateName.getLastName() != null &&
									StringUtils.containsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName()), ReCiterStringUtil.deAccent(author.getAuthorName().getLastName())))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(author);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	
	//Not Used
	public int checkLastNamePartMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
		int count = 0;
		
		for (ReCiterAuthor author : authors) {
			if(matchCount > 1 && author.isTargetAuthor()) {
				if(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()).contains(ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).contains(ReCiterStringUtil.deAccent(alternateName.getLastName())))) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			else if(matchCount == 0 && !author.isTargetAuthor()) {
				if(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()).contains(ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).contains(ReCiterStringUtil.deAccent(alternateName.getLastName())))) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
		}
		return count;
	}
		
	//Not Used
	public int checkFirstNameMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
		int count = 0;
		for (ReCiterAuthor author : authors) {
			if(matchCount > 1 && author.isTargetAuthor()) {
				if(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()).contains(ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).contains(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			else if(matchCount == 0 && !author.isTargetAuthor()) {
				if(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName().trim()).contains(ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstName()).contains(ReCiterStringUtil.deAccent(alternateName.getFirstName())))) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			
		}
		return count;
	}
	
	//Not Used
	public int checkFirstInitialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
        int count = 0;
		for (ReCiterAuthor author : authors) {
			if(author.getAuthorName().getFirstInitial() != null && identity.getPrimaryName().getFirstInitial() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))) {
						author.setTargetAuthor(true);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
				else if(matchCount == 0 && !author.isTargetAuthor()) {
					if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))) {
						author.setTargetAuthor(true);
						count++;
					}
					else
						author.setTargetAuthor(false);
				}
			}
		}
		return count;
	}
	
	public void assignTargetAuthorFalse(List<ReCiterAuthor> authors) {
		for (ReCiterAuthor author : authors) {
			author.setTargetAuthor(false);
		}
	}
}
