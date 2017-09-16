package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import reciter.database.dynamodb.model.GoldStandard;

public interface GoldStandardRepository extends CrudRepository<GoldStandard, String> {
}
