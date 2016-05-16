package reciter.xml.retriever.engine;

import reciter.model.author.TargetAuthor;

public interface RetrievalSession {

	/**
	 * How to set levels of strictness of search for each person.
	 */
	void setLevel();
	
	/**
	 * Limit by year. From target author.
	 */
	
	
	void retrieve(TargetAuthor targetAuthor);
}
