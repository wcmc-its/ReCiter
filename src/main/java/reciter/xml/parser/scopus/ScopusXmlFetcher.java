package reciter.xml.parser.scopus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import reciter.engine.ReCiterEngineProperty;
import reciter.xml.parser.AbstractXmlFetcher;
import reciter.xml.parser.pubmed.PubmedXmlFetcher;
import reciter.xml.parser.pubmed.model.PubmedArticle;
import reciter.xml.parser.scopus.model.ScopusArticle;

/**
 * This class handles fetching the XML from Scopus and saving it to disk.
 * @author jil3004
 *
 */
public class ScopusXmlFetcher extends AbstractXmlFetcher {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlFetcher.class);
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
		saveXml(scopusXmlQuery.getQueryUrl(), ReCiterEngineProperty.scopusFolder, cwid, pmid);
	}

	/**
	 * Check if a Scopus XML file exists.
	 * @param cwid
	 * @param pmid
	 * @return
	 */
	public boolean scopusFileExist(String cwid, String pmid) {
		File file = new File(ReCiterEngineProperty.scopusFolder + cwid + "/" + pmid + ".xml");
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
		return getScopusXml(new File(ReCiterEngineProperty.scopusFolder + cwid + "/" + pmid + ".xml"));
	}

	public void fetch(String cwid) throws ParserConfigurationException, SAXException, IOException {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
		List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(cwid);
		for (PubmedArticle pubmedArticle : pubmedArticleList) {
			String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
			ScopusXmlFetcherRunnable scopusRunnable = new ScopusXmlFetcherRunnable(cwid, pmid);
			executor.execute(scopusRunnable);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {}
		
		slf4jLogger.info("Completed retrieving Scopus articles for " + cwid);
	}

	public ScopusXmlFetcher() {
		scopusXmlParser = new ScopusXmlParser(new ScopusXmlHandler());
	}
}
