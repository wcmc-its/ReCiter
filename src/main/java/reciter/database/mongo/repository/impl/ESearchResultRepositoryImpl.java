package reciter.database.mongo.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.repository.ESearchResultRepositoryCustom;

public class ESearchResultRepositoryImpl implements ESearchResultRepositoryCustom {

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Override
	public boolean pushESearchResult(ESearchResult eSearchResult) {
		return false;
//		String cwid = eSearchResult.getCwid();
//		ESearchPmid eSearchPmid = eSearchResult.getEsearchResult();
//		// Insert each ESearchPmid object into the collection.
//		BasicDBObject basicDbObject = new BasicDBObject("$addToSet", new BasicDBObject("eSearchPmid", new BasicDBObject("$each", eSearchPmid)));
//		WriteResult writeResult = mongoTemplate.getCollection("esearchresult").update(new BasicDBObject("cwid", cwid), basicDbObject, true, false);
//		return writeResult.wasAcknowledged();
	}
}
