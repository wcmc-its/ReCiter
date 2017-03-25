package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.IdentityMongo;

public interface IdentityRepository extends MongoRepository<IdentityMongo, String> {

}
