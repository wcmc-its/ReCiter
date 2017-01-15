package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="trainingdata")
public class TrainingData {
	
	@Id
	private String id;
	private List<String> uids;

	public List<String> getUid() {
		return uids;
	}

	public void setUid(List<String> uids) {
		this.uids = uids;
	}
	
}
