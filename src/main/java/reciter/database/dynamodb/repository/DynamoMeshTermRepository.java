package reciter.database.dynamodb.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.MeshTerm;

@Repository
public interface DynamoMeshTermRepository extends CrudRepository<MeshTerm, String> {
}
