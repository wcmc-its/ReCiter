package reciter.algorithm.evidence.targetauthor.name.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class NameStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		boolean isMatchName = false;

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
					
					isMatchName = true;
			}
		}
		if (isMatchName)
			return 1;
		else
			return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, targetAuthor);
		}
		return sum;
	}
}
