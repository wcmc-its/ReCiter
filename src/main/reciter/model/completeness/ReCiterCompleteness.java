package main.reciter.model.completeness;

import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.author.ReCiterAuthor;

public class ReCiterCompleteness implements ArticleCompleteness {

	@Override
	public double computeCompleteness(ReCiterArticle article, ReCiterAuthor target) {
		double articleCompleteness = 0;
		int numAuthorFullNames = 0;
		int totalNumberOfAuthors = article.getArticleCoAuthors().getNumberCoAuthors();
		
		if (article.getArticleTitle().exist()) {
			articleCompleteness += 1;
		}
		if (article.getJournal().exist()) {
			articleCompleteness += 1;
		}
		if (article.getArticleKeywords().exist()) {
			articleCompleteness += 1;
		}
		if (article.getArticleCoAuthors().exist()) {
			for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
				articleCompleteness += author.getAuthorName().nameSimilarityScore(target.getAuthorName());
				if (author.getAuthorName().isFullName()) { numAuthorFullNames += 1; }
			}
		}
		// the percentage of full names is greater than 50%.
		if (totalNumberOfAuthors != 0 && 
			(double) numAuthorFullNames / totalNumberOfAuthors > 0.5) {
			articleCompleteness += 20;
		}
		return articleCompleteness;
	}
}
