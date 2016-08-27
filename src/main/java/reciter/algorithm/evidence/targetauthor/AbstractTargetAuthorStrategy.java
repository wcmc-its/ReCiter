package reciter.algorithm.evidence.targetauthor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.ReCiterAuthor;
import reciter.string.ReCiterStringUtil;

public abstract class AbstractTargetAuthorStrategy implements TargetAuthorStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractTargetAuthorStrategy.class);

	protected boolean matchAuthorName(ReCiterArticle reCiterArticle, Identity identity) {
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String firstName = author.getAuthorName().getFirstName();
				String middleInitial = author.getAuthorName().getMiddleInitial();
				String lastName = author.getAuthorName().getLastName();

				String targetAuthorFirstName = identity.getAuthorName().getFirstName();
				String targetAuthorFirstNameInitial = identity.getAuthorName().getFirstInitial();
				String targetAuthorMiddleInitial = identity.getAuthorName().getMiddleInitial();
				String targetAuthorLastName = identity.getAuthorName().getLastName();

				// Case: ses9022, Last name = 'Somersan Karakaya', PMID = 11673488, last name = 'Somersan'.
				// Split last name and check if each individual parts match the last name from article.
				boolean partNameMatch = false;
				String[] targetAuthorLastNameArray = targetAuthorLastName.split("\\s+");
				if (targetAuthorLastNameArray.length > 1) {
					for (int i = 0; i < targetAuthorLastNameArray.length; i++) {
						if (StringUtils.equalsIgnoreCase(lastName, targetAuthorLastNameArray[i])) {
							partNameMatch = true;
							break;
						}
					}
				}
				if (StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName) || partNameMatch) {
					if (middleInitial.length() == 0) {
						// If first name is only a first initial, and no middle name.
						// check using first name of target author.
						if (firstName.length() == 1) {
							if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstNameInitial)) {
								return true;
							}
						} else {
							int levenshteinDist = ReCiterStringUtil.levenshteinDistance(firstName, targetAuthorFirstName);
							boolean isAcceptableDistance = Double.valueOf(levenshteinDist) / firstName.length() <= 0.25;
							
							if (isAcceptableDistance) {
								slf4jLogger.info("PMID=[" + reCiterArticle.getArticleId() + "], levenshtein distance of firstName=[" + 
										firstName + "] targetAuthorFirstName=[" + targetAuthorFirstName + "] is [" + levenshteinDist + "] is accepted");
							}

							// Case: PMID = 12069979, first name = "Juan", in db name = "Juan Miguel".
							boolean isFirstNameMatchByPart = isFirstNameMatchByPart(targetAuthorFirstName, firstName);
							
							if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName) || isAcceptableDistance || isFirstNameMatchByPart) {
								return true;
							}
						}
					} // If first name is full, and middle initial exist.
					else {
						if (firstName.length() == 1) {
							if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstNameInitial) &&
								StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial)) {
								return true;
							}
						} else {
							int levenshteinDist = ReCiterStringUtil.levenshteinDistance(firstName, targetAuthorFirstName);
							boolean isAcceptableDistance = Double.valueOf(levenshteinDist) / firstName.length() <= 0.25;
							
							if (isAcceptableDistance) {
								slf4jLogger.info("PMID=[" + reCiterArticle.getArticleId() + "], levenshtein distance of firstName=[" + 
										firstName + "] targetAuthorFirstName=[" + targetAuthorFirstName + "] is [" + levenshteinDist + "] is accepted");
							}
							
							// Case: PMID = 12069979, first name = "Juan", in db name = "Juan Miguel".
							boolean isFirstNameMatchByPart = isFirstNameMatchByPart(targetAuthorFirstName, firstName);
							
							if ((StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName) || isAcceptableDistance || isFirstNameMatchByPart) &&
								StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private boolean isFirstNameMatchByPart(String targetAuthorFirstName, String firstName) {
		// Case: PMID = 12069979, first name = "Juan", in db name = "Juan Miguel".
		boolean isFirstNameMatchByPart = false;
		String[] firstNameParts = targetAuthorFirstName.split("\\s+");
		if (firstNameParts.length > 0) {
			for (int i = 0; i < firstNameParts.length; i++) {
				if (StringUtils.equalsIgnoreCase(firstName, firstNameParts[i])) {
					isFirstNameMatchByPart = true;
					break;
				}
			}
		}
		return isFirstNameMatchByPart;
	}

	protected boolean matchRelaxedAuthorName(ReCiterArticle reCiterArticle, Identity identity) {
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String firstInitial = author.getAuthorName().getFirstInitial();
				String middleInitial = author.getAuthorName().getMiddleInitial();
				String lastName = author.getAuthorName().getLastName();

				String targetAuthorFirstInitial = identity.getAuthorName().getFirstInitial();
				String targetAuthorMiddleInitial = identity.getAuthorName().getMiddleInitial();
				String targetAuthorLastName = identity.getAuthorName().getLastName();

				if (StringUtils.equalsIgnoreCase(firstInitial, targetAuthorFirstInitial) &&
						StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) &&
						StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName))

					return true;
			}
		}
		return false;
	}
}
