package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.NameFrequency;

@Repository
public class NameFrequencyRepository extends DynamoDbCrudRepository<NameFrequency, String> {

    public NameFrequencyRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, NameFrequency.class);
    }
}
