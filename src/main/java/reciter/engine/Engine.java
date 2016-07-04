package reciter.engine;

import java.util.List;

import reciter.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface Engine {

//	ReCiterEngineProperty getReCiterEngineProperty();
	Analysis run(TargetAuthor targetAuthor, List<ReCiterArticle> reCiterArticleList);
}
