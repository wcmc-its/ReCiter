package reciter.algorithm.evidence.cluster.clustersize.strategy;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.cluster.AbstractRemoveClusterStrategy;
import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;

public class ClusterSizeStrategy extends AbstractRemoveClusterStrategy {

	/**
	 * Large clusters should have more than one or two pieces of evidence supporting authorship assertion
	 * https://github.com/wcmc-its/ReCiter/issues/136
	 */
	@Override
	public double executeStrategy(ReCiterCluster reCiterCluster, Identity identity) {
		double sumOfArticleScores = 0;
		int clusterSize = reCiterCluster.getArticleCluster().size();
		for (ReCiterArticle reCiterArticle : reCiterCluster.getArticleCluster()) {
			
			sumOfArticleScores += reCiterArticle.getDepartmentStrategyScore() + 
								  reCiterArticle.getKnownCoinvestigatorScore() +
								  reCiterArticle.getAffiliationScore() + 
								  reCiterArticle.getScopusStrategyScore() + 
								  reCiterArticle.getCoauthorStrategyScore() + 
								  reCiterArticle.getJournalStrategyScore() + 
								  reCiterArticle.getCitizenshipStrategyScore() + 
								  reCiterArticle.getEducationStrategyScore();
		}
		
		/*
		 * Assert that the target author did not write the publication under these circumstances:
		 * The number of records in the cluster is between 15 and 22 and there is only 1 or no pieces of evidence supporting authorship.
		 * The number of records in the cluster is between 23 and 30 and there is only 2 or fewer pieces of evidence supporting authorship.
		 * The number of records in the cluster is above 31 and there is only 3 or fewer pieces of evidence supporting authorship.
		 */
		if ((clusterSize >= 15 && clusterSize <= 22 && sumOfArticleScores <= 1) ||
			(clusterSize >= 23 && clusterSize <= 30 && sumOfArticleScores <= 2) || 
			(clusterSize >= 31 && sumOfArticleScores <= 3)) {
			
			reCiterCluster.setClusterInfo(reCiterCluster.getClusterInfo() + ", [clusterSize=" + clusterSize + " and score=" + sumOfArticleScores + "]");
			return 1;
		}
		
		return 0;
	}
}
