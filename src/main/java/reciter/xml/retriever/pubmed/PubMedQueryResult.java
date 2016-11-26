package reciter.xml.retriever.pubmed;

public class PubMedQueryResult {

	private String query;
	private int numResult;
	private boolean isUsed;

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
	public boolean isUsed() {
		return isUsed;
	}
	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}
	@Override
	public String toString() {
		return "PubMedQueryResult [query=" + query + ", numResult=" + numResult + ", isUsed=" + isUsed + "]";
	}
}
