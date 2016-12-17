package reciter.service;

import java.util.Collection;
import java.util.List;

import reciter.model.pubmed.PubMedArticle;

public interface PubMedService {

	void save(Collection<PubMedArticle> pubMedArticles);
	
	List<PubMedArticle> findByPmids(List<Long> pmids);
}
