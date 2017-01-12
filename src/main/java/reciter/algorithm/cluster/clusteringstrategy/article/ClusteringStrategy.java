package reciter.algorithm.cluster.clusteringstrategy.article;

import java.util.List;
import java.util.Map;
import java.util.Set;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;

public interface ClusteringStrategy {

	Map<Long, ReCiterCluster> cluster(List<ReCiterArticle> reCiterArticles);
	
	Map<Long, ReCiterCluster> cluster(List<ReCiterArticle> reCiterArticles, Set<Long> seedPmids);
}
