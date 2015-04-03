package test.examples.pubmed;

import java.util.Map.Entry;

import main.reciter.model.author.ReCiterAuthor;
import main.reciter.utils.InputCsvParser;
import main.xml.scopus.ScopusXmlFetcher;

public class RetrieveScopusPublication {
	
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
			ScopusXmlFetcher fetcher = new ScopusXmlFetcher();
			fetcher.fetch(lastName, firstName, cwid);
		}
	}
}
