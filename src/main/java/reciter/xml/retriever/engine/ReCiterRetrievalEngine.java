package reciter.xml.retriever.engine;

import java.util.List;

import reciter.database.mongo.model.Identity;

public interface ReCiterRetrievalEngine {

	List<Long> retrieve(Identity identity);

	List<Long> retrieveWithMultipleStrategies(Identity identity);
}
