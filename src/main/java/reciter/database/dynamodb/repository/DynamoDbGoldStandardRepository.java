package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.GoldStandard;

@Repository
public class DynamoDbGoldStandardRepository extends DynamoDbCrudRepository<GoldStandard, String> {

    public DynamoDbGoldStandardRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, GoldStandard.class);
    }
}
