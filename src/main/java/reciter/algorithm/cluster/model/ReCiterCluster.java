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
	 * Cluster Id.
	 */
	private final long clusterId;
	
	/**
	 * Atomic integer counter
	 */
	private static AtomicInteger clusterIDCounter = new AtomicInteger(0);

	/**
	 * List of articles in its cluster.
	 */
	private List<ReCiterArticle> articleCluster;

	/**
	 * Cluster originator.
	 */
	private long clusterOriginator;
	
	/**
	 * Logger.
	 */
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterCluster.class);	

	private String clusterInfo = "";
	
	/**
	 * Returns a list of pmids of articles in this cluster.
	 */
	public Set<Long> getPmidSet() {
		Set<Long> pmidSet = new HashSet<Long>();
		for (ReCiterArticle reCiterArticle : articleCluster) {
			pmidSet.add(reCiterArticle.getArticleId());
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
//			double sim = article.getDocumentSimmilarity().documentSimilarity(article, currentArticle);
			double sim = 0;
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
//		for (ReCiterArticle article : articleCluster) {
//			for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
//				if (targetAuthor.getAuthorName().isNameVariant(author.getAuthorName())) {
//					return true;
//				}
//			}
//		}
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
	public int getMatchingCoauthorCount(ReCiterArticle currentArticle, TargetAuthor targetAuthor) {
		int matchingCoauthorCount = 0;
		// For each article in this cluster.
		for (ReCiterArticle article : articleCluster) {
			// For each author in this article.
			for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
				// For each author in the currentArticle.
				for (ReCiterAuthor currentAuthor : currentArticle.getArticleCoAuthors().getAuthors()) {
					
					// Check if the names match.
					if ((currentAuthor.getAuthorName().isFullNameMatch(author.getAuthorName()) 
							&& !currentAuthor.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName())
							&& !author.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName()))) {
						
						matchingCoauthorCount += 1;
						
					} else if (currentAuthor.getAffiliation() != null && currentAuthor.getAffiliation().getAffiliationName() != null
							&& author.getAffiliation() != null && author.getAffiliation().getAffiliationName() != null) {
						
						if (currentAuthor.getAuthorName().firstInitialLastNameMatch(author.getAuthorName()) 
							) {
							
//							slf4jLogger.debug(currentAuthor.getAuthorName() + " " + author.getAuthorName());
								matchingCoauthorCount += 1;
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
		clusterId = clusterIDCounter.incrementAndGet();
		articleCluster = new ArrayList<ReCiterArticle>();
	}

	public long getClusterID() {
		return clusterId;
	}
	
	public static AtomicInteger getClusterIDCounter() {
		return clusterIDCounter;
	}

	public long getClusterOriginator() {
		return clusterOriginator;
	}

	public void setClusterOriginator(long clusterOriginator) {
		this.clusterOriginator = clusterOriginator;
	}

	public String getClusterInfo() {
		return clusterInfo;
	}

	public void setClusterInfo(String clusterInfo) {
		this.clusterInfo = clusterInfo;
	}
}
