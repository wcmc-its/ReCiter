package reciter.database.mongo.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "esearchresult")
public class ESearchResult {

	private String cwid;
	private ESearchPmid eSearchPmid;
	
	public ESearchResult(String cwid, ESearchPmid eSearchPmid) {
		this.cwid = cwid;
		this.seteSearchPmid(eSearchPmid);
	}
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public ESearchPmid geteSearchPmid() {
		return eSearchPmid;
	}
	public void seteSearchPmid(ESearchPmid eSearchPmid) {
		this.eSearchPmid = eSearchPmid;
	}

}
