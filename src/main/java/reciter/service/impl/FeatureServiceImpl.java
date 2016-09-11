package reciter.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.Feature;
import reciter.database.mongo.repository.FeatureRepository;
import reciter.service.FeatureService;

@Service("featureService")
public class FeatureServiceImpl implements FeatureService {

	@Autowired
	private FeatureRepository featureRepository;
	
	@Override
	public void save(List<Feature> features) {
		featureRepository.save(features);
	}

}
