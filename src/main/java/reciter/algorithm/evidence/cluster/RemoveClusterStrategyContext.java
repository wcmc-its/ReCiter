package reciter.algorithm.evidence.cluster;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.model.identity.Identity;

public interface RemoveClusterStrategyContext extends StrategyContext {
	double executeStrategy(ReCiterCluster reCiterCluster, Identity identity);
}
