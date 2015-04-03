package test.examples.pubmed;

import main.xml.scopus.ScopusXmlFetcher;

/**
 * Scopus Affiliation fetcher example.
 * @author jil3004
 *
 */
public class ScopusXmlFetcherExample {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Please enter last name, first initial, and cwid");
		} else {
			String lastName = args[0];
			String firstInitial = args[1];
			String cwid = args[2];
			ScopusXmlFetcher fetcher = new ScopusXmlFetcher();
			long startTime = System.currentTimeMillis();
			fetcher.fetch(lastName, firstInitial, cwid);
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println("Total fetch time: " + elapsedTime + " ms.");
		}
	}
}
