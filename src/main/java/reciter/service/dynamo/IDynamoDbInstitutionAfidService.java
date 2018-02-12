package reciter.service.dynamo;

import reciter.database.dynamodb.model.InstitutionAfid;

import java.util.Collection;

public interface IDynamoDbInstitutionAfidService {
    void save(Collection<InstitutionAfid> institutionAfids);
    InstitutionAfid findByInstitution(String institution);
}
