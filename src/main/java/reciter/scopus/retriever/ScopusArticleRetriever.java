package reciter.scopus.retriever;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.model.scopus.ScopusArticle;
import reciter.scopus.callable.ScopusUriParserCallable;
import reciter.scopus.querybuilder.ScopusXmlQuery;
import reciter.scopus.xmlparser.ScopusXmlHandler;

public class ScopusArticleRetriever {

	private final static Logger slf4jLogger = LoggerFactory.getLogger(ScopusArticleRetriever.class);

	/**
	 * Scopus retrieval threshold.
	 */
	protected static final int SCOPUS_DEFAULT_THRESHOLD = 24;

	/**
	 * Scopus retrieval max threshold.
	 */
	protected static final int SCOPUS_MAX_THRESHOLD = 25;

	public List<ScopusArticle> retrieveScopus(List<Long> pmids) {

		List<String> pmidQueries = new ArrayList<String>();
		if (pmids.size() == 1) {
			pmidQueries.add("pmid(" + pmids + ")");
		} else {
			StringBuffer sb = new StringBuffer();
			int i = 0;
			Iterator<Long> itr = pmids.iterator();
			while (itr.hasNext()) {
				long pmid = itr.next();
				if (i == 0 || (i % SCOPUS_DEFAULT_THRESHOLD != 0 && i != pmids.size() - 1)) {
					sb.append("pmid(");
					sb.append(pmid);
					sb.append(")+OR+");
				} else {
					sb.append("pmid(");
					sb.append(pmid);
					sb.append(")");
				}
				if (i != 0 && i % SCOPUS_DEFAULT_THRESHOLD == 0) {
					pmidQueries.add(sb.toString());
					sb = new StringBuffer();
				}
				i++;
			}
			// add the remaining pmids
			String remaining = sb.toString();
			if (!remaining.isEmpty()) {
				pmidQueries.add(remaining);
			}
		}

		List<Callable<List<ScopusArticle>>> callables = new ArrayList<Callable<List<ScopusArticle>>>();

		for (String query : pmidQueries) {
			ScopusXmlQuery scopusXmlQuery = new ScopusXmlQuery.ScopusXmlQueryBuilder(query, SCOPUS_MAX_THRESHOLD).build();
			String scopusUrl = scopusXmlQuery.getQueryUrl();
			ScopusUriParserCallable scopusUriParserCallable = new ScopusUriParserCallable(new ScopusXmlHandler(), scopusUrl);
			callables.add(scopusUriParserCallable);
		}

		List<List<ScopusArticle>> list = new ArrayList<List<ScopusArticle>>();

		int numAvailableProcessors = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numAvailableProcessors);

		try {
			executor.invokeAll(callables)
			.stream()
			.map(future -> {
				try {
					return future.get();
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}).forEach(list::add);
		} catch (InterruptedException e) {
			slf4jLogger.error("Unable to invoke callable.", e);
		}

		List<ScopusArticle> results = new ArrayList<ScopusArticle>();
		list.forEach(results::addAll);
		return results;
	}
}
