package reciter.algorithm.cluster.clusteringstrategy.article;

import java.util.List;
import java.util.Map;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;

public interface ClusteringStrategy {

	Map<Integer, ReCiterCluster> cluster(List<ReCiterArticle> reCiterArticles);
}
