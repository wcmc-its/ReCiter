package reciter.engine;

import java.util.List;
import java.util.Map;

import reciter.database.mongo.model.Identity;
import reciter.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;

public interface Engine {

	Analysis run(Identity identity, List<ReCiterArticle> reCiterArticleList);

	Map<String, Long> getMeshTermCache();

	void setMeshTermCache(Map<String, Long> meshTermCache);
}
