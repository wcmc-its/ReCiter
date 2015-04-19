package test.examples.pubmed;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.AnalysisCSVParser;
import main.xml.scopus.ScopusXmlFetcher;

/**
 * Retrieve all Scopus publications for cwids in "cwid_test.data.csv".
 * @author jil3004
 *
 */
public class RetrieveScopusPublication {
	
	private final static Logger slf4jLogger = LoggerFactory.getLogger(RetrieveScopusPublication.class);
	
	public static void main(String[] args) throws InterruptedException {
		
		AnalysisCSVParser parser = new AnalysisCSVParser();
		ScopusXmlFetcher fetcher = new ScopusXmlFetcher();
		int i = 1;
		for (Entry<String, ReCiterAuthor> entry : parser.parse("cwid_test.data.csv").entrySet()) {
			String cwid = entry.getKey();
			ReCiterAuthor author = entry.getValue();
			String firstName = author.getAuthorName().getFirstName();
			String lastName = author.getAuthorName().getLastName();
			
			slf4jLogger.info(i + ": cwid: " + cwid + " author: " + author);
			i++;
			fetcher.fetch(lastName, firstName, cwid);
		}
	}
}
