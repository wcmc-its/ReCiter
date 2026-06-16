package reciter.service.dynamo;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dynamodb.model.PmidProvenance;
import reciter.database.dynamodb.repository.PmidProvenanceRepository;
import reciter.service.PmidProvenanceService;

@Service
public class PmidProvenanceServiceImpl implements PmidProvenanceService {

	private static final Logger log = LoggerFactory.getLogger(PmidProvenanceServiceImpl.class);

	@Autowired
	private PmidProvenanceRepository pmidProvenanceRepository;

	@Override
	public void save(PmidProvenance pmidProvenance) {
		pmidProvenanceRepository.save(pmidProvenance);
	}

	@Override
	public void saveIfNotExists(PmidProvenance pmidProvenance) {
		pmidProvenanceRepository.saveIfNotExists(pmidProvenance);
	}

	@Override
	public void saveAllIfNotExists(List<PmidProvenance> pmidProvenances) {
		pmidProvenanceRepository.saveAllIfNotExists(pmidProvenances);
	}

	@Override
	public List<PmidProvenance> findByUid(String uid) {

		return pmidProvenanceRepository.findByUid(uid);
	}

	@Override
	public Set<Long> findPmidsByUid(String uid) {
		return pmidProvenanceRepository.findPmidsByUid(uid);
	}

	@Override
	public Set<Long> findPmidsByUidAndStrategy(String uid, String strategy) {
		return pmidProvenanceRepository.findPmidsByUidAndStrategy(uid, strategy);
	}

	@Override
	public void updateStrategyIfBackfill(String uid, long pmid, String realStrategy) {
		pmidProvenanceRepository.updateStrategyIfBackfill(uid, pmid, realStrategy);
	}
}
