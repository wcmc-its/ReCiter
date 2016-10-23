package reciter.database.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import reciter.xml.parser.scopus.model.ScopusArticle;

public interface ScopusRepository extends MongoRepository<ScopusArticle, String> {

	@Query(value = "{ 'pubmedId': { $exists: true, $in: ?0 } }")
    List<ScopusArticle> findByPubmedId(List<Long> pmids);
}
