package reciter.database.mongo.model;

import java.time.LocalDateTime;
import java.util.List;

public class ESearchPmid {

	private List<Long> pmids;
	private String retrievalStrategyName;
	private LocalDateTime retrievalDate;

	public ESearchPmid(List<Long> pmids, String retrievalStrategyName, LocalDateTime retrievalDate) {
		this.pmids = pmids;
		this.retrievalStrategyName = retrievalStrategyName;
		this.retrievalDate = retrievalDate;
	}
	
	public List<Long> getPmids() {
		return pmids;
	}
	public void setPmids(List<Long> pmids) {
		this.pmids = pmids;
	}
	public String getRetrievalStrategyName() {
		return retrievalStrategyName;
	}
	public void setRetrievalStrategyName(String retrievalStrategyName) {
		this.retrievalStrategyName = retrievalStrategyName;
	}
	public LocalDateTime getRetrievalDate() {
		return retrievalDate;
	}
	public void setRetrievalDate(LocalDateTime retrievalDate) {
		this.retrievalDate = retrievalDate;
	}
}
