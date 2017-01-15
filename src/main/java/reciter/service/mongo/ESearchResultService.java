package reciter.service.mongo;

import java.util.List;

import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultService {

	void save(ESearchResult eSearchResult);

	List<ESearchResult> findByUid(String uid);

	boolean pushESearchResult(ESearchResult eSearchResult);

	void update(ESearchResult eSearchResult);
	
	boolean existByUidAndRetrievalStrategyName(String uid, String eSearchPmidretrievalStrategyName);
}
