package reciter.engine;

import java.util.List;

import reciter.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public interface Engine {

	ReCiterEngineProperty getReCiterEngineProperty();
	void run(TargetAuthor targetAuthor);
	void run();
	Analysis constructAnalysis();
	void checkNumQueries();
	void run(String cwid);
	void run(TargetAuthor targetAuthor, List<ReCiterArticle> reCiterArticleList);
	Analysis getAnalysis();
}
