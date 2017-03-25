package reciter.database.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import reciter.database.mongo.model.PubMedArticleFeature;

public interface PubMedArticleFeatureRepository extends MongoRepository<PubMedArticleFeature, String> {

}
