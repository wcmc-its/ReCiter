package reciter.xml.retriever.engine;

import java.io.IOException;

import reciter.model.author.TargetAuthor;

public interface ReCiterRetrievalEngine {

	void retrieve(TargetAuthor targetAuthor) throws IOException;
}
