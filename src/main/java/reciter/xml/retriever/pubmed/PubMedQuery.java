package reciter.xml.retriever.pubmed;

public class PubMedQuery {
	
	// Query that tends to return more results than the strict query.
	private PubMedQueryResult lenientQuery;
	
	// QUery that tends to return less results than the lenient query.
	private PubMedQueryResult strictQuery;

	public PubMedQueryResult getLenientQuery() {
		return lenientQuery;
	}

	public void setLenientQuery(PubMedQueryResult lenientQuery) {
		this.lenientQuery = lenientQuery;
	}

	public PubMedQueryResult getStrictQuery() {
		return strictQuery;
	}

	public void setStrictQuery(PubMedQueryResult strictQuery) {
		this.strictQuery = strictQuery;
	}
}
