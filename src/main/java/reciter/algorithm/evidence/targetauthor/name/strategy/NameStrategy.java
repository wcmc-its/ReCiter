package reciter.algorithm.evidence.targetauthor.name.strategy;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.ReCiterAuthor;

public class NameStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		boolean isMatchName = false;

		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
				String firstName = author.getAuthorName().getFirstName();
				String middleInitial = author.getAuthorName().getMiddleInitial();
				String lastName = author.getAuthorName().getLastName();
				
				String targetAuthorFirstName = identity.getAuthorName().getFirstName();
				String targetAuthorMiddleInitial = identity.getAuthorName().getMiddleInitial();
				String targetAuthorLastName = identity.getAuthorName().getLastName();
				
				if (StringUtils.equalsIgnoreCase(firstName, targetAuthorFirstName) &&
					StringUtils.equalsIgnoreCase(middleInitial, targetAuthorMiddleInitial) &&
					StringUtils.equalsIgnoreCase(lastName, targetAuthorLastName))
					
					isMatchName = true;
			}
		}

		if (isMatchName) {
			reCiterArticle.setNameStrategyScore(1);
			return 1;
		} else {
			reCiterArticle.setNameStrategyScore(0);
			return 0;
		}
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, identity);
		}
		return sum;
	}
}
