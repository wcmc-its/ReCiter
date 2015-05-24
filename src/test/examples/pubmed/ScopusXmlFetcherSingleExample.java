package test.examples.pubmed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.reciter.utils.ReCiterConfigProperty;
import main.xml.pubmed.PubmedXmlFetcher;
import main.xml.pubmed.model.PubmedArticle;
import main.xml.scopus.ScopusXmlFetcherRunnable;

public class ScopusXmlFetcherSingleExample {

	private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlFetcherSingleExample.class);
	
	public static void main(String[] args) throws IOException {
		
		slf4jLogger.info("Number of available processors: " + NUM_THREADS);
		long startTime = System.currentTimeMillis();
		// Load configuration data.
		Files.walk(Paths.get("data/xml")).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				String cwid = filePath.getFileName().toString().replace("_0.xml", "");
				ReCiterConfigProperty reCiterConfigProperty = new ReCiterConfigProperty();
				try {	
					reCiterConfigProperty.loadProperty("data/properties/" + cwid + "/" + cwid + ".properties");
					
					String lastName = reCiterConfigProperty.getLastName();
					String firstName = reCiterConfigProperty.getFirstName();
					String firstInitial = firstName.substring(0, 1);
					
					slf4jLogger.info("Fetching Scopus data for : " + cwid);
					// Get PMIDs from PubMed.
					PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
					pubmedXmlFetcher.setPerformRetrievePublication(reCiterConfigProperty.isPerformRetrievePublication());
					List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstInitial, cwid);
					
					ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
					for (PubmedArticle pubmedArticle : pubmedArticleList) {
						String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
						ScopusXmlFetcherRunnable scopusRunnable = new ScopusXmlFetcherRunnable(cwid, pmid);
						executor.execute(scopusRunnable);
					}
					executor.shutdown();
					while (!executor.isTerminated()) {}
					slf4jLogger.info("Completed : " + cwid);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		slf4jLogger.info("Total execution time: " + elapsedTime + " ms.");
	}
}
