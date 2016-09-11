package reciter.algorithm.evidence.targetauthor.grant.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.database.mongo.model.Feature;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;

public class KnownCoinvestigatorStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		double score = 0;
//		List<AuthorName> authorNames = identity.getGrantCoauthors();
		List<AuthorName> authorNames = identity.getRelatedAuthorNames();

		if (authorNames != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				// do not match target author's name
				if (!author.getAuthorName().firstInitialLastNameMatch(identity.getAuthorName())) {
					for (AuthorName authorName : authorNames) {
						if (authorName.isFullNameMatch(author.getAuthorName())) {
							reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[known co-investigator match: " + 
									authorName + "] ");
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
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, identity);
		}
		return sum;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		int score = 0;
		List<AuthorName> authorNames = identity.getRelatedAuthorNames();

		if (authorNames != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				// do not match target author's name
				if (!author.getAuthorName().firstInitialLastNameMatch(identity.getAuthorName())) {
					for (AuthorName authorName : authorNames) {
						if (authorName.isFullNameMatch(author.getAuthorName())) {
							score += 1;
						}
					}
				}
			}
			reCiterArticle.setKnownCoinvestigatorScore(score);
		}
		feature.setNumKnownCoinvestigators(score);
	}

}
