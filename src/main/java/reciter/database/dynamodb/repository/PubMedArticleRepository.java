package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.PubMedArticle;

@Repository
public class PubMedArticleRepository extends DynamoDbCrudRepository<PubMedArticle, Long> {

    public PubMedArticleRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, PubMedArticle.class);
    }
}
