package reciter.service;

import java.util.Collection;
import java.util.List;

import reciter.model.scopus.ScopusArticle;

public interface ScopusService {

	void save(Collection<ScopusArticle> pubMedArticles);
	
	List<ScopusArticle> findByPmids(List<Long> pmids);
}
