package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "goldstandard")
public class GoldStandard {
	
	@Id
	private String id;
	private List<Long> knownPmids;
	private List<Long> rejectedPmids;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<Long> getKnownPmids() {
		return knownPmids;
	}
	public void setKnownPmids(List<Long> knownPmids) {
		this.knownPmids = knownPmids;
	}
	public List<Long> getRejectedPmids() {
		return rejectedPmids;
	}
	public void setRejectedPmids(List<Long> rejectedPmids) {
		this.rejectedPmids = rejectedPmids;
	}
}
