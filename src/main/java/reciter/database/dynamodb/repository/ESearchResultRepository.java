package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.ESearchResult;

@Repository
public class ESearchResultRepository extends DynamoDbCrudRepository<ESearchResult, String> {

    public ESearchResultRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, ESearchResult.class);
    }
}
