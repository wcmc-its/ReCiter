package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.ESearchCount;

@Repository
public class ESearchCountRepository extends DynamoDbCrudRepository<ESearchCount, String> {

    public ESearchCountRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, ESearchCount.class);
    }
}
