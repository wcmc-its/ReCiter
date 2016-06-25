package reciter.database.mongo.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.repository.ESearchResultRepositoryCustom;

public class ESearchResultRepositoryImpl implements ESearchResultRepositoryCustom {

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Override
	public boolean pushESearchResult(ESearchResult eSearchResult) {
		String cwid = eSearchResult.getCwid();
		List<Long> pmids = eSearchResult.getPmids();
		BasicDBObject basicDbObject = new BasicDBObject("$addToSet", new BasicDBObject("pmids", new BasicDBObject("$each", pmids)));
		WriteResult writeResult = mongoTemplate.getCollection("esearchresult").update(new BasicDBObject("cwid", cwid), basicDbObject, true, false);
		System.out.println("writeResult=[" + writeResult.wasAcknowledged() + "].");
		return writeResult.wasAcknowledged();
	}
}
