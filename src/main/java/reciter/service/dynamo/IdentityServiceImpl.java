package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.repository.IdentityRepository;
import reciter.model.identity.Identity;
import reciter.service.IdentityService;

import java.util.ArrayList;
import java.util.Collection;
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
        identityRepository.save(identitiesDynamos);
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
        return null;
    }

    @Override
    public Identity findByUid(String uid) {
        return null;
    }
}
