package reciter.service.impl;

import java.util.List;

import org.bson.Document;

import reciter.database.mongo.ESearchResultDao;
import reciter.database.mongo.impl.ESearchResultDaoImpl;
import reciter.service.ESearchResultService;

public class ESearchResultServiceImpl implements ESearchResultService {

	@Override
	public void insertESearchResult(String cwid, List<String> pmids) {
		ESearchResultDao eSearchResultDao = new ESearchResultDaoImpl();
		Document doc = new Document("cwid", cwid).append("pmids", pmids);
		eSearchResultDao.insertESearchResult(doc);
	}

}
