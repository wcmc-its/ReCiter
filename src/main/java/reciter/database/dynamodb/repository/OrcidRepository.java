package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.AdminOrcid;

@Repository
public class OrcidRepository extends DynamoDbCrudRepository<AdminOrcid, String> {

    public OrcidRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, AdminOrcid.class);
    }
}
