package reciter.algorithm.cluster.article.scorer;

import java.util.List;
import java.util.Map;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public interface ArticleScorer {
	
	void runArticleScorer(Map<Long, ReCiterCluster> clusters, Identity identity);
	List<ReCiterArticle> executePythonScriptForArticleIdentityTotalScore(List<ReCiterArticle> reCiterArticles, Identity identity);
}
