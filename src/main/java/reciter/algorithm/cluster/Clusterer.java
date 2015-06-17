package reciter.algorithm.cluster;

import java.util.List;

import reciter.model.article.ReCiterArticle;

/**
 * Clusterer interface. Provides a set of functions that must be implemented
 * by any clustering class that implements this interface.
 * 
 * @author jil3004
 *
 */
public interface Clusterer {
	
	double getArticleToArticleSimilarityThresholdValue();
	
	/**
	 * Performs a clustering on the list of ReCiterArticles.
	 * @param reciterArticleList
	 */
	void cluster(List<ReCiterArticle> reciterArticleList);
}
