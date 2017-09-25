package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.repository.ESearchResultRepository;
import reciter.service.ESearchResultService;

import java.util.List;

@Service("eSearchResultService")
public class ESearchResultServiceImpl implements ESearchResultService {

    @Autowired
    private ESearchResultRepository eSearchResultRepository;

    @Override
    public void save(ESearchResult eSearchResult) {
        eSearchResultRepository.save(eSearchResult);
    }

    @Override
    public List<ESearchResult> findByUid(String uid) {
        return null;
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
}
