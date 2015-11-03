package xmlparser.scopus;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xmlparser.AbstractXmlFetcher;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.model.ScopusArticle;

/**
 * This class handles fetching the XML from Scopus and saving it to disk.
 * @author jil3004
 *
 */
public class ScopusXmlFetcher extends AbstractXmlFetcher {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlFetcher.class);
//	private static final String DEFAULT_LOCATION = "src/main/resources/data/scopus/";
	private static final String DEFAULT_LOCATION = "/home/jil3004/reciter_data/data/scopus/";
	
	private ScopusXmlParser scopusXmlParser;

	public class ScopusXmlFetcherRunnable implements Runnable {

		private final String cwid;
		private final String pmid;

		public ScopusXmlFetcherRunnable(String cwid, String pmid) {
			this.cwid = cwid;
			this.pmid = pmid;
		}

		@Override
		public void run() {
			ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
			if (!scopusFileExist(cwid, pmid)) {
				scopusXmlFetcher.fetchSingleScopus(cwid, pmid);
			}
		}
	}

	/**
	 * Fetch a single Scopus XML file based on the PMID and save XML file into disk.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 * @param pmid
	 */
	public void fetchSingleScopus(String cwid, String pmid) {
		ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder(pmid).build();
		saveXml(scopusXmlQuery.getQueryUrl(), cwid, pmid);
	}

	/**
	 * Check if a Scopus XML file exists.
	 * @param cwid
	 * @param pmid
	 * @return
	 */
	public boolean scopusFileExist(String cwid, String pmid) {
		File file = new File(DEFAULT_LOCATION + cwid + "/" + pmid + ".xml");
		return file.exists();
	}

	public ScopusArticle getScopusXml(File scopusXmlFile) {
		return scopusXmlFile.exists() ? scopusXmlParser.parse(scopusXmlFile) : null;
	}

	/**
	 * Fetch a single Scopus XML file based on the PMID.
	 * @param lastName
	 * @param firstInitial
	 * @param cwid
	 * @param pmid
	 */
	public ScopusArticle getScopusXml(String cwid, String pmid) {
		return getScopusXml(new File(DEFAULT_LOCATION + cwid + "/" + pmid + ".xml"));
	}

	@Override
	public void fetch(String lastName, String firstName, String middleName, String cwid) {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstName.substring(0, 1), middleName, cwid);
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			ScopusXmlFetcherRunnable scopusRunnable = new ScopusXmlFetcherRunnable(cwid, pmid);
			executor.execute(scopusRunnable);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {}
//		slf4jLogger.info("Completed retrieving Scopus articles for " + cwid);
	}

	public ScopusXmlFetcher() {
		super(DEFAULT_LOCATION);
		scopusXmlParser = new ScopusXmlParser(new ScopusXmlHandler());
	}

	public ScopusXmlFetcher(String directory) {
		super(directory);
	}
}
