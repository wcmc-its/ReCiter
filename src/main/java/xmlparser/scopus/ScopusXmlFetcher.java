package xmlparser.scopus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
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

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlFetcher.class);
	private static final String PROPERTIES_FILE_LOCATION = "src/main/resources/config/reciter.properties";
	private ScopusXmlParser scopusXmlParser;

	private void loadProperty() {
		Properties p = new Properties();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(PROPERTIES_FILE_LOCATION);
			p.load(inputStream);
		} catch (IOException e) {
			slf4jLogger.error(e.getMessage(), e);
		}

		directory = p.getProperty("scopus_xml_folder");
	}

	public class ScopusXmlFetcherRunnable implements Runnable {

		private final String cwid;
		private final String pmid;
		private final String scopusDirectory;

		public ScopusXmlFetcherRunnable(String cwid, String pmid, String scopusDirectory) {
			this.cwid = cwid;
			this.pmid = pmid;
			this.scopusDirectory = scopusDirectory;
		}

		public ScopusXmlFetcherRunnable(String cwid, String pmid) {
			this.cwid = cwid;
			this.pmid = pmid;
			scopusDirectory = null;

		}

		@Override
		public void run() {
			ScopusXmlFetcher scopusXmlFetcher;
			if (scopusDirectory == null) {
				scopusXmlFetcher = new ScopusXmlFetcher();
			} else {
				scopusXmlFetcher = new ScopusXmlFetcher(scopusDirectory);
			}
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
		File file = new File(directory + cwid + "/" + pmid + ".xml");
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
		return getScopusXml(new File(directory + cwid + "/" + pmid + ".xml"));
	}

	public void fetch(String query, String fileName, String pubmedFileLocation, String scopusFileLocation) {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		PubmedXmlFetcher pubmedXmlFetcher;
		if (pubmedFileLocation == null) {
			pubmedXmlFetcher = new PubmedXmlFetcher();
		} else {
			pubmedXmlFetcher = new PubmedXmlFetcher(pubmedFileLocation);
		}
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(query, fileName);
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			ScopusXmlFetcherRunnable scopusRunnable = new ScopusXmlFetcherRunnable(fileName, pmid, scopusFileLocation);
			executor.execute(scopusRunnable);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {}
		//		slf4jLogger.info("Completed retrieving Scopus articles for " + cwid);
	}

	@Override
	public void fetch(String query, String fileName) {
		fetch(query, fileName, null, null);
	}

	public ScopusXmlFetcher() {
		loadProperty();
		scopusXmlParser = new ScopusXmlParser(new ScopusXmlHandler());
	}

	public ScopusXmlFetcher(String directory) {
		super(directory);
		scopusXmlParser = new ScopusXmlParser(new ScopusXmlHandler());
	}
}
