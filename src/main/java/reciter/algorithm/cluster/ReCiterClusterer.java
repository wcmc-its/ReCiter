package reciter.algorithm.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.clusteringstrategy.article.ClusteringStrategy;
import reciter.algorithm.cluster.clusteringstrategy.article.NameMatchingClusteringStrategy;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class ReCiterClusterer extends AbstractClusterer {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);
	
	private List<ReCiterArticle> reCiterArticles;
	private TargetAuthor targetAuthor;
	private Map<Integer, ReCiterCluster> clusters;
	private ClusteringStrategy clusteringStrategy;
	
	public ReCiterClusterer(TargetAuthor targetAuthor, List<ReCiterArticle> reCiterArticles) {
		this.reCiterArticles = reCiterArticles;
		this.targetAuthor = targetAuthor;
		clusters = new HashMap<Integer, ReCiterCluster>();
		clusteringStrategy = new NameMatchingClusteringStrategy(targetAuthor);
	}

	/**
	 * <p>
	 * Phase I clustering.
	 * <p>
	 * Cluster articles together based on the ClusteringStrategy selected.
	 */
	@Override
	public void cluster() {
		slf4jLogger.info("Running ReCiter for: [" + targetAuthor.getCwid() + "] "
				+ "Number of articles to be clustered:" + reCiterArticles.size());
		clusters = clusteringStrategy.cluster(reCiterArticles);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<Integer, ReCiterCluster> cluster : clusters.entrySet()) {
			sb.append("\nCluster id: " + cluster.getKey() + "= ," + cluster.getValue().getClusterInfo());
			for (ReCiterArticle reCiterArticle : cluster.getValue().getArticleCluster()) {
				sb.append(reCiterArticle.getArticleId() + ", ");
			}
		}
		return sb.toString();
	}

	public Map<Integer, ReCiterCluster> getClusters() {
		return clusters;
	}

	public List<ReCiterArticle> getReCiterArticles() {
		return reCiterArticles;
	}

	public void setReCiterArticles(List<ReCiterArticle> reCiterArticles) {
		this.reCiterArticles = reCiterArticles;
	}

	public TargetAuthor getTargetAuthor() {
		return targetAuthor;
	}

	public void setTargetAuthor(TargetAuthor targetAuthor) {
		this.targetAuthor = targetAuthor;
	}
}
