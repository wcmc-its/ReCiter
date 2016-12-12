package reciter.service.impl.local;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.TrainingData;
import reciter.database.mongo.repository.TrainingDataRepository;
import reciter.service.TrainingDataService;

@Service("trainingDataService")
public class TrainingDataServiceImpl implements TrainingDataService {

	@Autowired
	private TrainingDataRepository trainingDataRepository;
	
	@Override
	public List<TrainingData> findAll() {
		return trainingDataRepository.findAll();
	}
}
