package reciter.service.mongo.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.mongo.model.IdentityMongo;
import reciter.database.mongo.repository.IdentityRepository;
import reciter.model.identity.Identity;
import reciter.service.mongo.IdentityService;

@Service("identityService")
public class IdentityServiceImpl implements IdentityService {

	@Autowired
	private IdentityRepository identityRepository;

	@Override
	public void save(Collection<Identity> identities) {
		List<IdentityMongo> identityMongos = new ArrayList<>();
		for (Identity identity : identities) {
			IdentityMongo identityMongo = new IdentityMongo();
			identityMongo.setId(identity.getUid());
			identityMongo.setIdentity(identity);
			identityMongos.add(identityMongo);
		}
		identityRepository.save(identityMongos);
	}
	
	@Override
	public void save(Identity identity) {
		IdentityMongo identityMongo = new IdentityMongo();
		identityMongo.setId(identity.getUid());
		identityMongo.setIdentity(identity);
		identityRepository.save(identityMongo);
	}

	@Override
	public List<Identity> findByUids(List<String> uids) {
		Iterator<IdentityMongo> iterator = identityRepository.findAll(uids).iterator();
		List<Identity> identities = new ArrayList<>(uids.size());
		while (iterator.hasNext()) {
			identities.add(iterator.next().getIdentity());
		}
		return identities;
	}

	@Override
	public Identity findByUid(String uid) {
		IdentityMongo identityMongo = identityRepository.findOne(uid);
		return identityMongo.getIdentity();
	}
}
