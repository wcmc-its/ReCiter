package reciter.algorithm.cluster;

import java.util.List;
import java.util.Map;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;

/**
 * Clusterer interface. Provides a set of functions that must be implemented
 * by any clustering class that implements this interface.
 * 
 * @author jil3004
 *
 */
public interface Clusterer {
	
	void cluster();
	
	Identity getIdentity();
	
	List<ReCiterArticle> getReCiterArticles();
	
	Map<Long, ReCiterCluster> getClusters();
}
