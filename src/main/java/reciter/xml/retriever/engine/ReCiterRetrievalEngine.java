package reciter.xml.retriever.engine;

import reciter.model.author.TargetAuthor;

public interface ReCiterRetrievalEngine {

	RetrievalSession selectSession(TargetAuthor targetAuthor);
}
