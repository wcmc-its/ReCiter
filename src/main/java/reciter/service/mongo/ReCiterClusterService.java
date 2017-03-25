package reciter.service.mongo;

import java.util.List;

import reciter.algorithm.cluster.model.ReCiterCluster;

public interface ReCiterClusterService {

	void save(List<ReCiterCluster> reCiterClusters, String cwid);
}
