package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.ApplicationUser;

@Repository
public class ApplicationUserRepository extends DynamoDbCrudRepository<ApplicationUser, String> {

    public ApplicationUserRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, ApplicationUser.class);
    }
}
