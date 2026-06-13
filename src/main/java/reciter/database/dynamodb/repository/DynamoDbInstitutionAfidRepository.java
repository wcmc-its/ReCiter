package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.InstitutionAfid;

@Repository
public class DynamoDbInstitutionAfidRepository extends DynamoDbCrudRepository<InstitutionAfid, String> {

    public DynamoDbInstitutionAfidRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, InstitutionAfid.class);
    }
}
