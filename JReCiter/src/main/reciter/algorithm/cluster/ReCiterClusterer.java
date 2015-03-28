package main.reciter.algorithm.cluster;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.reciter.algorithm.cluster.model.ReCiterCluster;
import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.Analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author jil3004
 *
 */
public class ReCiterClusterer {
	
	/**
	 * Debug boolean.
	 */
	private boolean debug = false;
	
	/**
	 * Logger.
	 */
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);	
	
	/**
	 * A map that contains the id to ReCiterCluster object.
	 */
	private Map<Integer, ReCiterCluster> finalCluster;
	
	/**
	 * List of articles to be clustered.
	 */
	private List<ReCiterArticle> articleList;
	
	/**
	 * Boolean containing the value of whether it's currently selecting the final target person.
	 */
	boolean selectingTarget = false;

	/**
	 * ReCiterArticle to ReCiterCluster similarity threshold value.
	 */
	public double similarityThreshold = 0.1; // 
	
	/**
	 * ReCiterArticle (target person) to ReCiterClusters in the 
	 * finalCluster similarity threshold value.
	 */
	public double targetAuthorSimilarityThreshold = 0.001; // target to cluster similarity threshold value.
	
	/**
	 * Hierarchical Agglomerative Clustering:
	 * cluster to cluster similarity threshold value.
	 */
	double hacSimilarityThreshold = 0.7; // hierarchical agglomerative clustering threshold similarity.
	
	/**
	 * Constructor initializing the finalCluster map data structure.
	 */
	public ReCiterClusterer() {
		slf4jLogger.info("Initializing ReCiterClusterer...\nParameter Settings:\n"
				+ "Cluster to article similarity threshold: " + similarityThreshold + "\n"
						+ "Final Cluster list to target author similarity threshold: " + targetAuthorSimilarityThreshold);
		finalCluster = new HashMap<Integer, ReCiterCluster>();
	}
	
	public void cluster(double similarityThreshold, double incrementer) {
		for (; similarityThreshold < 1.0; similarityThreshold += incrementer) {
			this.similarityThreshold = similarityThreshold;
			cluster();
			for (ReCiterCluster reCiterClusterer : finalCluster.values()) {
				
			}
		}
	}

	/**
	 * Getter method to retrieve the finalCluster map data structure.
	 * @return
	 */
	public Map<Integer, ReCiterCluster> getFinalCluster() {
		return finalCluster;
	}

	/**
	 * 
	 * @param article
	 * @param targetAuthor
	 * @return
	 */
	public int assignTargetToCluster(ReCiterArticle article, ReCiterAuthor targetAuthor) {
		selectingTarget = true;
		return selectCandidateCluster(article);
	}
	
	/**
	 * Hierarchical Agglomerative Clustering.
	 * TODO place this function in another class.
	 * @param articleList
	 */
	public void hacCluster(List<ReCiterArticle> articleList) {
		for (ReCiterArticle article : articleList) {
			ReCiterCluster r = new ReCiterCluster();
			r.add(article);
			finalCluster.put(r.getClusterID(), r);
		}
		double currentMaxSimilarity = Integer.MAX_VALUE;
		while (currentMaxSimilarity > hacSimilarityThreshold && finalCluster.size() > 0) {
			double currentMaxSim = -1;
			int clusterId1 = -1;
			int clusterId2 = -1;
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				for (Entry<Integer, ReCiterCluster> entryInner : finalCluster.entrySet()) {
					if (entry.getKey() != entryInner.getKey()) {
						double sim = entry.getValue().similar(entryInner.getValue());
						if (sim > currentMaxSim) {
							currentMaxSim = sim;
							clusterId1 = entry.getKey();
							clusterId2 = entryInner.getKey();
						}
					}
				}
			}
			if (clusterId1 != -1 && clusterId2 != -1) {
				finalCluster.get(clusterId1).addAll(finalCluster.get(clusterId2)); // add all articles from cluster 2 to 1.
				finalCluster.remove(clusterId2);
				currentMaxSimilarity = currentMaxSim;
			}
		}
	}
	
	/**
	 * 
	 * @param articleList
	 * @param targetAuthor
	 */
	public void cluster() {
		slf4jLogger.info("Number of articles to be clustered: " + articleList.size());
		ReCiterArticle first = articleList.get(0);
		slf4jLogger.info("First article: " + first);
		ReCiterCluster firstCluster = new ReCiterCluster();
		firstCluster.add(first);
		finalCluster.put(firstCluster.getClusterID(), firstCluster);
		for (int i = 1; i < articleList.size(); i++) {
			ReCiterArticle article = articleList.get(i);
			slf4jLogger.info("Assigning article id " + article.getArticleID());
			int selection = selectCandidateCluster(article);
			if (selection == -1) {
				
//				System.out.println(article.getArticleId() + " - forming its own cluster.");
				// create its own cluster.
				ReCiterCluster newCluster = new ReCiterCluster();
				newCluster.add(article);
				finalCluster.put(newCluster.getClusterID(), newCluster);
			} else {
				finalCluster.get(selection).add(article);
			}
		}
	}

	/**
	 * Select the candidate cluster.
	 * @param currentArticle
	 * @param targetAuthor
	 * @return
	 */
	public int selectCandidateCluster(ReCiterArticle currentArticle) {
		
		if (debug) {
			slf4jLogger.info("Selecting candidate clusters...");
		}
		// Get cluster ids with max number of coauthor matches.
		Set<Integer> clusterIdSet = getKeysWithMaxVal(computeCoauthorMatch(currentArticle));
		
		// If groups have matching co-authors, the program selects the group that has the most matching names.
		if (clusterIdSet.size() == 1) {
			for (int id : clusterIdSet) {
				if (selectingTarget) {
					if (debug) {
						slf4jLogger.info("(Target to Cluster) Selecting the group with most matching coauthors: " + id);
					}
				}
				slf4jLogger.info("Selecting cluster " + id + " as candidate cluster based on most matching names.");
				return id;
			}
		}
		
		// If two or more of these have the same number of coauthors, the one with the highest matching score is selected.
		if (clusterIdSet.size() > 1) {
			if (selectingTarget) {
				if (debug) {
					slf4jLogger.info("(Target to Cluster) Selecting the group with most content similarity");
				}
			}
			return getIdWithMostContentSimilarity(clusterIdSet, currentArticle);
		}
		
		// If groups have no matching co-authors, the group with the highest 
		// matching (cosine) score is selected, provided that the score
		// exceeds a given threshold.
		return getIdWithMostContentSimilarity(finalCluster.keySet(), currentArticle);
	}
	
	/**
	 * 
	 * @param clusterIdList
	 * @param currentArticle
	 * @return
	 */
	private int getIdWithMostContentSimilarity(Set<Integer> clusterIdList, ReCiterArticle currentArticle) {
		double currentMax = -1;
		int currentMaxId = -1;
		for (int id : clusterIdList) {
			double sim = finalCluster.get(id).contentSimilarity(currentArticle); // cosine similarity score.
			if (selectingTarget) {
				if (debug) {
					slf4jLogger.info("Cosine similarity between target and cluster " + id + ": score=" + sim);
				}
				
				if (sim > targetAuthorSimilarityThreshold && sim > currentMax) {
					currentMaxId = id;
					currentMax = sim;
//					System.out.println("Calculating cosine similarity in ReCiterClusterer.java " + id + ": " + sim);
				}
			} else if (sim > similarityThreshold && sim > currentMax) {
				if (debug) {
					slf4jLogger.info("Similarity article " + currentArticle.getArticleID() + " to cluster: " + id + " score: " + sim);
					slf4jLogger.info(finalCluster.get(id).toString());
				}
				currentMaxId = id;
				currentMax = sim;
				// TODO: what happens if cosine similarity is tied?
			}
		}
		return currentMaxId; // found a cluster.
	}
	
	/**
	 * 
	 * @param currentArticle
	 * @param targetAuthor
	 * @return
	 */
	// computes coauthor matches of this article with all current clusters.
	private Map<Integer, Integer> computeCoauthorMatch(ReCiterArticle currentArticle) {
		Map<Integer, Integer> coauthorsCount = new HashMap<Integer, Integer>(); // ClusterId to number of coauthors.
		for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
			int clusterId = entry.getKey();
			ReCiterCluster reCiterCluster = entry.getValue();
			int matchingCoauthors = reCiterCluster.getMatchingCoauthorCount(currentArticle);
			coauthorsCount.put(clusterId, matchingCoauthors);
		}
		return coauthorsCount;
	}
	
	/**
	 * 
	 * @param map
	 * @return
	 */
	// helper function to find keys in map data structure with max values.
	private Set<Integer> getKeysWithMaxVal(Map<Integer, Integer> map) {
		Set<Integer> keyList = new HashSet<Integer>();
		int maxValueInMap=(Collections.max(map.values()));  // This will return max value in the Hashmap
//		System.out.println("Max value: " + maxValueInMap);
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() == maxValueInMap && maxValueInMap != 0) {
            	keyList.add(entry.getKey());
            }
        }
        return keyList;
	}
	
	
	public List<ReCiterArticle> getArticleList() {
		return articleList;
	}

	public void setArticleList(List<ReCiterArticle> articleList) {
		this.articleList = articleList;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Number of clusters formed: " + getFinalCluster().size() + "\n");
		
		for (ReCiterCluster r : getFinalCluster().values()) {
			sb.append("{");
			sb.append(r.getClusterID());
			sb.append(" (size of cluster=");
			sb.append(r.getArticleCluster().size());
			sb.append("): ");
			for (ReCiterArticle a : r.getArticleCluster()) {
				sb.append(a.getArticleID());
				sb.append(", ");
			}
			sb.append("}\n");
		}
		return sb.toString();
	}

}
