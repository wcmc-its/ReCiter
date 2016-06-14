package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "esearchresult")
public class ESearchResult {

	private String cwid;
	private List<Long> pmids;
	
	public ESearchResult(String cwid, List<Long> pmids) {
		this.cwid = cwid;
		this.pmids = pmids;
	}
	
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
