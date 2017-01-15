package reciter.service.mongo;

import java.util.Collection;
import java.util.List;

import reciter.model.identity.Identity;

public interface IdentityService {

	void save(Collection<Identity> identities);
	
	void save(Identity identity);
	
	List<Identity> findByUids(List<String> uids);
	
	Identity findByUid(String uid);
}
