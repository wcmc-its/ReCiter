package reciter.database.mongo.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

import reciter.database.mongo.model.ESearchPmid;
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

	@Override
	public WriteResult update(String cwid, ESearchPmid eSearchPmid) {
		
		BasicDBObject query = new BasicDBObject("cwid", cwid).append("eSearchPmid.retrievalStrategyName", eSearchPmid.getRetrievalStrategyName());
		BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("eSearchPmid.pmids", eSearchPmid.getPmids()));
		
		WriteResult writeResult = mongoTemplate.getCollection("esearchresult").update(query, update, true, false);
		return writeResult;
	}
	
	@Override
	public boolean existByCwidAndRetrievalStrategyName(String cwid, String retrievalStrategyName) {
		BasicDBObject query = new BasicDBObject("cwid", cwid).append("eSearchPmid.retrievalStrategyName", retrievalStrategyName);
		return mongoTemplate.getCollection("esearchresult").findOne(query) != null;
	}
}
