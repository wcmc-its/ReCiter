package reciter.xml.retriever.engine;

import java.io.IOException;
import java.util.Set;

import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;

public interface ReCiterRetrievalEngine {

	void retrieve(TargetAuthor targetAuthor);
	
	Set<AuthorName> findUniqueAuthorsWithSameLastNameAsTargetAuthor(TargetAuthor targetAuthor);
}
