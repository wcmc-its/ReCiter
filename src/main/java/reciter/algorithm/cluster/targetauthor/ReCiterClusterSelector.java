package reciter.algorithm.cluster.targetauthor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.coauthor.CoauthorStrategyContext;
import reciter.algorithm.evidence.article.coauthor.strategy.CoauthorStrategy;
import reciter.algorithm.evidence.article.journal.JournalStrategyContext;
import reciter.algorithm.evidence.article.journal.strategy.JournalStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.WeillCornellAffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.citizenship.CitizenshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.citizenship.strategy.CitizenshipStrategy;
import reciter.algorithm.evidence.targetauthor.degree.DegreeStrategyContext;
import reciter.algorithm.evidence.targetauthor.degree.strategy.DegreeType;
import reciter.algorithm.evidence.targetauthor.degree.strategy.YearDiscrepancyStrategy;
import reciter.algorithm.evidence.targetauthor.department.DepartmentStrategyContext;
import reciter.algorithm.evidence.targetauthor.department.strategy.DepartmentStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.email.EmailStrategyContext;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.grant.GrantStrategyContext;
import reciter.algorithm.evidence.targetauthor.grant.strategy.KnownCoinvestigatorStrategy;
import reciter.algorithm.evidence.targetauthor.scopus.ScopusStrategyContext;
import reciter.algorithm.evidence.targetauthor.scopus.strategy.StringMatchingAffiliation;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class ReCiterClusterSelector extends AbstractClusterSelector {

	/** Cluster selection strategy contexts. */
	
	/**
	 * Email Strategy.
	 */
	private StrategyContext emailStrategyContext;
	
	/**
	 * Department Strategy.
	 */
	private StrategyContext departmentStringMatchStrategyContext;
	
	/**
	 * Known co-investigator strategy context.
	 */
	private StrategyContext grantCoauthorStrategyContext;
	
	/**
	 * Affiliation strategy context.
	 */
	private StrategyContext affiliationStrategyContext;

	/** Individual article selection strategy contexts. */
	/**
	 * Scopus strategy context.
	 */
	private StrategyContext scopusStrategyContext;
	
	/**
	 * Coauthor strategy context.
	 */
	private StrategyContext coauthorStrategyContext;
	
	/**
	 * Journal strategy context.
	 */
	private StrategyContext journalStrategyContext;
	
	/**
	 * Citizenship strategy context.
	 */
	private StrategyContext citizenshipStrategyContext;
	
	/**
	 * Year Discrepancy (Bachelors).
	 */
	private StrategyContext bachelorsYearDiscrepancyStrategyContext;
	
	/**
	 * Year Discrepancy (Doctoral).
	 */
	private StrategyContext doctoralYearDiscrepancyStrategyContext;
	
//	private StrategyContext boardCertificationStrategyContext;
//
//	private StrategyContext degreeStrategyContext;
//	
	private List<StrategyContext> strategyContexts;

	private Set<Integer> selectedClusterIds;

	public ReCiterClusterSelector(TargetAuthor targetAuthor) {
		
		// Strategies that select clusters that are similar to the target author.
		emailStrategyContext = new EmailStrategyContext(new EmailStringMatchStrategy());
		departmentStringMatchStrategyContext = new DepartmentStrategyContext(new DepartmentStringMatchStrategy());
		grantCoauthorStrategyContext = new GrantStrategyContext(new KnownCoinvestigatorStrategy());
		affiliationStrategyContext = new AffiliationStrategyContext(new WeillCornellAffiliationStrategy());
		
		// Using the following strategy contexts in sequence to reassign individual articles
		// to selected clusters.
		scopusStrategyContext = new ScopusStrategyContext(new StringMatchingAffiliation());
		coauthorStrategyContext = new CoauthorStrategyContext(new CoauthorStrategy(targetAuthor));
		journalStrategyContext = new JournalStrategyContext(new JournalStrategy(targetAuthor));
		citizenshipStrategyContext = new CitizenshipStrategyContext(new CitizenshipStrategy());
		
		// TODO: reAssignArticlesByPubmedAffiliationCosineSimilarity(map);
		// TODO: getBoardCertificationScore(map);
		
		// TODO: removeArticlesBasedOnYearDiscrepancy(map);
		bachelorsYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.BACHELORS));
		doctoralYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.DOCTORAL));
		
		strategyContexts = new ArrayList<StrategyContext>();
		strategyContexts.add(scopusStrategyContext);
		strategyContexts.add(coauthorStrategyContext);
		strategyContexts.add(journalStrategyContext);
		strategyContexts.add(citizenshipStrategyContext);
	}

	@Override
	public void runSelectionStrategy(Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {
		// Select clusters that are similar to the target author.
		selectClusters(clusters, targetAuthor);

		// Reassign individual article that are similar to the target author. 
		reAssignArticles(strategyContexts, clusters, targetAuthor);
	}

	/**
	 * Selecting clusters based on evidence types.
	 * 
	 * @param clusters Clusters formed in Phase I clustering.
	 * @param targetAuthor Target author.
	 * 
	 * @return A set of cluster ids that are selected because of the cluster's
	 * similarity to target author.
	 */
	public void selectClusters(Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {

		Set<Integer> selectedClusterIds = new HashSet<Integer>();
		for (Entry<Integer, ReCiterCluster> entry : clusters.entrySet()) {
			int clusterId = entry.getKey();
			List<ReCiterArticle> reCiterArticles = entry.getValue().getArticleCluster();

			double emailStrategyScore = ((TargetAuthorStrategyContext) emailStrategyContext).executeStrategy(reCiterArticles, targetAuthor);
			if (emailStrategyScore > 0) {
				selectedClusterIds.add(clusterId);
//				analysisObject.setEmailStrategyScore(emailStrategyScore);
			}

			double departmentStrategyScore = ((TargetAuthorStrategyContext) departmentStringMatchStrategyContext).executeStrategy(reCiterArticles, targetAuthor);
			if (departmentStrategyScore > 0) {
				selectedClusterIds.add(clusterId);
//				analysisObject.setDepartmentStrategyScore(departmentStrategyScore);
			}

			double knownCoinvestigatorStrategyScore = ((TargetAuthorStrategyContext) grantCoauthorStrategyContext).executeStrategy(reCiterArticles, targetAuthor);
			if (knownCoinvestigatorStrategyScore > 0) {
				selectedClusterIds.add(clusterId);
//				analysisObject.setKnownCoinvestigatorScore(knownCoinvestigatorStrategyScore);
			}

			double affiliationScore = ((TargetAuthorStrategyContext)affiliationStrategyContext).executeStrategy(reCiterArticles, targetAuthor);
			if (affiliationScore > 0) {
				selectedClusterIds.add(clusterId);
//				analysisObject.setAffiliationScore(affiliationScore);
			}
		}
		this.selectedClusterIds = selectedClusterIds;
	}

	/**
	 * Reassign individual articles that are similar to the target author based
	 * on a given instance of strategy context.
	 * @param strategyContexts list of strategy context that are going to be used to reassign the article.
	 * @param clusters current state of the clusters.
	 * @param targetAuthor target author.
	 */
	public void reAssignArticles(List<StrategyContext> strategyContexts, Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {
		for (StrategyContext strategyContext : strategyContexts) {
			handleStrategyContext(strategyContext, clusters, targetAuthor);
		}
	}

	/**
	 * Handler for target author specific strategy context.
	 * @param targetAuthorStrategyContext
	 * @param clusters
	 * @param targetAuthor
	 */
	public void handleTargetAuthorStrategyContext(
			TargetAuthorStrategyContext targetAuthorStrategyContext, 
			Map<Integer, ReCiterCluster> clusters, 
			TargetAuthor targetAuthor) {

		// Map of cluster ids to ReCiterarticle objects. Keep tracks of the new cluster ids that these
		// ReCiterArticle objects will be placed at the end of the below loop.
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds) {
			for (Entry<Integer, ReCiterCluster> entry : clusters.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.contains(entry.getKey())) {

					// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
					Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
					while (iterator.hasNext()) {
						ReCiterArticle otherReCiterArticle = iterator.next();

						if (targetAuthorStrategyContext.executeStrategy(otherReCiterArticle, targetAuthor) > 0) {
							if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
								clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
							} else {
								List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
								articleList.add(otherReCiterArticle);
								clusterIdToReCiterArticleList.put(clusterId, articleList);
							}
							// remove from old cluster.
							iterator.remove();
						}
					}
				}
			}
		}
		// Now move the selected article to new cluster using clusterIdToReCiterArticleList map.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				clusters.get(entry.getKey()).add(article);
			}
		}
	}

	/**
	 * Handler for ReCiterArticle specific strategy context.
	 * @param reCiterArticleStrategyContext
	 * @param clusters
	 * @param targetAuthor
	 */
	public void handleReCiterArticleStrategyContext(
			ReCiterArticleStrategyContext reCiterArticleStrategyContext,
			Map<Integer, ReCiterCluster> clusters, 
			TargetAuthor targetAuthor) {

		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds) {
			for (Entry<Integer, ReCiterCluster> entry : clusters.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.contains(entry.getKey())) {
					for (ReCiterArticle reCiterArticle : clusters.get(clusterId).getArticleCluster()) {

						// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
						Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
						while (iterator.hasNext()) {
							ReCiterArticle otherReCiterArticle = iterator.next();

							if (reCiterArticleStrategyContext.executeStrategy(reCiterArticle, otherReCiterArticle) > 0) {
								if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
									clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
								} else {
									List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
									articleList.add(otherReCiterArticle);
									clusterIdToReCiterArticleList.put(clusterId, articleList);
								}
								// remove from old cluster.
								iterator.remove();
							}
						}
					}
				}
			}
		}

		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				clusters.get(entry.getKey()).add(article);
			}
		}
	}

	/**
	 * Handler for a generic StrategyContext object.
	 * @param strategyContext
	 * @param clusters
	 * @param targetAuthor
	 */
	public void handleStrategyContext(StrategyContext strategyContext, Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {
		if (strategyContext instanceof TargetAuthorStrategyContext) {
			handleTargetAuthorStrategyContext((TargetAuthorStrategyContext) strategyContext, clusters, targetAuthor);
		} else if (strategyContext instanceof ReCiterArticleStrategyContext) {
			handleReCiterArticleStrategyContext((ReCiterArticleStrategyContext) strategyContext, clusters, targetAuthor);
		}
	}

	public Set<Integer> getSelectedClusterIds() {
		return selectedClusterIds;
	}

	public void setSelectedClusterIds(Set<Integer> selectedClusterIds) {
		this.selectedClusterIds = selectedClusterIds;
	}
	
	public List<StrategyContext> getStrategyContexts() {
		return strategyContexts;
	}
}
