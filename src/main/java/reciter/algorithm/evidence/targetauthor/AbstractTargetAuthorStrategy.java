package reciter.algorithm.evidence.targetauthor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.string.ReCiterStringUtil;

public abstract class AbstractTargetAuthorStrategy implements TargetAuthorStrategy {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(AbstractTargetAuthorStrategy.class);

	protected boolean matchAuthorName(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String firstName = author.getAuthorName().getFirstName();
				String middleInitial = author.getAuthorName().getMiddleInitial();
				String lastName = author.getAuthorName().getLastName();

				String targetAuthorFirstName = targetAuthor.getAuthorName().getFirstName();
				String targetAuthorFirstNameInitial = targetAuthor.getAuthorName().getFirstInitial();
				String targetAuthorMiddleInitial = targetAuthor.getAuthorName().getMiddleInitial();
				String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();

				if (StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName)) {
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
							isAcceptableDistance = false; // TODO remove
							if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName) || isAcceptableDistance) {
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
							isAcceptableDistance = false; // TODO remove.
							if ((StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName) || isAcceptableDistance) &&
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

	protected boolean matchRelaxedAuthorName(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String firstInitial = author.getAuthorName().getFirstInitial();
				String middleInitial = author.getAuthorName().getMiddleInitial();
				String lastName = author.getAuthorName().getLastName();

				String targetAuthorFirstInitial = targetAuthor.getAuthorName().getFirstInitial();
				String targetAuthorMiddleInitial = targetAuthor.getAuthorName().getMiddleInitial();
				String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();

				if (StringUtils.equalsIgnoreCase(firstInitial, targetAuthorFirstInitial) &&
						StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) &&
						StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName))

					return true;
			}
		}
		return false;
	}
}
