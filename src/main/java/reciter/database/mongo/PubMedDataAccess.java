package reciter.database.mongo;

public interface PubMedDataAccess {

	void insertPubMedArticle(String json);

	void upsertPubMedArticle(String json, String pmid);
}
