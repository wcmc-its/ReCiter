package reciter.xml.retriever.pubmed;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import reciter.model.identity.Identity;
import reciter.model.scopus.ScopusArticle;
import reciter.xml.retriever.pubmed.AbstractRetrievalStrategy.RetrievalResult;

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
	RetrievalResult retrievePubMedArticles(Identity identity) throws IOException;
	
	/**
	 * Retrieve the articles for this identity restricted by the start date and end date.
	 * 
	 * @param identity
	 * 
	 * @return Unique map of PMID to of PubMed articles for this identity.
	 */
	RetrievalResult retrievePubMedArticles(Identity identity, LocalDate startDate, LocalDate endDate) throws IOException;
	
	/**
	 * Retrieve the articles for list of pmids.
	 * 
	 * @param pmids
	 * 
	 * @return Unique map of PMID to of PubMed articles for this identity.
	 */
	RetrievalResult retrievePubMedArticles(List<Long> pmids) throws IOException;

	/**
	 * Retrieve Scopus articles based on list of doi strings.
	 * 
	 * @param dois
	 * @return
	 */
	List<ScopusArticle> retrieveScopusDoi(Collection<String> dois);
}
