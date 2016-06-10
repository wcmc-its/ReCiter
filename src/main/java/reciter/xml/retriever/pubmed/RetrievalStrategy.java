package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.util.List;

import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;

public interface RetrievalStrategy {

	/**
	 * Retrieve the articles for this author.
	 * 
	 * @param targetAuthor
	 * 
	 * @return List of PubMed articles for this author.
	 * 
	 * @throws IOException
	 */
	List<PubMedArticle> retrieve(TargetAuthor targetAuthor) throws IOException;
	
	/**
	 * Set to <code>true</code> if the retrieval strategy should retrieve the articles even if the number of articles
	 * exceeds the threshold.
	 * @param isRetrieveExceedThreshold
	 */
	void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold);
}
