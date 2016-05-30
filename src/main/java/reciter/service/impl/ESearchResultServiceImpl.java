package reciter.service.impl;

import java.util.List;

import org.bson.Document;

import reciter.database.mongo.ESearchResultDataAccess;
import reciter.database.mongo.impl.ESearchResultDataAccessImpl;
import reciter.service.ESearchResultService;

public class ESearchResultServiceImpl implements ESearchResultService {

	@Override
	public void insertESearchResult(String cwid, List<String> pmids) {
		ESearchResultDataAccess eSearchResultDao = new ESearchResultDataAccessImpl();
		Document doc = new Document("cwid", cwid).append("pmids", pmids);
		eSearchResultDao.insertESearchResult(doc);
	}

}
