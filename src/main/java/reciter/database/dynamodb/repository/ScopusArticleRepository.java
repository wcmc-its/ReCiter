package reciter.database.dynamodb.repository;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import reciter.database.dynamodb.model.ScopusArticle;

@Repository
public class ScopusArticleRepository extends DynamoDbCrudRepository<ScopusArticle, String> {

    public ScopusArticleRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, ScopusArticle.class);
    }
}
