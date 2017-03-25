package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.AnalysisMongo;

public interface AnalysisRepository extends MongoRepository<AnalysisMongo, String> {

}
