package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.util.List;

import reciter.model.author.TargetAuthor;
import reciter.model.pubmed.PubMedArticle;

public interface RetrievalStrategy {

	/**
	 * Set to <code>true</code> if the retrieval strategy should retrieve the articles even if the number of articles
	 * exceeds the threshold.
	 * @param isRetrieveExceedThreshold
	 */
	void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold);

	void constructPubMedQuery(TargetAuthor targetAuthor) throws IOException;
	
	String getPubMedQuery();
	void setPubMedQuery(String pubMedQuery);
	int getNumberOfPubmedArticles();
	void setNumberOfPubmedArticles(int numberOfPubmedArticles);
	int getThreshold();
	void setThreshold(int threshold);
	
	/**
	 * Retrieve the articles for this author.
	 * 
	 * @param targetAuthor
	 * 
	 * @return List of PubMed articles for this author.
	 * 
	 */
	List<PubMedArticle> retrieve();

	String[] retrievePmids(String query) throws IOException;
}
