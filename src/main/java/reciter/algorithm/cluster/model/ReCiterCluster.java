package reciter.algorithm.cluster.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;

public class ReCiterCluster {

	/**
	 * Cluster ID.
	 */
	private final int clusterID;
	
	/**
	 * Atomic integer counter
	 */
	private static AtomicInteger clusterIDCounter = new AtomicInteger(0);
	
	public static AtomicInteger getClusterIDCounter() {
		return clusterIDCounter;
	}

	/**
	 * List of articles in its cluster.
	 */
	private List<ReCiterArticle> articleCluster;

	/**
	 * Debug boolean.
	 */
	private boolean debug = false;

	/**
	 * Logger.
	 */
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterCluster.class);	

	/**
	 * Returns a list of pmids of articles in this cluster.
	 */
	public Set<Integer> getPmidSet() {
		Set<Integer> pmidSet = new HashSet<Integer>();
		for (ReCiterArticle reCiterArticle : articleCluster) {
			boolean addedToSet = pmidSet.add(reCiterArticle.getArticleID());
			if (debug) {
				if (!addedToSet) {
					slf4jLogger.info("Duplicate Pmid: " + reCiterArticle.getArticleID());
				}
			}
		}
		return pmidSet;
	}

	/**
	 * Calculates the similarity of this cluster with another cluster.
	 * @param c
	 * @return
	 */
	public double similar(ReCiterCluster c) {
		double maxSim = -1;
		for (ReCiterArticle article : c.getArticleCluster()) {
			double sim = contentSimilarity(article);
			if (sim > maxSim) {
				maxSim = sim;
			}
		}
		return maxSim;
	}

	/**
	 * Calculates the similarity between an ReCiterArticle and a ReCiterCluster.
	 * @param currentArticle
	 * @return
	 */
	public double contentSimilarity(ReCiterArticle currentArticle) {
		double similarityScore = -1;
		for (ReCiterArticle article : articleCluster) {
			double sim = article.getDocumentSimmilarity().documentSimilarity(article, currentArticle);
			if (sim > similarityScore) {
				similarityScore = sim;
			}
		}
		return similarityScore;
	}

	/**
	 * Checks whether this cluster contains an author who has a name variant
	 * to the target author.
	 * @param type
	 * @param initials
	 * @return
	 */
	public boolean containsNameVariant(ReCiterAuthor targetAuthor) {
		for (ReCiterArticle article : articleCluster) {
			for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
				if (targetAuthor.getAuthorName().isNameVariant(author.getAuthorName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get number of matching coauthors except the target author for each of the articles in this cluster 
	 * with the <code>currentArticle</code> and return the max number of matching coauthors out of the articles in this cluster.
	 * @param currentArticle current article being compared.
	 * @return the max number of matching coauthors.
	 * 
	 * TODO revise the if statement logic.
	 */
	public int getMatchingCoauthorCount(ReCiterArticle currentArticle) {
		int matchingCoauthorCount = 0;
		// For each article in this cluster.
		for (ReCiterArticle article : articleCluster) {
			// For each author in this article.
			for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
				// For each author in the currentArticle.
				for (ReCiterAuthor currentAuthor : currentArticle.getArticleCoAuthors().getCoAuthors()) {
					// Check if the names match.
					if (currentAuthor.getAuthorName().isFullNameMatch(author.getAuthorName()) 
							&& !currentAuthor.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())
							&& !author.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
						
						matchingCoauthorCount += 1;
						if (debug) {
							slf4jLogger.info("PMID: " + currentArticle.getArticleID() + " " + article.getArticleID() + " " + currentAuthor.getAuthorName());
						}
					}
				}
			}
		}
		return matchingCoauthorCount;
	}

	public void add(ReCiterArticle article) {
		articleCluster.add(article);
	}

	public void addAll(ReCiterCluster cluster) {
		articleCluster.addAll(cluster.getArticleCluster());
	}

	public List<ReCiterArticle> getArticleCluster() {
		return articleCluster;
	}

	public void setArticleCluster(List<ReCiterArticle> articleCluster) {
		this.articleCluster = articleCluster;
	}

	public ReCiterCluster() {
		clusterID = clusterIDCounter.incrementAndGet();
		articleCluster = new ArrayList<ReCiterArticle>();
	}

	public int getClusterID() {
		return clusterID;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(getClusterID());
		sb.append(" (size of cluster=");
		sb.append(getArticleCluster().size());
		sb.append("): ");
		for (ReCiterArticle a : getArticleCluster()) {
			sb.append(a.getArticleID());
			sb.append(", ");
		}
		sb.append("}\n");
		return sb.toString();
	}
}
