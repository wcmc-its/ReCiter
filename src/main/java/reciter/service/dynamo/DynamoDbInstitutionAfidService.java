package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.model.InstitutionAfid;
import reciter.database.dynamodb.repository.DynamoDbInstitutionAfidRepository;

import java.util.Collection;

@Service("dynamoDbInstitutionAfidService")
public class DynamoDbInstitutionAfidService implements IDynamoDbInstitutionAfidService {

    @Autowired
    private DynamoDbInstitutionAfidRepository dynamoDbInstitutionAfidRepository;

    @Override
    public void save(Collection<InstitutionAfid> institutionAfids) {
        dynamoDbInstitutionAfidRepository.save(institutionAfids);
    }
}
