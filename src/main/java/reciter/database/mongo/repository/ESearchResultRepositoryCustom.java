package reciter.database.mongo.repository;

import com.mongodb.WriteResult;

import reciter.database.mongo.model.ESearchPmid;
import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultRepositoryCustom {

	boolean pushESearchResult(ESearchResult eSearchResult);

	WriteResult update(String cwid, ESearchPmid eSearchPmid);

	boolean existByCwidAndRetrievalStrategyName(String cwid, String retrievalStrategyName);
}
