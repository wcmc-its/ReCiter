package reciter.service;

import java.util.List;

import reciter.service.dto.AnalysisDTO;

public interface AnalysisService {

	void emptyTable();
	void insertAnalysisList(List<AnalysisDTO> analysisList);
}
