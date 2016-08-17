package reciter.service;

import java.util.List;

import reciter.xml.parser.scopus.model.ScopusArticle;

public interface ScopusService {

	void save(List<ScopusArticle> scopusArticles);

	List<ScopusArticle> findByPubmedId(List<Long> pmids);
}
