package reciter.algorithm.cluster.targetauthor;

import java.util.Map;
import java.util.Set;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.author.TargetAuthor;

public interface ClusterSelector {

	void runSelectionStrategy(Map<Integer, ReCiterCluster> clusters, TargetAuthor targetAuthor);
	
	Set<Integer> getSelectedClusterIds();
}
