package reciter.xml.retriever.engine;

import java.util.List;

import reciter.model.author.TargetAuthor;

public interface ReCiterRetrievalEngine {

	List<Long> retrieve(TargetAuthor targetAuthor);
}
