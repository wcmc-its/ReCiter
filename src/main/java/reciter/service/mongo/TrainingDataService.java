package reciter.service.mongo;

import java.util.List;

import reciter.database.mongo.model.TrainingData;

public interface TrainingDataService {

	List<TrainingData> findAll();
}
