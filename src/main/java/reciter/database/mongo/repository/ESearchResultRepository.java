package reciter.database.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultRepository extends MongoRepository<ESearchResult, String>, ESearchResultRepositoryCustom {

	List<ESearchResult> findByUid(String uid);
}
