package reciter.service.dynamo;

import java.util.List;

import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import reciter.database.dynamodb.model.ExternalArticle;
import reciter.service.ExternalArticleService;

@Service
public class ExternalArticleServiceImpl implements ExternalArticleService {

    private final DynamoDBMapper dynamoDBMapper;

    public ExternalArticleServiceImpl(AmazonDynamoDB amazonDynamoDB) {
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
    }

    @Override
    public void save(ExternalArticle externalArticle) {
        dynamoDBMapper.save(externalArticle);
    }

    @Override
    public List<ExternalArticle> findByUid(String uid) {
        ExternalArticle hashKey = new ExternalArticle();
        hashKey.setUid(uid);
        DynamoDBQueryExpression<ExternalArticle> queryExpression =
                new DynamoDBQueryExpression<ExternalArticle>()
                        .withHashKeyValues(hashKey)
                        .withConsistentRead(false);
        return dynamoDBMapper.query(ExternalArticle.class, queryExpression);
    }

    @Override
    public ExternalArticle find(String uid, String articleId) {
        return dynamoDBMapper.load(ExternalArticle.class, uid, articleId);
    }

    @Override
    public void delete(String uid, String articleId) {
        ExternalArticle key = new ExternalArticle();
        key.setUid(uid);
        key.setArticleId(articleId);
        dynamoDBMapper.delete(key);
    }
}
