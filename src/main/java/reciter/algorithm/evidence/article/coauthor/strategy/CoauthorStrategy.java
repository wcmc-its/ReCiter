package reciter.algorithm.evidence.article.coauthor.strategy;

import java.util.List;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class CoauthorStrategy extends AbstractReCiterArticleStrategy {

	private final TargetAuthor targetAuthor;

	public CoauthorStrategy(TargetAuthor targetAuthor) {
		this.targetAuthor = targetAuthor;
	}

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		return getNumberOfMutualCoauthors(reCiterArticle, otherReCiterArticle);
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Determine whether two reCiterArticles contain mutual authors.
	 * @param reCiterArticle
	 * @param targetAuthor
	 * @return
	 */
	public int getNumberOfMutualCoauthors(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {

		int numCoauthorMatches = 0;
		for (ReCiterAuthor authorA : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			for (ReCiterAuthor authorB : otherReCiterArticle.getArticleCoAuthors().getAuthors()) {

				// Only check authors except for the target author (because target author always matches)
				if (!authorA.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName()) &&
					!authorB.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName()) &&
					 authorA.getAuthorName().isFullNameMatch(authorB.getAuthorName())) {

					++numCoauthorMatches;
				}
			}
		}
		return numCoauthorMatches;
	}
}
