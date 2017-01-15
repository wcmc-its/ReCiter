package reciter.database.mongo.repository;

import com.mongodb.WriteResult;

import reciter.database.mongo.model.ESearchPmid;
import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultRepositoryCustom {

	boolean pushESearchResult(ESearchResult eSearchResult);

	WriteResult update(String uid, ESearchPmid eSearchPmid);

	boolean existByUidAndRetrievalStrategyName(String uid, String retrievalStrategyName);
}
