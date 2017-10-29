package reciter.database.dynamodb.repository;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;
import reciter.database.dynamodb.model.MeshTerm;

@EnableScan
public interface DynamoMeshTermRepository extends CrudRepository<MeshTerm, String> {
}
