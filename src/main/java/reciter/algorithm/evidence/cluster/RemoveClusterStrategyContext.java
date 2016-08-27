package reciter.algorithm.evidence.cluster;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.database.mongo.model.Identity;

public interface RemoveClusterStrategyContext extends StrategyContext {
	double executeStrategy(ReCiterCluster reCiterCluster, Identity identity);
}
