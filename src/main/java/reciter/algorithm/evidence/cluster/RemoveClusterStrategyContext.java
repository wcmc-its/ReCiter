package reciter.algorithm.evidence.cluster;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.model.author.TargetAuthor;

public interface RemoveClusterStrategyContext extends StrategyContext {
	double executeStrategy(ReCiterCluster reCiterCluster, TargetAuthor targetAuthor);
}
