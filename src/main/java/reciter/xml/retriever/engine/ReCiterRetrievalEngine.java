package reciter.xml.retriever.engine;

import java.io.IOException;
import java.util.Set;

import reciter.database.mongo.model.Identity;

public interface ReCiterRetrievalEngine {

	Set<Long> retrieve(Identity identity) throws IOException;
}
