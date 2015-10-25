package reciter.algorithm.cluster;

import java.util.List;
import java.util.Map;

import reciter.algorithm.cluster.model.ReCiterCluster;
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
	 * Performs a clustering on the list of ReCiterArticles and return the
	 * clustering result.
	 * 
	 * @param reciterArticles ReCiter articles to be clustered.
	 */
	Map<Integer, ReCiterCluster> cluster(List<ReCiterArticle> reCiterArticles);

}
