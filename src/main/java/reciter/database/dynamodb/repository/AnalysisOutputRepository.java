package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.AnalysisOutput;

@Repository
public class AnalysisOutputRepository extends DynamoDbCrudRepository<AnalysisOutput, String> {

    public AnalysisOutputRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, AnalysisOutput.class);
    }
}
