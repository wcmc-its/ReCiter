package reciter.database.mongo.repository;

import reciter.database.mongo.model.ESearchResult;

public interface ESearchResultRepositoryCustom {

	boolean pushESearchResult(ESearchResult eSearchResult);
}
