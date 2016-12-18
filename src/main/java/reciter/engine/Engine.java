package reciter.engine;

import java.util.List;

import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public interface Engine {

	Analysis run(EngineParameters parameters);

	List<Feature> generateFeature(Identity identity, List<ReCiterArticle> reCiterArticleList);
}
