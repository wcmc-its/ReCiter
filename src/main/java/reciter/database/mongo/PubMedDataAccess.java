package reciter.database.mongo;

import reciter.xml.parser.pubmed.model.PubMedArticle;

public interface PubMedDataAccess {

	void insertPubMedArticle(String json);

	void upsertPubMedArticle(String json, long pmid);
	
	PubMedArticle getPubMedArticleByPmid(long pmid);
}
