package reciter.database.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.model.pubmed.PubMedArticle;

public interface PubMedRepository extends MongoRepository<PubMedArticle, String> {

//	void insertPubMedArticle(String json);
//
//	void upsertPubMedArticle(String json, long pmid);
	
//	PubMedArticle findByMedlineCitationPMID(MedlineCitationPMID medlineCitationPMID);
	
//	List<PubMedArticle> findPubMedArticles(String cwid);
}
