package reciter.service.mongo;

import reciter.engine.erroranalysis.Analysis;

public interface ReCiterAnalysisService {

	Analysis runAnalysis(String cwid);
}
