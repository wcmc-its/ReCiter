package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.Feature;

public interface FeatureRepository extends MongoRepository<Feature, String> {

}
