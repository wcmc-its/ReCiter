package reciter.xml.parser.scopus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import reciter.database.mongo.model.ESearchResult;
import reciter.engine.ReCiterEngineProperty;
import reciter.model.scopus.ScopusArticle;
import reciter.service.ESearchResultService;
import reciter.xml.parser.AbstractXmlFetcher;

/**
 * This class handles fetching the XML from Scopus and saving it to disk.
 * @author jil3004
 *
 */
public class ScopusXmlFetcher extends AbstractXmlFetcher {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusXmlFetcher.class);
	private ScopusXmlParser scopusXmlParser;

	@Autowired
	private ESearchResultService eSearchResultService;
	
	public class ScopusXmlFetcherRunnable implements Runnable {

		private final String cwid;
		private final long pmid;


		public ScopusXmlFetcherRunnable(String cwid, long pmid) {
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
	public void fetchSingleScopus(String cwid, long pmid) {
		String pmidStr = String.valueOf(pmid);
		ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder(pmidStr).buildSingle();
		slf4jLogger.info(scopusXmlQuery.getQueryUrl());
		saveXml(scopusXmlQuery.getQueryUrl(), ReCiterEngineProperty.scopusFolder, cwid, pmidStr);
	}

	/**
	 * Check if a Scopus XML file exists.
	 * @param cwid
	 * @param pmid
	 * @return
	 */
	public boolean scopusFileExist(String cwid, long pmid) {
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
		List<ESearchResult> eSearchResults = eSearchResultService.findByCwid(cwid);
		
		for (ESearchResult eSearchResult : eSearchResults) {
			List<Long> pmids = eSearchResult.geteSearchPmid().getPmids();
			for (long pmid : pmids) {
				ScopusXmlFetcherRunnable scopusRunnable = new ScopusXmlFetcherRunnable(cwid, pmid);
				executor.execute(scopusRunnable);
			}
		}
		executor.shutdown();
		while (!executor.isTerminated()) {}
		
		slf4jLogger.info("Completed retrieving Scopus articles for " + cwid);
	}

	public ScopusXmlFetcher() {
		scopusXmlParser = new ScopusXmlParser(new ScopusXmlHandler());
	}
}
