package reciter.algorithm.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.clusteringstrategy.article.ClusteringStrategy;
import reciter.algorithm.cluster.clusteringstrategy.article.NameMatchingClusteringStrategy;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public class ReCiterClusterer extends AbstractClusterer {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);
	
	private List<ReCiterArticle> reCiterArticles;
	private Identity identity;
	private Map<Long, ReCiterCluster> clusters;
	private ClusteringStrategy clusteringStrategy;
	
	public ReCiterClusterer(Identity identity, List<ReCiterArticle> reCiterArticles) {
		this.reCiterArticles = reCiterArticles;
		this.identity = identity;
		clusters = new HashMap<Long, ReCiterCluster>();
		clusteringStrategy = new NameMatchingClusteringStrategy(identity);
	}

	/**
	 * <p>
	 * Phase I clustering.
	 * <p>
	 * Cluster articles together based on the ClusteringStrategy selected.
	 */
	@Override
	public void cluster() {
		slf4jLogger.info("Running ReCiter for: [" + identity.getCwid() + "] "
				+ "Number of articles to be clustered:" + reCiterArticles.size());
		clusters = clusteringStrategy.cluster(reCiterArticles);
	}
	

	@Override
	public void cluster(Set<Long> seedPmids) {
		slf4jLogger.info("Running ReCiter for: [" + identity.getCwid() + "] "
				+ "Number of articles to be clustered:" + reCiterArticles.size() + " initial seeds=" + seedPmids);
		clusters = clusteringStrategy.cluster(reCiterArticles, seedPmids);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<Long, ReCiterCluster> cluster : clusters.entrySet()) {
			sb.append("\nCluster id: " + cluster.getKey() + "= ," + cluster.getValue().getClusterInfo());
			for (ReCiterArticle reCiterArticle : cluster.getValue().getArticleCluster()) {
				sb.append(reCiterArticle.getArticleId() + ", ");
			}
		}
		return sb.toString();
	}

	@Override
	public Map<Long, ReCiterCluster> getClusters() {
		return clusters;
	}

	public List<ReCiterArticle> getReCiterArticles() {
		return reCiterArticles;
	}

	public void setReCiterArticles(List<ReCiterArticle> reCiterArticles) {
		this.reCiterArticles = reCiterArticles;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
}
