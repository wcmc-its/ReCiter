package reciter.xml.retriever.engine;

import java.util.List;
import java.util.Set;

import reciter.model.author.AuthorName;
import reciter.model.author.TargetAuthor;

public interface ReCiterRetrievalEngine {

	List<Long> retrieve(TargetAuthor targetAuthor);
	
	Set<AuthorName> findUniqueAuthorsWithSameLastNameAsTargetAuthor(TargetAuthor targetAuthor);
}
