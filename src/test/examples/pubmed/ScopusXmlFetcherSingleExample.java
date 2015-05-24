package test.examples.pubmed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import main.xml.scopus.ScopusXmlFetcherRunnable;

public class ScopusXmlFetcherSingleExample {

	public static void main(String[] args) {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		String[] pmidList = {"25676978", "25548331", 
				"24551322", "24358447"};
		for (String pmid : pmidList) {
			ScopusXmlFetcherRunnable r = 
				new ScopusXmlFetcherRunnable("kukafka", pmid);
			executor.execute(r);
		}
		executor.shutdown();
	}
}
