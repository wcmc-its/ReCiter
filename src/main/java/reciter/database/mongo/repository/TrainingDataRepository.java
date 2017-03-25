package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.TrainingData;

public interface TrainingDataRepository extends MongoRepository<TrainingData, String>{

}
