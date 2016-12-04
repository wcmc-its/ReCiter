package reciter.database.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import reciter.model.pubmed.PubMedArticle;

public interface PubMedRepository extends MongoRepository<PubMedArticle, String>, PubMedRepositoryCustom {
	
//	@Query(value = "{ 'medlineCitation.medlineCitationPMID.pmid': { $exists: true, $in: ?0 } }", fields = "{ 'medlineCitation.medlineCitationPMID.pmid' : 1 }")
	@Query(value = "{ 'pubMedArticle.medlineCitation.medlineCitationPMID.pmid': { $exists: true, $in: ?0 } }")
    List<PubMedArticle> findByMedlineCitationMedlineCitationPMIDPmid(List<Long> pmids);
}
