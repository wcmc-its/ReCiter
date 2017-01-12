package reciter.algorithm.evidence.article.coauthor.strategy;

import java.util.List;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class CoauthorStrategy extends AbstractReCiterArticleStrategy {

	private final Identity identity;

	public CoauthorStrategy(Identity identity) {
		this.identity = identity;
	}

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		double score = getNumberOfMutualCoauthors(reCiterArticle, otherReCiterArticle);
		reCiterArticle.setCoauthorStrategyScore(score);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, otherReCiterArticle);
		}
		return sum;
	}

	/**
	 * Determine whether two reCiterArticles contain mutual authors.
	 * @param reCiterArticle
	 * @param identity
	 * @return
	 */
	public int getNumberOfMutualCoauthors(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {

		int numCoauthorMatches = 0;
		for (ReCiterAuthor authorA : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			for (ReCiterAuthor authorB : otherReCiterArticle.getArticleCoAuthors().getAuthors()) {

				// Only check authors except for the target author (because target author always matches)
				if (!authorA.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName()) &&
					!authorB.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName()) &&
					 authorA.getAuthorName().isFullNameMatch(authorB.getAuthorName())) {

					++numCoauthorMatches;
				}
			}
		}
		return numCoauthorMatches;
	}
}
