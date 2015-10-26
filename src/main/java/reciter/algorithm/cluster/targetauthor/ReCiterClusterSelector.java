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
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.AffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.department.DepartmentStrategyContext;
import reciter.algorithm.evidence.targetauthor.department.strategy.DepartmentStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.email.EmailStrategyContext;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.grant.GrantStrategyContext;
import reciter.algorithm.evidence.targetauthor.grant.strategy.KnownCoinvestigatorStrategy;
import reciter.algorithm.evidence.targetauthor.scopus.ScopusStrategyContext;
import reciter.algorithm.evidence.targetauthor.scopus.strategy.StringMatchingAffiliation;
import reciter.erroranalysis.AnalysisObject;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class ReCiterClusterSelector extends AbstractClusterSelector {

	private TargetAuthorStrategyContext emailStrategyContext;
	private TargetAuthorStrategyContext boardCertificationStrategyContext;

	private TargetAuthorStrategyContext scopusStrategyContext;
	private TargetAuthorStrategyContext degreeStrategyContext;
	private TargetAuthorStrategyContext departmentStringMatchStrategyContext;
	private TargetAuthorStrategyContext grantCoauthorStrategyContext;
	private TargetAuthorStrategyContext affiliationStrategyContext;

	private AnalysisObject analysisObject;

	private Set<Integer> selectedClusterIds;

	public ReCiterClusterSelector(AnalysisObject analysisObject) {
		emailStrategyContext = new EmailStrategyContext(new EmailStringMatchStrategy());
		departmentStringMatchStrategyContext = new DepartmentStrategyContext(new DepartmentStringMatchStrategy());
		grantCoauthorStrategyContext = new GrantStrategyContext(new KnownCoinvestigatorStrategy());
		affiliationStrategyContext = new AffiliationStrategyContext(new AffiliationStrategy());
		scopusStrategyContext = new ScopusStrategyContext(new StringMatchingAffiliation());
		this.analysisObject = analysisObject;
	}

	@Override
	public void runSelectionStrategy(Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {
		List<TargetAuthorStrategyContext> list = new ArrayList<TargetAuthorStrategyContext>();
		list.add(scopusStrategyContext);
		selectClusters(clusters, targetAuthor);
		reAssignArticles(list, clusters, targetAuthor);
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

			double emailStrategyScore = emailStrategyContext.executeStrategy(reCiterArticles, targetAuthor);
			if (emailStrategyScore > 0) {
				selectedClusterIds.add(clusterId);
				analysisObject.setEmailStrategyScore(emailStrategyScore);
			}

			double departmentStrategyScore = departmentStringMatchStrategyContext.executeStrategy(reCiterArticles, targetAuthor);
			if (departmentStrategyScore > 0) {
				selectedClusterIds.add(clusterId);
				analysisObject.setDepartmentStrategyScore(departmentStrategyScore);
			}

			double knownCoinvestigatorStrategyScore = grantCoauthorStrategyContext.executeStrategy(reCiterArticles, targetAuthor);
			if (knownCoinvestigatorStrategyScore > 0) {
				selectedClusterIds.add(clusterId);
				analysisObject.setKnownCoinvestigatorScore(knownCoinvestigatorStrategyScore);
			}

			double affiliationScore = affiliationStrategyContext.executeStrategy(reCiterArticles, targetAuthor);
			if (affiliationScore > 0) {
				selectedClusterIds.add(clusterId);
				analysisObject.setAffiliationScore(affiliationScore);
			}
		}
		this.selectedClusterIds = selectedClusterIds;
	}

	public void reAssignArticles(List<TargetAuthorStrategyContext> strategyContexts, Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {

		for (TargetAuthorStrategyContext strategyContext : strategyContexts) {

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
							
							if (strategyContext.executeStrategy(otherReCiterArticle, targetAuthor) > 0) {
								if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
									clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
								} else {
									List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
									articleList.add(otherReCiterArticle);
									clusterIdToReCiterArticleList.put(clusterId, articleList);
								}

								// remove from old cluster.
								iterator.remove();
								break; // break loop iterating over authors.
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
	}

	public void selectIndividualReCiterArticles(Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {

	}

	public Set<Integer> getSelectedClusterIds() {
		return selectedClusterIds;
	}

	public void setSelectedClusterIds(Set<Integer> selectedClusterIds) {
		this.selectedClusterIds = selectedClusterIds;
	}
}
