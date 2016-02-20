package reciter.algorithm.evidence.targetauthor;

import org.apache.commons.lang3.StringUtils;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public abstract class AbstractTargetAuthorStrategy implements TargetAuthorStrategy {

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
							if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName)) {
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
							if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName) &&
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
