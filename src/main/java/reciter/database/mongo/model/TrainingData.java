package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="trainingdata")
public class TrainingData {
	
	private List<String> cwids;

	public List<String> getCwids() {
		return cwids;
	}

	public void setCwids(List<String> cwids) {
		this.cwids = cwids;
	}
	
}
