package reciter.service;

import java.util.List;

import reciter.database.mongo.model.Identity;

public interface IdentityService {
	void save(List<Identity> identities);
	Identity save(Identity identity);
	Identity findByCwid(String cwid);
	List<Identity> findByCwidRegex(String search);
}
