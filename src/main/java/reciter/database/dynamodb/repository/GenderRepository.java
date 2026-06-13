package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.Gender;

@Repository
public class GenderRepository extends DynamoDbCrudRepository<Gender, String> {

    public GenderRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, Gender.class);
    }
}
