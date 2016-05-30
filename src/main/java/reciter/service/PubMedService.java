package reciter.service;

import java.util.List;

import reciter.xml.parser.pubmed.model.PubmedArticle;

public interface PubMedService {

	/**
	 * Insert the PubMedArticle to disk.
	 * @param article
	 */
	void insertPubMedArticle(PubmedArticle article);

	/**
	 * Updates or insert if not exist by query pmid.
	 * @param json
	 * @param pmid
	 */
	void upsertPubMedArticle(PubmedArticle article);
	
	/**
	 * Retrieve PubMed articles for <code>cwid</code>.
	 * @param cwid
	 * @return
	 */
	List<PubmedArticle> retrieve(String cwid);
	
}
