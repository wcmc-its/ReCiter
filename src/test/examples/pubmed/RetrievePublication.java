package test.examples.pubmed;

import java.util.Map.Entry;

import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.AnalysisCSVParser;
import main.xml.pubmed.PubmedXmlFetcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieve all publications in "cwid_test.data.csv".
 * @author jil3004
 *
 */
public class RetrievePublication {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(RetrievePublication.class);
	
	public static void main(String[] args) throws InterruptedException {
		
		AnalysisCSVParser parser = new AnalysisCSVParser();
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();

		for (Entry<String, ReCiterAuthor> entry : parser.parse("cwid_test.data.csv").entrySet()) {
			String cwid = entry.getKey();
			ReCiterAuthor author = entry.getValue();
			String firstName = author.getAuthorName().getFirstName();
			String lastName = author.getAuthorName().getLastName();
			
			slf4jLogger.info("Retrieving publications for : " + cwid);
			
			pubmedXmlFetcher.fetch(lastName, firstName, cwid);
		}
	}
}
