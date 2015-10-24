package reciter.algorithm.cluster.article;

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
	
	/**
	 * Performs a clustering on the list of ReCiterArticles.
	 * @param reciterArticleList
	 */
	void cluster(List<ReCiterArticle> reCiterArticleList);
	
	/**
	 * Computes the similarity between a {@code ReCiterCluster} and a {@code TargetAuthor}.
	 * 
	 * @param reCiterCluster reCiterCluster being compared.
	 * @param targetAuthor targetAuthor being compared.
	 * 
	 * @return A similarity score that shows how similar the {@code ReCiterCluster} is to the
	 * {@code TargetAuthor}
	 * 
	 */
//	double computeClusterTargetSimilarity(ReCiterCluster reCiterCluster, TargetAuthor targetAuthor);
}
