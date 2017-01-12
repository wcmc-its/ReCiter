package reciter.engine;

import java.util.List;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.engine.erroranalysis.Analysis;

public class EngineOutput {

	private Analysis analysis;
	private List<ReCiterCluster> reCiterClusters;

	public Analysis getAnalysis() {
		return analysis;
	}
	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
	}
	public List<ReCiterCluster> getReCiterClusters() {
		return reCiterClusters;
	}
	public void setReCiterClusters(List<ReCiterCluster> reCiterClusters) {
		this.reCiterClusters = reCiterClusters;
	}
}
