package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultRepository extends MongoRepository<ESearchResult, String> {

}
