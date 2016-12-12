package reciter.service.impl.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.GoldStandard;
import reciter.database.mongo.repository.GoldStandardRepository;
import reciter.service.GoldStandardService;

@Service("goldStandardService")
public class GoldStandardServiceImpl implements GoldStandardService {

	@Autowired
	private GoldStandardRepository goldStandardRepository;
	
	@Override
	public void save(GoldStandard goldStandard) {
		goldStandardRepository.save(goldStandard);
	}
	
	
}
