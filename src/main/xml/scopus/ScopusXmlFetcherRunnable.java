package main.xml.scopus;

public class ScopusXmlFetcherRunnable implements Runnable {

	private final String cwid;
	private final String pmid;
	
	public ScopusXmlFetcherRunnable(String cwid, String pmid) {
		this.cwid = cwid;
		this.pmid = pmid;
	}
	
	@Override
	public void run() {
		ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
		scopusXmlFetcher.fetchSingleScopus(cwid, pmid);
	}
}
