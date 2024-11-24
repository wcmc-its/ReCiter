package reciter.algorithm.evidence.targetauthor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.util.ReCiterStringUtil;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

/**
 * @author szd2013
 * <p>This class identifies target author for a ReCiterArticle based on names in Idenity table<p>
 *
 */
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
			//Setting this for debug purposes
			if(reciterArticle.getArticleId() == 15590364) {
				slf4jLogger.info("here");
			}
			ReCiterArticleAuthors authors = reciterArticle.getArticleCoAuthors();
			Set<Entry<ReCiterAuthor, ReCiterAuthor>> sanitizedAritcleAuthors = authors.getSanitizedAuthorMap().entrySet();
			List<AuthorName> sanitizedIdentityAuthors = new ArrayList<AuthorName>(identity.getSanitizedNames().values());
			
			Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>();
			if (authors != null && !sanitizedAritcleAuthors.isEmpty()) {
				
				int lastMiddleFirstMatchCount = 0;
				int lastNameMiddleInitialFirstMatchCount = 0;
				int lastNameFirstNameMatchCount = 0;
				int lastNameFirstNameSubstringIdentityMatchCount = 0;
				int lastNameFirstNameIdentitySubstringMatchCount = 0;
				int lastNameFirstInitialMatchCount = 0;
				int emailMatchcount = 0;
				int middleToFirstInitialAndFirstInitialToMiddleMatchCount = 0;
				int lastNamePartialFirstInitialMatchCount = 0;
				int lastNameMatchCount = 0;
				int firstNameMatchCount = 0;
				int fullLastNameToIdentityPartialMatchCount = 0;
	            
	            lastMiddleFirstMatchCount = checkExactLastMiddleFirstNameMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, emailMatchcount, multipleMarkedTargetAuthor);
	            if(lastMiddleFirstMatchCount == 0 || lastMiddleFirstMatchCount > 1)
	            	lastNameMiddleInitialFirstMatchCount = checkExactLastMiddleInitialFirstNameMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, lastMiddleFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastMiddleFirstMatchCount ==1) {
	            	slf4jLogger.info("Exact Last Name, Middle Name and First Name Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameMiddleInitialFirstMatchCount == 0 || lastNameMiddleInitialFirstMatchCount > 1)
	            	lastNameFirstNameMatchCount = checkExactLastFirstNameMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, lastNameMiddleInitialFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameMiddleInitialFirstMatchCount == 1) {
	            	slf4jLogger.info("Last Name Middle Initial and First Name Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstNameMatchCount == 0 || lastNameFirstNameMatchCount > 1)
	            	lastNameFirstNameSubstringIdentityMatchCount = checkExactLastFirstNamePartialSubstringIdentityMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, lastNameFirstNameMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameFirstNameMatchCount == 1) {
	            	slf4jLogger.info("Last Name First Name exact Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstNameSubstringIdentityMatchCount == 0 || lastNameFirstNameSubstringIdentityMatchCount > 1)
	            	lastNameFirstNameIdentitySubstringMatchCount = checkExactLastFirstNamePartialIdentityPartialSubstringMatch(authors.getSanitizedAuthorMap().entrySet(), sanitizedIdentityAuthors, lastNameMiddleInitialFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameFirstNameSubstringIdentityMatchCount == 1) {
	            	slf4jLogger.info("Last Name First Name partial match of Identity Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstNameIdentitySubstringMatchCount == 0 || lastNameFirstNameIdentitySubstringMatchCount > 1)
	            	lastNameFirstInitialMatchCount = checkExactLastFirstInitialNameMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, lastNameMiddleInitialFirstMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameFirstNameIdentitySubstringMatchCount == 1) {
	            	slf4jLogger.info("Last Name Identity First name partial of Article Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameFirstInitialMatchCount == 0 || lastNameFirstInitialMatchCount > 1)
	            	emailMatchcount = checkEmailMatch(sanitizedAritcleAuthors, identity, emailMatchcount, multipleMarkedTargetAuthor);
	            if(lastNameFirstInitialMatchCount == 1) {	
	            	slf4jLogger.info("Last Name First Initial exact Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(emailMatchcount == 0 || emailMatchcount > 1)
	            	middleToFirstInitialAndFirstInitialToMiddleMatchCount = checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, lastNameFirstInitialMatchCount, multipleMarkedTargetAuthor);
	            if(emailMatchcount == 1) {
	            	slf4jLogger.info("Email Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(middleToFirstInitialAndFirstInitialToMiddleMatchCount == 0 || middleToFirstInitialAndFirstInitialToMiddleMatchCount > 1)
	            	lastNamePartialFirstInitialMatchCount = checkPartialLastNameFirstInitialMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, middleToFirstInitialAndFirstInitialToMiddleMatchCount, multipleMarkedTargetAuthor);
	            if(middleToFirstInitialAndFirstInitialToMiddleMatchCount == 1) {
	            	slf4jLogger.info("Middle intial to first initial and first intial to middle initial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNamePartialFirstInitialMatchCount == 0 || lastNamePartialFirstInitialMatchCount > 1)
	            	lastNameMatchCount = checkLastNameExactMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, lastNamePartialFirstInitialMatchCount, multipleMarkedTargetAuthor);
	            if(lastNamePartialFirstInitialMatchCount == 1) {
	            	slf4jLogger.info("Last Name Partial First Initial Match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(lastNameMatchCount == 0 || lastNameMatchCount > 1)
	            	firstNameMatchCount = checkFirstNameExactMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, lastNameMatchCount, multipleMarkedTargetAuthor);
	            if(lastNameMatchCount == 1) {
	            	slf4jLogger.info("Exact First name match found for article: " + reciterArticle.getArticleId());
	            	continue;
	            }
	            
	            if(firstNameMatchCount == 0 || firstNameMatchCount > 1)
	            	fullLastNameToIdentityPartialMatchCount = checkLastNameFullArticleToIdentityPartialMatch(sanitizedAritcleAuthors, sanitizedIdentityAuthors, firstNameMatchCount, multipleMarkedTargetAuthor);
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
	
	//Step 7 : attempt email match if match then automatically its a target author
	/**
	 * Check for email match from affiliation statement with Identity email
	 * @param authors
	 * @param identity
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkEmailMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, Identity identity, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		String affiliation = null;
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if (originalAuthor.getAffiliation() != null && !originalAuthor.getAffiliation().isEmpty()) {
				affiliation = originalAuthor.getAffiliation();
				if(identity!=null && identity.getEmails()!=null && !identity.getEmails().isEmpty() && affiliation != null) {
					for(String email: identity.getEmails()) {
						if(affiliation.contains(email)) {
							author.setTargetAuthor(true);
							originalAuthor.setTargetAuthor(true);
							matchCount++;
							multipleMarkedTargetAuthor.add(entry);
						}
					}
				}
			}
			else {
				author.setTargetAuthor(false);
				originalAuthor.setTargetAuthor(false);
			}
		}
		return matchCount;
	}
	
	//Step 1 : Attempt strict last name, strict middle name, and strict first name match
	/**
	 * Check for exact last name, middle and last name match from article to identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkExactLastMiddleFirstNameMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null && author.getAuthorName().getMiddleName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&&
							sanitizedIdentityName.getMiddleName() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getMiddleName().equalsIgnoreCase(sanitizedIdentityName.getMiddleName())
							&& 
							author.getAuthorName().getFirstName().equalsIgnoreCase(sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
					
					
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&&
							sanitizedIdentityName.getMiddleName() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getMiddleName().equalsIgnoreCase(sanitizedIdentityName.getMiddleName())
							&& 
							author.getAuthorName().getFirstName().equalsIgnoreCase(sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	//Step 2 : Attempt strict last name, middle initial, and strict first name match
	/**
	 * Check for exact last name, middle initial and first name match from article to identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkExactLastMiddleInitialFirstNameMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null && author.getAuthorName().getMiddleInitial() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&&
							sanitizedIdentityName.getMiddleInitial() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getMiddleInitial().equalsIgnoreCase(sanitizedIdentityName.getMiddleInitial())
							&& 
							author.getAuthorName().getFirstName().equalsIgnoreCase(sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&&
							sanitizedIdentityName.getMiddleInitial() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getMiddleInitial().equalsIgnoreCase(sanitizedIdentityName.getMiddleInitial())
							&& 
							author.getAuthorName().getFirstName().equalsIgnoreCase(sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
		
	/**
	 * Check for exact last and first name match from article to identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	//Step 3 : Attempt strict last name and strict first name match
	public int checkExactLastFirstNameMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&&
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getFirstName().equalsIgnoreCase(sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getFirstName().equalsIgnoreCase(sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	//Step 4:Attempt strict last name and partial first name match, in which article is substring of identity
	/**
	 * Check for exact last name and partial first name where article first name is a substring of identity first name
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkExactLastFirstNamePartialSubstringIdentityMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							StringUtils.containsIgnoreCase(sanitizedIdentityName.getFirstName(), author.getAuthorName().getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
					
					
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							StringUtils.containsIgnoreCase(sanitizedIdentityName.getFirstName(), author.getAuthorName().getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	//Step 5:Attempt strict last name and partial first name match, in which identity is substring of article.
	/**
	 * Check for exact last name and partial first name where identity first name is a substring of article first name
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkExactLastFirstNamePartialIdentityPartialSubstringMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstName() != null && author.getAuthorName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							StringUtils.containsIgnoreCase(author.getAuthorName().getFirstName(), sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}	
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							StringUtils.containsIgnoreCase(author.getAuthorName().getFirstName(), sanitizedIdentityName.getFirstName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	/**
	 * Check for exact last and first initial match from article to identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	//Step 6 : Attempt strict last name and first initial match
	public int checkExactLastFirstInitialNameMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstInitial() != null
							&&
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getFirstInitial().equalsIgnoreCase(sanitizedIdentityName.getFirstInitial()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}	
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstInitial() != null
							&&
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getLastName().equalsIgnoreCase(sanitizedIdentityName.getLastName())
							&& 
							author.getAuthorName().getFirstInitial().equalsIgnoreCase(sanitizedIdentityName.getFirstInitial()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	/**
	 * Check for article first initial to middle initial and article middle initial to first initial and exact lastname match
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	//Step 8:Attempt firstInitial to middleInitial, and middleInitial to firstInitial match with strict lastname match
	public int checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null && author.getAuthorName().getMiddleInitial() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getFirstInitial().length() > 0 
							&& 
							author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 
							&& 
							sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstInitial() != null && sanitizedIdentityName.getFirstInitial().length() > 0
							&&
							sanitizedIdentityName.getMiddleInitial() != null && sanitizedIdentityName.getMiddleInitial().length() > 0
							&& 
							(StringUtils.equalsIgnoreCase(author.getAuthorName().getFirstInitial().trim(), sanitizedIdentityName.getMiddleInitial().trim())) //FirstInitial to MiddleInitial
							&&
							(StringUtils.equalsIgnoreCase(author.getAuthorName().getMiddleInitial().trim(), sanitizedIdentityName.getFirstInitial().trim()))//MiddleInitial to First Initial
							&&
							(StringUtils.equalsIgnoreCase(author.getAuthorName().getLastName().trim(), sanitizedIdentityName.getLastName().trim()))//Exact lastname match
						    )) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
				else if(matchCount == 0) {
					if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getFirstInitial().length() > 0 
							&& 
							author.getAuthorName().getMiddleInitial() != null && author.getAuthorName().getMiddleInitial().length() > 0 
							&& 
							sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstInitial() != null && sanitizedIdentityName.getFirstInitial().length() > 0
							&&
							sanitizedIdentityName.getMiddleInitial() != null && sanitizedIdentityName.getMiddleInitial().length() > 0
							&& 
							(StringUtils.equalsIgnoreCase(author.getAuthorName().getFirstInitial().trim(), sanitizedIdentityName.getMiddleInitial().trim())) //FirstInitial to MiddleInitial
							&&
							(StringUtils.equalsIgnoreCase(author.getAuthorName().getMiddleInitial().trim(), sanitizedIdentityName.getFirstInitial().trim()))//MiddleInitial to First Initial
							&&
							(StringUtils.equalsIgnoreCase(author.getAuthorName().getLastName().trim(), sanitizedIdentityName.getLastName().trim()))//Exact lastname match
						    )) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	//Step 9: Attempt partial last name and exact first initial match
	/**
	 * Check for partial last name with exact first initial match from article to identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkPartialLastNameFirstInitialMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
        int count = 0;
        if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstInitial() != null && author.getAuthorName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstInitial() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getFirstInitial().equalsIgnoreCase(sanitizedIdentityName.getFirstInitial())
							&& 
							StringUtils.containsIgnoreCase(author.getAuthorName().getLastName(), sanitizedIdentityName.getLastName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstInitial() != null
							&& 
							sanitizedIdentityName.getLastName() != null
							&& 
							author.getAuthorName().getFirstInitial().equalsIgnoreCase(sanitizedIdentityName.getFirstInitial())
							&& 
							StringUtils.containsIgnoreCase(author.getAuthorName().getLastName(), sanitizedIdentityName.getLastName()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	
	//Step 10: attempt lastName match from article to identity using all name aliases in identity table
	/**
	 * Check for exact last name match from article to identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkLastNameExactMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getLastName() != null
							&&
							StringUtils.equalsIgnoreCase(author.getAuthorName().getLastName().trim(), sanitizedIdentityName.getLastName().trim()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}		
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getLastName() != null
							&&
							StringUtils.equalsIgnoreCase(author.getAuthorName().getLastName().trim(), sanitizedIdentityName.getLastName().trim()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	//Step 11: attempt strict firstname match
	/**
	 * Check for exact first name match from article to identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkFirstNameExactMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getFirstName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&&
							StringUtils.equalsIgnoreCase(author.getAuthorName().getFirstName().trim(), sanitizedIdentityName.getFirstName().trim()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getFirstName() != null
							&&
							StringUtils.equalsIgnoreCase(author.getAuthorName().getFirstName().trim(), sanitizedIdentityName.getFirstName().trim()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
		}
		return count;
	}
	
	//Step 12: Attempt full last name match from article to partial last name from identity. (e.g., Somersan-Karakaya)
	/**
	 * Check for full last name with partial last name from identity
	 * @param authors
	 * @param sanitizedIdentityAuthors
	 * @param matchCount
	 * @param multipleMarkedTargetAuthor
	 * @return
	 */
	public int checkLastNameFullArticleToIdentityPartialMatch(Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors, List<AuthorName> sanitizedIdentityAuthors, int matchCount, Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor) {
		int count = 0;
		if(matchCount > 1) {
			authors = new HashSet<Entry<ReCiterAuthor, ReCiterAuthor>>(multipleMarkedTargetAuthor);
		}
		for (Entry<ReCiterAuthor, ReCiterAuthor> entry : authors) {
			ReCiterAuthor author = entry.getValue();
			ReCiterAuthor originalAuthor = entry.getKey();
			if(author.getAuthorName().getLastName() != null) {
				if(matchCount > 1 && author.isTargetAuthor()) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getLastName() != null
							&&
							StringUtils.containsIgnoreCase(sanitizedIdentityName.getLastName().trim(), author.getAuthorName().getLastName().trim()))) {
						author.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						originalAuthor.setTargetAuthor(true);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
				else if(matchCount == 0) {
					if(sanitizedIdentityAuthors.stream().anyMatch(sanitizedIdentityName -> sanitizedIdentityName.getLastName() != null
							&&
							StringUtils.containsIgnoreCase(sanitizedIdentityName.getLastName().trim(), author.getAuthorName().getLastName().trim()))) {
						author.setTargetAuthor(true);
						originalAuthor.setTargetAuthor(true);
						multipleMarkedTargetAuthor.add(entry);
						count++;
					}
					else {
						author.setTargetAuthor(false);
						originalAuthor.setTargetAuthor(false);
					}
				}
			}
		}
		if(matchCount > 1 && count == 0) {
			return matchCount;
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
