package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.Identity;

public interface IdentityRepository extends MongoRepository<Identity, String> {

}
