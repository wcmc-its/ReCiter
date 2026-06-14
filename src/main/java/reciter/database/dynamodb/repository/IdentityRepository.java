package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.Identity;

@Repository
public class IdentityRepository extends DynamoDbCrudRepository<Identity, String> {

    public IdentityRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, Identity.class);
    }
}
