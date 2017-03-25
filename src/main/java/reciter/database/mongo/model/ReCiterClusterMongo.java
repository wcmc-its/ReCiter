package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import reciter.algorithm.cluster.model.ReCiterCluster;

@Document(collection = "recitercluster")
public class ReCiterClusterMongo {

	@Id
	private String cwid;
	private List<ReCiterCluster> reCiterClusters;
	
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public List<ReCiterCluster> getReCiterClusters() {
		return reCiterClusters;
	}
	public void setReCiterClusters(List<ReCiterCluster> reCiterClusters) {
		this.reCiterClusters = reCiterClusters;
	}

}
