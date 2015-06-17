package reciter.algorithm.cluster.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.model.article.ReCiterArticle;

public class ReCiterClusterGroup {

	public static void main(String[] args) {
		ReCiterClusterGroup c = new ReCiterClusterGroup();
		System.out.println(c.getClusterer().getArticleToArticleSimilarityThresholdValue());
	}
	private Map<Integer, ReCiterCluster> finalCluster;
	
	@Autowired
	private Clusterer clusterer;
	
	public ReCiterClusterGroup() {
		finalCluster = new HashMap<Integer, ReCiterCluster>();
		setClusterer(new ReCiterClusterer());
	}

	public Map<Integer, ReCiterCluster> cluster(List<ReCiterArticle> articleList) {
		return null;
	}
	
	public Map<Integer, ReCiterCluster> getFinalCluster() {
		return finalCluster;
	}

	public void setFinalCluster(Map<Integer, ReCiterCluster> finalCluster) {
		this.finalCluster = finalCluster;
	}

	public Clusterer getClusterer() {
		return clusterer;
	}

	public void setClusterer(Clusterer clusterer) {
		this.clusterer = clusterer;
	}
}
