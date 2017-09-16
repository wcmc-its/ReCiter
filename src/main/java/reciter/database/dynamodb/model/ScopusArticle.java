package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

@DynamoDBTable(tableName = "ScopusArticle")
@Data
@AllArgsConstructor
public class ScopusArticle {

    private final long pmid;
    private reciter.model.scopus.ScopusArticle scopusArticle;

    @DynamoDBHashKey(attributeName = "pmid")
    public long getPmid() {
        return pmid;
    }

    @DynamoDBAttribute(attributeName = "scopusarticle")
    public reciter.model.scopus.ScopusArticle getScopusArticle() {
        return scopusArticle;
    }
}
