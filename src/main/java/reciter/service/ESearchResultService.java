package reciter.service;

import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultService {

	void save(ESearchResult eSearchResult);

	ESearchResult findByCwid(String cwid);

	boolean pushESearchResult(ESearchResult eSearchResult);
}
