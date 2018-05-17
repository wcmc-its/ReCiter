package reciter.algorithm.evidence.targetauthor;

import java.util.List;

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
	
	public void identifyTargetAuthor(List<ReCiterArticle> reciterArticles, Identity identity) {
		
		for(ReCiterArticle reciterArticle: reciterArticles) {
			ReCiterArticleAuthors authors = reciterArticle.getArticleCoAuthors();
			if (authors != null) {
				int emailMatchcount = 0;
				int lastNameMatchCount = 0;
				int lastNamePartialMatchCount = 0;
				int lastNameFirstInitialMatchCount = 0;
				int lastNameMiddleInitialMatchCount = 0;
				int firstNameMatchCount = 0;
				int firstInitialMatchCount = 0;
				int firstInitialMiddleInitialCount = 0;
	            emailMatchcount = checkEmailMatch(authors.getAuthors(), identity, emailMatchcount);
	            if(emailMatchcount == 0 || emailMatchcount > 1)
	            	lastNameMatchCount = checkLastNameExactMatch(authors.getAuthors(), identity, emailMatchcount);
	            if(emailMatchcount == 1) {	
	            	slf4jLogger.info("Email Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameMatchCount == 0 || lastNameMatchCount > 1)
	            	lastNamePartialMatchCount = checkLastNamePartMatch(authors.getAuthors(), identity, lastNameMatchCount);
	            if(lastNameMatchCount ==1) {
	            	slf4jLogger.info("Exact Last Name Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNamePartialMatchCount == 0 || lastNamePartialMatchCount > 1)
	            	lastNameFirstInitialMatchCount = checkLastNameFirstInitialMatch(authors.getAuthors(), identity, lastNamePartialMatchCount);
	            if(lastNamePartialMatchCount == 1) {
	            	slf4jLogger.info("Last Name partial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstInitialMatchCount == 0 || lastNameFirstInitialMatchCount > 1)
	            	lastNameMiddleInitialMatchCount = checkLastNameMiddleInitialMatch(authors.getAuthors(), identity, lastNameFirstInitialMatchCount);
	            if(lastNameFirstInitialMatchCount == 1) {
	            	slf4jLogger.info("Last Name First Initial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameMiddleInitialMatchCount == 0 || lastNameMiddleInitialMatchCount > 1)
	            	firstNameMatchCount = checkFirstNameMatch(authors.getAuthors(), identity, lastNameMiddleInitialMatchCount);
	            if(lastNameMiddleInitialMatchCount == 1) {
	            	slf4jLogger.info("Last Name Middle Initial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(firstNameMatchCount == 0 || firstNameMatchCount > 1)
	            	firstInitialMatchCount = checkFirstInitialMatch(authors.getAuthors(), identity, firstNameMatchCount);
	            if(firstNameMatchCount == 1) {
	            	slf4jLogger.info("First Name Contains Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(firstInitialMatchCount == 0 || firstInitialMatchCount > 1)
	            	firstInitialMiddleInitialCount = checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(authors.getAuthors(), identity, firstInitialMatchCount);
	            if(firstInitialMatchCount == 1) {
	            	slf4jLogger.info("First Initial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(firstInitialMiddleInitialCount == 0 || firstInitialMiddleInitialCount > 1)
	            	slf4jLogger.info("There was no target author found for " + reciterArticle.getArticleId());
	            if(firstInitialMiddleInitialCount == 1)
	            	continue;
            	
            		
	        }
		}
		
	}
	
	//Step 1 : attempt email match if match then automatically its a target author
	public int checkEmailMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
		String affiliation = null;
		for (ReCiterAuthor author : authors) {
			if (author.getAffiliation() != null && !author.getAffiliation().isEmpty()) {
				affiliation = author.getAffiliation();
				if(!identity.getEmails().isEmpty() && affiliation != null) {
					for(String email: identity.getEmails()) {
						if(affiliation.contains(email)) {
							author.setTargetAuthor(true);
							matchCount++;
						}
					}
				}
			}
			else
				author.setTargetAuthor(false);
		}
		return matchCount;
	}
	
	//Step 2: attempt lastName match from article to identity using all name aliases in identity table
	public int checkLastNameExactMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
		int count = 0;
		
		for (ReCiterAuthor author : authors) {
			if(matchCount > 1 && author.isTargetAuthor()) {
				if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
				
				
			}
			else if(matchCount == 0 && !author.isTargetAuthor()) {
				if(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
		}
		return count;
	}
	
	//Step 3: Attempt %lastName OR lastName% match.
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
	
	//Step 4: Attempt lastName, firstInitial match
	public int checkLastNameFirstInitialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
        int count = 0;
		for (ReCiterAuthor author : authors) {
			if(matchCount > 1 && author.isTargetAuthor()) {
				if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))) //Last Name Match
					&&
					(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//First Initial match
				)) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			else if(matchCount == 0 && !author.isTargetAuthor()) {
				if((StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> author.getAuthorName().getLastName().equalsIgnoreCase(alternateName.getLastName()))) //Last Name Match
					&&
					(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//First Initial match
				)) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			
		}
		return count;
	}
	
	//Step 5: Attempt lastName, middleInitial match
	public int checkLastNameMiddleInitialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
        
		int count = 0;
		for (ReCiterAuthor author : authors) {
			if(matchCount > 1 && author.isTargetAuthor()) {
				if(author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 && (StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))) //Last Name Match
					&&
					(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial())))//Middle Initial match
				)) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			else if(matchCount == 0 && !author.isTargetAuthor()) {
				if(author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 && (StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getLastName().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getLastName().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getLastName()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getLastName())))) //Last Name Match
					&&
					(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial())))//Middle Initial match
				)) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
		}
		return count;
	}
		
	//Step 6: Attempt %firstName% match
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
	
	//Step 7: Attempt Attempt FirstInitial match
	public int checkFirstInitialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
        int count = 0;
		for (ReCiterAuthor author : authors) {
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
		return count;
	}
	
	//Step 8:Attempt firstInitial to middleInitial, and middleInitial to firstInitial match
	public int checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(List<ReCiterAuthor> authors, Identity identity, int matchCount) {
		int count = 0;
		for (ReCiterAuthor author : authors) {
			if(matchCount > 1 && author.isTargetAuthor()) {
				if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getFirstInitial().length() > 0 && 
						author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 && 
						identity.getPrimaryName().getMiddleInitial() != null && identity.getPrimaryName().getMiddleInitial().length() > 0 &&
						identity.getPrimaryName().getFirstInitial() != null && identity.getPrimaryName().getFirstInitial().length() > 0 &&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial())))) //FirstInitial to MiddleInitial
					&&
					(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//MiddleInitial to First Initial
				)) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			else if(matchCount == 0 && !author.isTargetAuthor()) {
				if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getFirstInitial().length() > 0 && 
						author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 &&
						identity.getPrimaryName().getMiddleInitial() != null && identity.getPrimaryName().getMiddleInitial().length() > 0 &&
						identity.getPrimaryName().getFirstInitial() != null && identity.getPrimaryName().getFirstInitial().length() > 0 &&
						(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getMiddleInitial().trim())) ||
						identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getFirstInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getMiddleInitial())))) //FirstInitial to MiddleInitial
					&&
					(StringUtils.equalsIgnoreCase(ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial().trim()), ReCiterStringUtil.deAccent(identity.getPrimaryName().getFirstInitial().trim())) ||
							identity.getAlternateNames().stream().anyMatch(alternateName -> ReCiterStringUtil.deAccent(author.getAuthorName().getMiddleInitial()).equalsIgnoreCase(ReCiterStringUtil.deAccent(alternateName.getFirstInitial())))//MiddleInitial to First Initial
				)) {
					author.setTargetAuthor(true);
					count++;
				}
				else
					author.setTargetAuthor(false);
			}
			
		}
		return count;
	}
		
		
		
		


}
