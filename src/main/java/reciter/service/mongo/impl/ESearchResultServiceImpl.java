package reciter.service.mongo.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.ESearchResult;
import reciter.database.mongo.repository.ESearchResultRepository;
import reciter.service.mongo.ESearchResultService;

@Service("eSearchResultService")
public class ESearchResultServiceImpl implements ESearchResultService {

	@Autowired
	private ESearchResultRepository eSearchResultRepository;
	
	@Override
	public void save(ESearchResult eSearchResult) {
		eSearchResultRepository.save(eSearchResult);
	}
	
	@Override
	public void update(ESearchResult eSearchResult) {
		eSearchResultRepository.update(eSearchResult.getUid(), eSearchResult.getESearchPmid());
	}

	@Override
	public List<ESearchResult> findByUid(String uid) {
		return eSearchResultRepository.findByUid(uid);
	}
	
	@Override
	public boolean pushESearchResult(ESearchResult eSearchResult) {
		return eSearchResultRepository.pushESearchResult(eSearchResult);
	}

	@Override
	public boolean existByUidAndRetrievalStrategyName(String uid,
			String eSearchPmidretrievalStrategyName) {
		return eSearchResultRepository.existByUidAndRetrievalStrategyName(uid, eSearchPmidretrievalStrategyName);
	}
	
//	@Override
//	public void insertESearchResult(String uid, List<String> pmids) {
//		ESearchResultRepository eSearchResultDao = new ESearchResultDataAccessImpl();
//		Document doc = new Document("uid", uid).append("pmids", pmids);
//		eSearchResultDao.insertESearchResult(doc);
//	}

}
