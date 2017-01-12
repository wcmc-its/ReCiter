package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Component;

import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.pubmed.retriever.PubMedArticleRetriever;
import reciter.xml.retriever.pubmed.PubMedQuery.PubMedQueryBuilder;

@Component("goldStandardRetrievalStrategy")
public class GoldStandardRetrievalStrategy extends AbstractRetrievalStrategy {

	@Override
	public String getRetrievalStrategyName() {
		return this.getClass().getName();
	}

	@Override
	protected List<PubMedQuery> buildQuery(Identity identity) {
		throw new UnsupportedOperationException("Does not support retrieval.");
	}

	@Override
	protected List<PubMedQuery> buildQuery(Identity identity, LocalDate startDate, LocalDate endDate) {
		throw new UnsupportedOperationException("Does not support retrieval.");
	}

	@Override
	public RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException {
		Map<String, Integer> queries = new PubMedQueryBuilder(pmids).buildPmids();
		PubMedArticleRetriever pubMedArticleRetriever = new PubMedArticleRetriever();
		
		Map<Long, PubMedArticle> pubMedArticles = new HashMap<>();
		List<PubMedQueryResult> pubMedQueryResults = new ArrayList<>();
		
		for (Entry<String, Integer> entry : queries.entrySet()) {
			List<PubMedArticle> results = pubMedArticleRetriever.retrievePubMed(
					URLEncoder.encode(entry.getKey(), "UTF-8"), entry.getValue());
			
			for (PubMedArticle pubMedArticle : results) {
				pubMedArticles.put(pubMedArticle.getMedlineCitation().getMedlineCitationPMID().getPmid(), pubMedArticle);
			}
			PubMedQueryResult pubMedQueryResult = new PubMedQueryResult(entry.getKey());
			pubMedQueryResult.setUsed(true);
			pubMedQueryResult.setNumResult(entry.getValue());
		}
		return new RetrievalResult(pubMedArticles, pubMedQueryResults);
	}
}
