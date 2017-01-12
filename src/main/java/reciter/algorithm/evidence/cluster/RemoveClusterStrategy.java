package reciter.algorithm.evidence.cluster;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.identity.Identity;

public interface RemoveClusterStrategy {
	double executeStrategy(ReCiterCluster reCiterCluster, Identity identity);
}
