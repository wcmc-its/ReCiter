package reciter.service.dynamo;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.repository.ESearchResultRepository;
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
    public ESearchResult findByUid(String uid) {
        return eSearchResultRepository.findById(uid).orElseGet(() -> null);
    }


	@Override
	public void deleteAll() {
		eSearchResultRepository.deleteAll();
	}

	@Override
	public void delete(String uid) {
		eSearchResultRepository.deleteById(uid);
	}

	@Override
	public void stampLastFullSweepIfNewer(String uid, Instant sweepTime) {
		eSearchResultRepository.stampLastFullSweepIfNewer(uid, sweepTime);
	}

	@Override
	public Instant findLastFullSweep(String uid) {
		return eSearchResultRepository.findLastFullSweep(uid);
	}

}
