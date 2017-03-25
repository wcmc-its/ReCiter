package reciter.algorithm.cluster.clusteringstrategy.article;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.citation.CitationStrategyContext;
import reciter.algorithm.evidence.article.citation.strategy.CitationStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class NameMatchingClusteringStrategy extends AbstractClusteringStrategy {
	
	private final Identity identity;
	
	private StrategyContext citationStrategyContext;
//	private StrategyContext coCitationStrategyContext;
	
	public NameMatchingClusteringStrategy(Identity identity) {
		citationStrategyContext = new CitationStrategyContext(new CitationStrategy());
//		coCitationStrategyContext = new CitationStrategyContext(new CoCitationStrategy());
		this.identity = identity;
	}
	
	public Map<Long, ReCiterCluster> cluster(List<ReCiterArticle> reCiterArticles, Set<Long> seedPmids) {
		
		// Reset ReCiterCluster's static id counter to 0, so that subsequent calls
		// to cluster method has ReCiterCluster id starts with 0.
		ReCiterCluster.getClusterIDCounter().set(0);
		Map<Long, ReCiterCluster> clusters = new HashMap<Long, ReCiterCluster>();
		ReCiterCluster firstCluster = new ReCiterCluster();
		clusters.put(firstCluster.getClusterID(), firstCluster);
		
		List<ReCiterArticle> remainingArticles = new ArrayList<>();
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			if (seedPmids.contains(reCiterArticle.getArticleId())) {
				firstCluster.add(reCiterArticle);
			} else {
				remainingArticles.add(reCiterArticle);
			}
		}
		
		for (ReCiterArticle article : remainingArticles) {
			boolean foundCluster = false;
			for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
		        ReCiterCluster reCiterCluster = entry.getValue();
		        for (ReCiterArticle reCiterArticle : reCiterCluster.getArticleCluster()) {

		          boolean isSimilar = isTargetAuthorNameAndJournalMatch(article, reCiterArticle);
		          double citationReferenceScore = ((ReCiterArticleStrategyContext) citationStrategyContext).executeStrategy(article, reCiterArticle);
		          
		          if (isSimilar || citationReferenceScore == 1) {
		            clusters.get(entry.getKey()).add(article);
		            foundCluster = true;
		            break;
		          }
		        }
		        if (foundCluster) break;
			}
			if (!foundCluster) {
				// create its own cluster.
				ReCiterCluster newReCiterCluster = new ReCiterCluster();
				newReCiterCluster.setClusterOriginator(article.getArticleId());
				newReCiterCluster.add(article);
				clusters.put(newReCiterCluster.getClusterID(), newReCiterCluster);
			}
		}
		return clusters;
	}
	
	/**
	 * Select the first article from the list. Iterate through the remaining
	 * articles and assign article based on target author name match.
	 */
	@Override
	public Map<Long, ReCiterCluster> cluster(List<ReCiterArticle> reCiterArticles) {
		
		// Reset ReCiterCluster's static id counter to 0, so that subsequent calls
		// to cluster method has ReCiterCluster id starts with 0.
		ReCiterCluster.getClusterIDCounter().set(0);
		
		Map<Long, ReCiterCluster> clusters = new HashMap<Long, ReCiterCluster>();
		boolean isFirstArticleSelected = false;
		
		ReCiterCluster firstCluster = new ReCiterCluster();
		
		for (ReCiterArticle article : reCiterArticles) {
			if (!isFirstArticleSelected) {
				// Select first article.
				ReCiterArticle firstArticle = null;
				if (reCiterArticles != null && reCiterArticles.size() > 0) {
					firstArticle = reCiterArticles.get(0);
				}  else {
					return clusters;
				}
				firstCluster.setClusterOriginator(firstArticle.getArticleId());
				firstCluster.add(firstArticle);
				clusters.put(firstCluster.getClusterID(), firstCluster);
				isFirstArticleSelected = true;
			} else {
				// Assign subsequent articles to a cluster.
				boolean foundCluster = false;
				for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
			        ReCiterCluster reCiterCluster = entry.getValue();
			        for (ReCiterArticle reCiterArticle : reCiterCluster.getArticleCluster()) {

			          boolean isSimilar = isTargetAuthorNameAndJournalMatch(article, reCiterArticle);
			          double citationReferenceScore = ((ReCiterArticleStrategyContext) citationStrategyContext).executeStrategy(article, reCiterArticle);
//			          double coCitationReferenceScore = ((ReCiterArticleStrategyContext) coCitationStrategyContext).executeStrategy(article, reCiterArticle);
			          
			          if (isSimilar || citationReferenceScore == 1) {
			            clusters.get(entry.getKey()).add(article);
			            foundCluster = true;
			            break;
			          }
			        }
			        if (foundCluster) break;
				}
				if (!foundCluster) {
					// create its own cluster.
					ReCiterCluster newReCiterCluster = new ReCiterCluster();
					newReCiterCluster.setClusterOriginator(article.getArticleId());
					newReCiterCluster.add(article);
					clusters.put(newReCiterCluster.getClusterID(), newReCiterCluster);
				}
			}
		}
		return clusters;
	}
	
	/**
	 * <p>
	 * First name matching in phase one clustering.
	 * <p>
	 * For more details, see https://github.com/wcmc-its/ReCiter/issues/59.
	 */
	private boolean isTargetAuthorNameMatch(ReCiterArticle newArticle, ReCiterArticle articleInCluster) {
		for (ReCiterAuthor reCiterAuthor : newArticle.getArticleCoAuthors().getAuthors()) {
			for (ReCiterAuthor clusterAuthor : articleInCluster.getArticleCoAuthors().getAuthors()) {
				if (reCiterAuthor.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName()) &&
						clusterAuthor.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName())) {

					// Check both first name and middle initial.
					if (reCiterAuthor.getAuthorName().getFirstName().equalsIgnoreCase(
							clusterAuthor.getAuthorName().getFirstName())
							&&
						reCiterAuthor.getAuthorName().getMiddleInitial().equalsIgnoreCase(
								clusterAuthor.getAuthorName().getMiddleInitial())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * <p>
	 * First name matching and journal matching in phase one clustering.
	 * <p>
	 * For more details, see https://github.com/wcmc-its/ReCiter/issues/59.
	 */
	private boolean isTargetAuthorNameAndJournalMatch(ReCiterArticle newArticle, ReCiterArticle articleInCluster) {
		// check two or more mutual co-authors (in addition to the target author).
		int numMatchingMutualCoAuthors = 0;
		boolean isTargetAuthorNameMatch = false;
		boolean isJournalNameMatch = false;
		
		for (ReCiterAuthor reCiterAuthor : newArticle.getArticleCoAuthors().getAuthors()) {
			for (ReCiterAuthor clusterAuthor : articleInCluster.getArticleCoAuthors().getAuthors()) {
				if (reCiterAuthor.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName()) &&
						clusterAuthor.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName())) {

					// Check both first name and middle initial.
					if (reCiterAuthor.getAuthorName().getFirstName().equalsIgnoreCase(clusterAuthor.getAuthorName().getFirstName())
							&&
						reCiterAuthor.getAuthorName().getMiddleInitial().equalsIgnoreCase(clusterAuthor.getAuthorName().getMiddleInitial())) {
						
						isTargetAuthorNameMatch = true;
						
						// check journal.
						if (newArticle.getJournal() != null && articleInCluster.getJournal() != null) {
							String newArticleJournal = newArticle.getJournal().getJournalTitle();
							String articleInClusterJournal = articleInCluster.getJournal().getJournalTitle();
							
							if (newArticleJournal.length() > 0 && articleInClusterJournal.length() > 0 && 
									StringUtils.equalsIgnoreCase(newArticleJournal, articleInClusterJournal)) {
								
								isJournalNameMatch = true;
							}
						}
						
						
					}
				} else {
					// Check both first name and middle initial.
					if (reCiterAuthor.getAuthorName().getFirstName().equalsIgnoreCase(clusterAuthor.getAuthorName().getFirstName())
							&&
						reCiterAuthor.getAuthorName().getMiddleInitial().equalsIgnoreCase(clusterAuthor.getAuthorName().getMiddleInitial())) {
						
						numMatchingMutualCoAuthors++;
					}
				}
			}
		}
		
		if (isTargetAuthorNameMatch && (isJournalNameMatch)) {
			return true;
		}
		return false;
	}
	
	private boolean splitSingle(ReCiterArticle newArticle, ReCiterArticle articleInCluster) {
		return false;
	}

	public Identity getIdentity() {
		return identity;
	}

}
