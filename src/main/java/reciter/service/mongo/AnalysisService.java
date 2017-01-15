package reciter.service.mongo;

import reciter.engine.erroranalysis.Analysis;

public interface AnalysisService {

	void save(Analysis analysis, String uid);
}
