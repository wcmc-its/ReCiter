package reciter.service;

import java.util.List;

import reciter.service.bean.AnalysisBean;

public interface AnalysisService {

	void emptyTable();
	void insertAnalysisList(List<AnalysisBean> analysisList);
}
