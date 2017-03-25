package reciter.algorithm.evidence.cluster.clustersize;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.cluster.RemoveClusterStrategy;
import reciter.algorithm.evidence.cluster.RemoveClusterStrategyContext;
import reciter.model.identity.Identity;

public class ClusterSizeStrategyContext implements RemoveClusterStrategyContext {

	private final RemoveClusterStrategy strategy;
	
	public ClusterSizeStrategyContext(RemoveClusterStrategy strategy) {
		this.strategy = strategy;
	}

	@Override
	public double executeStrategy(ReCiterCluster reCiterCluster, Identity identity) {
		return strategy.executeStrategy(reCiterCluster, identity);
	}
}
