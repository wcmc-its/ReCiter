package reciter.algorithm.evidence.targetauthor.articlesize.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class ArticleSizeStrategy extends AbstractTargetAuthorStrategy {

	private static final int FIRST_LEVEL = 200;
	private static final int SECOND_LEVEL = 500;
	/**
	 * If a person has < 200 candidate publications, assume that the person wrote it in these circumstances:
	 * 1. Matching full first name (Richard Granstein, e.g., 6605225)
	 * 2. Matching first initial and middle initial (RD Granstein, e.g., 8288913)
	 * 
	 * If a person has < 500 candidate publications, assume that the person wrote it in these circumstances:
	 * 3. Both full first name and matching middle initial (Richard D. Granstein, e.g., 6231484, or Carl F. Nathan, e.g., 3989315)
	 */
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {

				String lastName = author.getAuthorName().getLastName();
				String firstName = author.getAuthorName().getFirstName();
				String middleInitial = author.getAuthorName().getMiddleInitial();

				String targetAuthorFirstName = targetAuthor.getAuthorName().getFirstName();
				String targetAuthorMiddleInitial = targetAuthor.getAuthorName().getMiddleInitial();
				String targetAuthorLastName = targetAuthor.getAuthorName().getLastName();

				if (lastName.equals(targetAuthorLastName)) {
					if ((targetAuthorFirstName.equalsIgnoreCase(firstName) || 
							(middleInitial.length() > 0 && middleInitial.equalsIgnoreCase(targetAuthorMiddleInitial))) &&
							targetAuthor.getArticleSize() < FIRST_LEVEL) {
						
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
								" [article size < 200 : pmid=" + reCiterArticle.getArticleId() + " is gold standard=" + reCiterArticle.getGoldStandard() + "]");
						
						return 1;
					} else if (targetAuthorFirstName.equalsIgnoreCase(firstName) && middleInitial.length() > 0 &&
							middleInitial.equalsIgnoreCase(targetAuthorMiddleInitial) &&
							targetAuthor.getArticleSize() < SECOND_LEVEL) {
						
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
								" [article size < 500 : pmid=" + reCiterArticle.getArticleId() + " is gold standard=" + reCiterArticle.getGoldStandard() + "]");
						return 1;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}

}
