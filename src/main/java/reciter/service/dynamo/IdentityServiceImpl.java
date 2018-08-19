package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.repository.IdentityRepository;
import reciter.model.identity.Identity;
import reciter.service.IdentityService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Primary
@Service
public class IdentityServiceImpl implements IdentityService {

    @Autowired
    private IdentityRepository identityRepository;

    @Override
    public void save(Collection<Identity> identities) {
        List<reciter.database.dynamodb.model.Identity> identitiesDynamos = new ArrayList<>();
        for (Identity identity : identities) {
            reciter.database.dynamodb.model.Identity identityDynamo = new reciter.database.dynamodb.model.Identity(
                    identity.getUid(), identity
            );
            identitiesDynamos.add(identityDynamo);
        }
        identityRepository.saveAll(identitiesDynamos);
    }

    @Override
    public void save(Identity identity) {
        reciter.database.dynamodb.model.Identity identityDynamo = new reciter.database.dynamodb.model.Identity(
                identity.getUid(), identity
        );
        identityRepository.save(identityDynamo);
    }

    @Override
    public List<Identity> findByUids(List<String> uids) {
        List<Identity> identities = new ArrayList<>();
        identityRepository.findAllById(uids).forEach(e -> identities.add(e.getIdentity()));
        return identities;
    }

    @Override
    public Identity findByUid(String uid) {
        reciter.database.dynamodb.model.Identity identity = identityRepository.findById(uid).orElseGet(() -> null);
        if (identity != null) {
            return identity.getIdentity();
        }
        return null;
    }

    @Override
    public List<Identity> findAll() {
        Iterable<reciter.database.dynamodb.model.Identity> it = identityRepository.findAll();
        List<Identity> identities = new ArrayList<>();
        Iterator<reciter.database.dynamodb.model.Identity> iterator = it.iterator();
        while (iterator.hasNext()) {
            Identity identity = iterator.next().getIdentity();
            System.out.println("getting identity:" + identity.getUid());
            identities.add(identity);
        }
        return identities;
    }

    @Override
    public void deleteAll() {
        identityRepository.deleteAll();
    }

    @Override
    public void delete(String uid) {
        identityRepository.deleteById(uid);
    }
}
