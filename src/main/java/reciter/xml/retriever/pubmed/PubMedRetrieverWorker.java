package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.xml.parser.pubmed.PubmedXmlQuery;
import reciter.xml.parser.pubmed.handler.PubmedESearchHandler;

public class PubMedRetrieverWorker implements Runnable {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(PubMedRetrieverWorker.class);

	private final String pubmedQuery;
	private final String commonDirectory;
	private final String cwid;
	private final int numberOfPubmedArticles;

	public PubMedRetrieverWorker(String pubmedQuery, String commonDirectory, String cwid, int numberOfPubmedArticles) {
		this.pubmedQuery = pubmedQuery;
		this.commonDirectory = commonDirectory;
		this.cwid = cwid;
		this.numberOfPubmedArticles = numberOfPubmedArticles;
	}

	@Override
	public void run() {
		try {
			slf4jLogger.info("retrieving PubMed articles for cwid=[" + cwid + "]...");
			retrieve(pubmedQuery, commonDirectory, cwid, numberOfPubmedArticles);
		} catch (IOException e) {
			slf4jLogger.error("Error retrieving articles for cwid=[" + cwid + "].", e);
		}
	}

	/**
	 * Fetch PubMed xmls using <code>pubmedQuery</code> and store the xmls in <code>commonDirectory</code> specified
	 * by <code>cwid</code>
	 * @param pubmedQuery
	 * @param commonDirectory
	 * @param cwid
	 * @param numberOfPubmedArticles
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws UnsupportedEncodingException 
	 */
	public void retrieve(String pubmedQuery, String commonDirectory, String cwid, int numberOfPubmedArticles) 
			throws UnsupportedEncodingException, MalformedURLException, IOException {

		// Get the count (number of publications for this query).
		PubmedXmlQuery pubmedXmlQuery = new PubmedXmlQuery();
		pubmedXmlQuery.setTerm(pubmedQuery);

		// Retrieve the publications retMax records at one time and store to disk.
		int currentRetStart = 0;

		// Number of partitions that we need to finish retrieving all XML.
		int numSteps = (int) Math.ceil((double)numberOfPubmedArticles / pubmedXmlQuery.getRetMax()); 

		// Use the retstart value to iteratively fetch all XMLs.
		for (int i = 0; i < numSteps; i++) {
			// Get webenv value.
			pubmedXmlQuery.setRetStart(currentRetStart);
			String eSearchUrl = pubmedXmlQuery.buildESearchQuery();

			pubmedXmlQuery.setWevEnv(PubmedESearchHandler.executeESearchQuery(eSearchUrl).getWebEnv());

			// Use the webenv value to retrieve xml.
			String eFetchUrl = pubmedXmlQuery.buildEFetchQuery();

			// Save the xml file to directory data/xml/cwid
			LocalDateTime timePoint = LocalDateTime.now();
			
			PubMedPersistenceWorker worker = 
					new PubMedPersistenceWorker(eFetchUrl, commonDirectory, cwid, timePoint.toString().replace(":", "-"));
			worker.run();
			
			// Update the retstart value.
			currentRetStart += pubmedXmlQuery.getRetMax();
			pubmedXmlQuery.setRetStart(currentRetStart);
		}
	}
}
