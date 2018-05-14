package reciter.service.dynamo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.repository.ESearchResultRepository;
import reciter.service.ESearchResultService;

@Service("eSearchResultService")
public class ESearchResultServiceImpl implements ESearchResultService {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ESearchResultServiceImpl.class);

    @Autowired
    private ESearchResultRepository eSearchResultRepository;

    @Override
    public void save(ESearchResult eSearchResult) {
        eSearchResultRepository.save(eSearchResult);
    }

    @Override
    public ESearchResult findByUid(String uid) throws EmptyResultDataAccessException {
        return eSearchResultRepository.findOne(uid);
    }

    @Override
    public boolean pushESearchResult(ESearchResult eSearchResult) {
        return false;
    }

    @Override
    public void update(ESearchResult eSearchResult) {

    }

    @Override
    public boolean existByUidAndRetrievalStrategyName(String uid, String eSearchPmidretrievalStrategyName) {
        return false;
    }

	@Override
	public void deleteAll() {
		eSearchResultRepository.deleteAll();
	}

	@Override
	public void delete(String uid) {
		eSearchResultRepository.delete(uid);
		slf4jLogger.debug("Deleted search result for " + uid);
	}

}
