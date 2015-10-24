package reciter.algorithm.cluster.article;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.clusteringstrategy.article.ClusteringStrategy;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;
import reciter.service.impl.TargetAuthorServiceImpl;

public class ReCiterClusterer extends AbstractClusterer {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);
	
	private Map<Integer, ReCiterCluster> finalCluster;
	private TargetAuthor targetAuthor;
	private Set<Integer> selectedClusterIds;
	
	private TargetAuthorStrategyContext boardCertificationStrategyContext;
	private TargetAuthorStrategyContext emailStrategyContext;
	private TargetAuthorStrategyContext scopusStrategyContext;
	private TargetAuthorStrategyContext degreeStrategyContext;
	private TargetAuthorStrategyContext departmentStringMatchStrategyContext;
	private TargetAuthorStrategyContext grantCoauthorStrategyContext;
	
	private ClusteringStrategy clusteringStrategy;
	
	public ReCiterClusterer(String cwid) {
		finalCluster = new HashMap<Integer, ReCiterCluster>();
		selectedClusterIds = new HashSet<Integer>();
		targetAuthor = new TargetAuthorServiceImpl().getTargetAuthor(cwid);
	}

	/**
	 * <p>
	 * Phase I clustering.
	 * <p>
	 * Cluster articles together based on the ClusteringStrategy selected.
	 */
	@Override
	public void cluster(List<ReCiterArticle> reCiterArticles) {
		slf4jLogger.info("Number of articles to be clustered:" + reCiterArticles.size());
		finalCluster = clusteringStrategy.cluster(reCiterArticles);
	}
	
	
	public void selectClusters() {
		
	}
	
	public void selectIndividualReCiterArticles() {
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<Integer, ReCiterCluster> cluster : finalCluster.entrySet()) {
			sb.append("\nCluster id: " + cluster.getKey() + "= ");
			for (ReCiterArticle reCiterArticle : cluster.getValue().getArticleCluster()) {
				sb.append(reCiterArticle.getArticleId() + ", ");
			}
		}
		return sb.toString();
	}

	public Map<Integer, ReCiterCluster> getFinalCluster() {
		return finalCluster;
	}

	public TargetAuthor getTargetAuthor() {
		return targetAuthor;
	}

	public Set<Integer> getSelectedClusterIds() {
		return selectedClusterIds;
	}
}
