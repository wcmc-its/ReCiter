package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "goldstandard")
public class GoldStandard {
	
	@Id
	private String id;
	private String cwid;
	private List<Long> pmids;
	
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public List<Long> getPmids() {
		return pmids;
	}
	public void setPmids(List<Long> pmids) {
		this.pmids = pmids;
	}
}
