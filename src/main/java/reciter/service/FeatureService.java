package reciter.service;

import java.util.List;

import reciter.database.mongo.model.Feature;

public interface FeatureService {

	void save(List<Feature> features);
}
