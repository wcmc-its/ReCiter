package reciter.engine;

import reciter.erroranalysis.Analysis;
import reciter.model.author.TargetAuthor;

public interface Engine {

	ReCiterEngineProperty getReCiterEngineProperty();
	void run(TargetAuthor targetAuthor);
	void run();
	Analysis constructAnalysis();
	void checkNumQueries();
}
