package reciter.service.dynamo;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dynamodb.repository.OrcidRepository;
import reciter.service.OrcidService;

@Service("orcidService")
public class OrcidServiceImpl implements OrcidService {

	private static final Logger log = LoggerFactory.getLogger(OrcidServiceImpl.class);

	@Autowired
	private OrcidRepository orcidRepository;

	@Override
	public Map<String, String> getAllOrcids() {
		log.info("Retrieving all ORCIDs from DynamoDB AdminOrcid table");

		Map<String, String> orcidMap = StreamSupport.stream(orcidRepository.findAll().spliterator(), false)
				.filter(orcid -> orcid.getPersonIdentifier() != null && !orcid.getPersonIdentifier().trim().isEmpty())
				.filter(orcid -> orcid.getOrcid() != null && !orcid.getOrcid().trim().isEmpty()).collect(HashMap::new,
						(map, orcid) -> map.put(orcid.getPersonIdentifier().trim(), orcid.getOrcid().trim()),
						HashMap::putAll);

		log.info("Retrieved {} ORCIDs from DynamoDB", orcidMap.size());
		return orcidMap;
	}

}
