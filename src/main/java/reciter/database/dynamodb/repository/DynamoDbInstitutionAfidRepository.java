package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.InstitutionAfid;

@Repository
public interface DynamoDbInstitutionAfidRepository extends CrudRepository<InstitutionAfid, String> {
}
