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
				String targetAuthorMiddleInitial = targetAuthor.getAuthorName().getMiddleInitial();
				String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();
				
				if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName) &&
					StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) &&
					StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName))
					
					return true;
			}
		}
		return false;
	}
}
