package reciter.algorithm.evidence.article.title.strategy;

import java.util.List;

import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

/**
 * Discount matches when a publication is not in English.
 * https://github.com/wcmc-its/ReCiter/issues/103.
 * 
 * @author Jie
 *
 */
public class ArticleTitleInEnglish implements RemoveReCiterArticleStrategyContext {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		String title = reCiterArticle.getArticleTitle();
		if (title != null && title.startsWith("[")) {
			reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[article title starts with '[']");
			reCiterArticle.setArticleTitleStartWithBracket(true);
			return 1;
		} else {
			reCiterArticle.setArticleTitleStartWithBracket(false);
			return 0;
		}
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, TargetAuthor targetAuthor) {
		// TODO Auto-generated method stub
		return 0;
	}
}
