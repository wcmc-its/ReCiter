package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.MeshTerm;

@Repository
public class DynamoMeshTermRepository extends DynamoDbCrudRepository<MeshTerm, String> {

    public DynamoMeshTermRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, MeshTerm.class);
    }
}
