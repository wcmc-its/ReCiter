package reciter.database.mongo.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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
		WriteResult writeResult = mongoTemplate.updateFirst(
	            Query.query(Criteria.where("cwid").is(cwid)), 
	            new Update().addToSet("pmids", pmids.toArray()), ESearchResult.class);
		
		return writeResult.wasAcknowledged();
	}
}
