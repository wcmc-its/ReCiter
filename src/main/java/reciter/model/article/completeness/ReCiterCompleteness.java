package reciter.model.article.completeness;

import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;

public class ReCiterCompleteness implements ArticleCompleteness {

	@Override
	public double computeCompleteness(ReCiterArticle article, ReCiterAuthor target) {
		double articleCompleteness = 0;
		int numAuthorFullNames = 0;
		int totalNumberOfAuthors = article.getArticleCoAuthors().getNumberOfAuthors();
		
		if (article.getArticleTitle() != null && article.getArticleTitle().length() != 0) {
			articleCompleteness += 1;
		}
		if (article.getJournal().exist()) {
			articleCompleteness += 1;
		}
		if (article.getArticleKeywords().exist()) {
			articleCompleteness += 1;
		}
		if (article.getArticleCoAuthors().exist()) {
			for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
//				articleCompleteness += author.getAuthorName().nameSimilarityScore(target.getAuthorName());
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
