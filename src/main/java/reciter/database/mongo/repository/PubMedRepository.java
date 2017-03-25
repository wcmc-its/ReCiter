package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.PubMedArticleMongo;

public interface PubMedRepository extends MongoRepository<PubMedArticleMongo, Long> {

}
