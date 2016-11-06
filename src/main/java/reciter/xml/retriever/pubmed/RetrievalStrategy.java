package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import reciter.database.mongo.model.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;

public interface RetrievalStrategy {
	
	/**
	 * Return the name of the retrieval strategy.
	 * 
	 * @return name of the retrieval strategy.
	 */
	String getRetrievalStrategyName();

	/**
	 * Retrieve Scopus articles based on list of pmids.
	 * 
	 * @param pmids List of pmids.
	 * 
	 * @return list of Scopus articles.
	 */
	List<ScopusArticle> retrieveScopus(Collection<Long> pmids);

	/**
	 * Retrieve the articles for this identity.
	 * 
	 * @param identity
	 * 
	 * @return Unique map of PMID to of PubMed articles for this identity.
	 */
	Map<Long, PubMedArticle> retrievePubMedArticles(Identity identity) throws IOException;
	
	/**
	 * Retrieve the articles for this identity restricted by the start date and end date.
	 * 
	 * @param identity
	 * 
	 * @return Unique map of PMID to of PubMed articles for this identity.
	 */
	Map<Long, PubMedArticle> retrievePubMedArticles(Identity identity, LocalDate startDate, LocalDate endDate) throws IOException;
}
