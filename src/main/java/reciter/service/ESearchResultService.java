package reciter.service;

import java.util.List;

import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultService {

	void save(ESearchResult eSearchResult);

	List<ESearchResult> findByCwid(String cwid);

	boolean pushESearchResult(ESearchResult eSearchResult);
}
