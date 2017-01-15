package reciter.database.mongo.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import reciter.xml.retriever.pubmed.PubMedQueryResult;

@Document(collection = "esearchresult")
public class ESearchResult {

	@Id
	private String id;
	private String uid;
	private ESearchPmid eSearchPmid;
	private List<PubMedQueryResult> pubMedQueryResults;
	
	public ESearchResult(String uid, ESearchPmid eSearchPmid, List<PubMedQueryResult> pubMedQueryResults) {
		this.uid = uid;
		this.eSearchPmid = eSearchPmid;
		this.pubMedQueryResults = pubMedQueryResults;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public ESearchPmid getESearchPmid() {
		return eSearchPmid;
	}
	public void setESearchPmid(ESearchPmid eSearchPmid) {
		this.eSearchPmid = eSearchPmid;
	}
	public List<PubMedQueryResult> getPubMedQueryResults() {
		return pubMedQueryResults;
	}
	public void setPubMedQueryResults(List<PubMedQueryResult> pubMedQueryResults) {
		this.pubMedQueryResults = pubMedQueryResults;
	}

}
