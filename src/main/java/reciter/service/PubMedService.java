package reciter.service;

import java.util.List;

import reciter.xml.parser.pubmed.model.PubMedArticle;

public interface PubMedService {

	/**
	 * Insert the PubMedArticle to disk.
	 * @param article
	 */
	void insertPubMedArticle(PubMedArticle article);

	/**
	 * Updates or insert if not exist by query pmid.
	 * @param json
	 * @param pmid
	 */
	void upsertPubMedArticle(PubMedArticle article);
	
	/**
	 * Retrieve PubMed articles for <code>cwid</code>.
	 * @param cwid
	 * @return
	 */
	List<PubMedArticle> retrieve(String cwid);
	
}
