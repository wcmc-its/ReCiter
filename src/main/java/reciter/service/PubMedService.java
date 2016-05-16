package reciter.service;

import java.util.List;

import reciter.xml.parser.pubmed.model.PubmedArticle;

public interface PubMedService {

	/**
	 * Persist the PubMedArticle to disk.
	 * @param article
	 */
	void persist(PubmedArticle article);
	
	/**
	 * Retrieve PubMed articles for <code>cwid</code>.
	 * @param cwid
	 * @return
	 */
	List<PubmedArticle> retrieve(String cwid);
	
	
}
