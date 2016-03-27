package reciter.database.dao;

import java.util.List;

import reciter.database.model.Analysis;

public interface AnalysisDao {

	void emptyTable();
	void insertAnalysisList(List<Analysis> analysisList);
}
