package reciter.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.repository.ESearchResultRepository;
import reciter.service.ESearchResultService;

@Service("eSearchResultService")
public class ESearchResultServiceImpl implements ESearchResultService {

	@Autowired
	private ESearchResultRepository eSearchResultRepository;
	
	@Override
	public void save(ESearchResult eSearchResult) {
		eSearchResultRepository.save(eSearchResult);
	}

	@Override
	public ESearchResult findByCwid(String cwid) {
		return eSearchResultRepository.findByCwid(cwid);
	}
	
	@Override
	public boolean pushESearchResult(ESearchResult eSearchResult) {
		return eSearchResultRepository.pushESearchResult(eSearchResult);
	}
	
//	@Override
//	public void insertESearchResult(String cwid, List<String> pmids) {
//		ESearchResultRepository eSearchResultDao = new ESearchResultDataAccessImpl();
//		Document doc = new Document("cwid", cwid).append("pmids", pmids);
//		eSearchResultDao.insertESearchResult(doc);
//	}

}
