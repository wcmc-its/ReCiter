package reciter.database.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import reciter.model.pubmed.PubMedArticle;

public interface PubMedRepository extends MongoRepository<PubMedArticle, String>, PubMedRepositoryCustom {

//	void insertPubMedArticle(String json);
//
//	void upsertPubMedArticle(String json, long pmid);
	
//	PubMedArticle findByMedlineCitationPMID(MedlineCitationPMID medlineCitationPMID);
	
//	List<PubMedArticle> findPubMedArticles(String cwid);
	
	@Query(value = "{ 'medlineCitation.medlineCitationPMID.pmid': { $exists: true, $in: ?0 } }", fields = "{ 'medlineCitation.medlineCitationPMID.pmid' : 1 }")
    List<PubMedArticle> findByMedlineCitationMedlineCitationPMIDPmid(List<Long> pmids);
	
	@Query(value = "{ 'medlineCitation.article.authorList.lastName': { $exists: true, $in: ?0 } }", fields = "{ 'medlineCitation.article.authorList.lastName' : 1 }")
	List<PubMedArticle> findByMedlineCitationArticleAuthorListLastName(String lastName);
}
