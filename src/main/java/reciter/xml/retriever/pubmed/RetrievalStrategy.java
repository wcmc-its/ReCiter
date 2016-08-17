package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.util.List;

import reciter.database.mongo.model.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.xml.parser.scopus.model.ScopusArticle;

public interface RetrievalStrategy {

	/**
	 * Set to <code>true</code> if the retrieval strategy should retrieve the articles even if the number of articles
	 * exceeds the threshold.
	 * @param isRetrieveExceedThreshold
	 */
	void setRetrieveExceedThreshold(boolean isRetrieveExceedThreshold);

	void constructPubMedQuery(Identity identity) throws IOException;
	
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
	
	String getRetrievalStrategyName();

	List<ScopusArticle> retrieveScopus(List<Long> pmids);
}
