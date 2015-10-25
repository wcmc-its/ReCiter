package reciter.algorithm.cluster.targetauthor;

import java.util.Map;
import java.util.Set;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.model.author.TargetAuthor;

public class ReCiterClusterSelector extends AbstractClusterSelector {

	private TargetAuthorStrategyContext boardCertificationStrategyContext;
	private TargetAuthorStrategyContext emailStrategyContext;
	private TargetAuthorStrategyContext scopusStrategyContext;
	private TargetAuthorStrategyContext degreeStrategyContext;
	private TargetAuthorStrategyContext departmentStringMatchStrategyContext;
	private TargetAuthorStrategyContext grantCoauthorStrategyContext;

	public Set<Integer> select(Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor) {


		return null;
	}



	public void selectClusters() {
		
	}
	
	//
	//public void selectIndividualReCiterArticles() {
	//	
	//}
}
