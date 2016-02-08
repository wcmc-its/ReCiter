package reciter.algorithm.evidence.cluster;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.author.TargetAuthor;

public interface RemoveClusterStrategy {
	double executeStrategy(ReCiterCluster reCiterCluster, TargetAuthor targetAuthor);
}
