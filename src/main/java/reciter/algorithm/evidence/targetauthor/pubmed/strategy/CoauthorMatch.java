package reciter.algorithm.evidence.targetauthor.pubmed.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class CoauthorMatch extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/**
	 * Determine whether two reCiterArticles contain mutual authors.
	 * @param reCiterArticle
	 * @param targetAuthor
	 * @return
	 */
	public boolean containsMutualCoauthors(ReCiterArticle reCiterArticleA, ReCiterArticle reCiterArticleB, TargetAuthor targetAuthor) {

		for (ReCiterAuthor authorA : reCiterArticleA.getArticleCoAuthors().getAuthors()) {
			for (ReCiterAuthor authorB : reCiterArticleB.getArticleCoAuthors().getAuthors()) {

				// do not match target author's name
				if (!authorA.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName()) &&
						!authorB.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName())) {

					if (authorA.getAuthorName().isFullNameMatch(authorB.getAuthorName())) {
						//						slf4jLogger.info(authorA.getAuthorName().toString());
						return true;
					}
				}

			}
		}
		return false;
	}
}
