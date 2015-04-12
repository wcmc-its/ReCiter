package test.examples.pubmed;

import java.util.Map.Entry;

import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.AnalysisCSVParser;
import main.xml.pubmed.PubmedXmlFetcher;

/**
 * Retrieve all publications in "cwid_test.data.csv".
 * @author jil3004
 *
 */
public class RetrievePublication {

	public static void main(String[] args) throws InterruptedException {
		AnalysisCSVParser parser = new AnalysisCSVParser();
		int i = 1;
		for (Entry<String, ReCiterAuthor> entry : parser.parse("cwid_test.data.csv").entrySet()) {
			String cwid = entry.getKey();
			ReCiterAuthor author = entry.getValue();
			if (cwid.equals("hef9020")) {


				System.out.println(i + ": cwid: " + cwid + " author: " + author);
				i++;
				String firstName = author.getAuthorName().getFirstName();
				String lastName = author.getAuthorName().getLastName();
				PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
				pubmedXmlFetcher.fetch(lastName, firstName.substring(0, 1), cwid);
			}
		}
	}
}
