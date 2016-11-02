package reciter.xml.retriever.pubmed;

public class PubMedQueryResult {

	private String query;
	private int numResult;
	
	public PubMedQueryResult(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getNumResult() {
		return numResult;
	}
	public void setNumResult(int numResult) {
		this.numResult = numResult;
	}
}
