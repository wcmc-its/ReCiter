package test.examples.pubmed;

import main.xml.pubmed.PubmedXmlFetcher;

/**
 * A PubMed XML fetcher example that stores XML contents retrieved in folder xml_cwid.
 * @author jil3004
 *
 */
public class PubMedXmlFetcherExample {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Please enter last name, first initial, and cwid");
		} else {
			String lastName = args[0];
			String firstInitial = args[1];
			String cwid = args[2];
			PubmedXmlFetcher fetcher = new PubmedXmlFetcher();
			long startTime = System.currentTimeMillis();
			fetcher.fetch(lastName, firstInitial, cwid); // should store xml for this person.
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println("Total fetch time: " + elapsedTime + " ms.");
		}
	}
}
