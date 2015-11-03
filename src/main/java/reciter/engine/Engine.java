package reciter.engine;

import reciter.model.author.TargetAuthor;

public interface Engine {

	ReCiterEngineProperty getReCiterEngineProperty();
	void run(TargetAuthor targetAuthor);
	void run();
}
