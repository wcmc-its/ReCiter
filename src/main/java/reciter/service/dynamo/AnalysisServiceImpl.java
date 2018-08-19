package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.repository.AnalysisOutputRepository;
import reciter.service.AnalysisService;

@Service("AnalysisOutputService")
public class AnalysisServiceImpl implements AnalysisService{
	
	 @Autowired
	private AnalysisOutputRepository analysisOutputRepository;

	@Override
	public void save(AnalysisOutput analysis) {
		analysisOutputRepository.save(analysis);
	}

	@Override
	public AnalysisOutput findByUid(String uid) {
		return analysisOutputRepository.findById(uid).orElseGet(() -> null);
	}

	@Override
	public void deleteAll() {
		 analysisOutputRepository.deleteAll();
	}

	@Override
	public void delete(String uid) {
		analysisOutputRepository.deleteById(uid);
	}
	

}
