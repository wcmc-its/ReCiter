package reciter.service;

import reciter.engine.erroranalysis.Analysis;

public interface ReCiterAnalysisService {

	Analysis runAnalysis(String cwid);
}
