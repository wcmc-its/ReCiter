package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.GoldStandard;

@Repository
public interface DynamoDbGoldStandardRepository extends CrudRepository<GoldStandard, String> {
}
