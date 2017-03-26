package reciter.service.mongo;

import java.util.List;

public interface InstitutionAfidService {

	List<Integer> getAfidByInstitution(String institution);
}
