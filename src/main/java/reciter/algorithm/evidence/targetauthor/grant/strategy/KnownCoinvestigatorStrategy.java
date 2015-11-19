package reciter.algorithm.evidence.targetauthor.grant.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class KnownCoinvestigatorStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		double score = 0;
		List<AuthorName> authorNames = targetAuthor.getGrantCoauthors();

		if (authorNames != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				// do not match target author's name
				if (!author.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName())) {
					for (AuthorName authorName : authorNames) {
						if (authorName.isFullNameMatch(author.getAuthorName())) {
							score += 1;
						}
					}
				}
			}
			reCiterArticle.setKnownCoinvestigatorScore(score);
		}
		return score;
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
