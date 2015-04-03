package test.examples.pubmed;

import java.util.Map.Entry;

import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.InputCsvParser;
import main.xml.pubmed.PubmedXmlFetcher;

/**
 * Retrieve all publications in "cwid_test.data.csv".
 * @author jil3004
 *
 */
public class RetreivePublication {

	public static void main(String[] args) throws InterruptedException {
		InputCsvParser parser = new InputCsvParser();
		int i = 1;
		for (Entry<String, ReCiterAuthor> entry : parser.parse("cwid_test.data.csv").entrySet()) {
			String cwid = entry.getKey();
			ReCiterAuthor author = entry.getValue();
			System.out.println(i + ": cwid: " + cwid + " author: " + author);
			i++;
			String firstName = author.getAuthorName().getFirstName();
			String lastName = author.getAuthorName().getLastName();
			PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
			pubmedXmlFetcher.fetch(lastName, firstName, cwid);
		}
	}
}
