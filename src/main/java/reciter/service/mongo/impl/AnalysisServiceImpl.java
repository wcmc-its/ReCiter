package reciter.service.mongo.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.AnalysisMongo;
import reciter.database.mongo.repository.AnalysisRepository;
import reciter.engine.erroranalysis.Analysis;
import reciter.service.mongo.AnalysisService;

@Service("analysisService")
public class AnalysisServiceImpl implements AnalysisService {

	@Autowired
	private AnalysisRepository analysisRepository;
	
	@Override
	public void save(Analysis analysis, String cwid) {
		AnalysisMongo analysisMongo = new AnalysisMongo();
		analysisMongo.setCwid(cwid);
		analysisMongo.setAnalysis(analysis);
		analysisRepository.save(analysisMongo);
	}
}
