package reciter.algorithm.evidence.cluster;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.database.mongo.model.Identity;

public interface RemoveClusterStrategy {
	double executeStrategy(ReCiterCluster reCiterCluster, Identity identity);
}
