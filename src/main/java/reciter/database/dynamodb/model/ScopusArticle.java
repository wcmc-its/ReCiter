package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;

@DynamoDBTable(tableName = "ScopusArticle")
@AllArgsConstructor
public class ScopusArticle {

    private long pmid;
    private reciter.model.scopus.ScopusArticle scopusArticle;

    @DynamoDBHashKey(attributeName = "pmid")
    public long getPmid() {
        return pmid;
    }

    @DynamoDBAttribute(attributeName = "scopusarticle")
    public reciter.model.scopus.ScopusArticle getScopusArticle() {
        return scopusArticle;
    }

    public void setPmid(long pmid) {
        this.pmid = pmid;
    }

    public void setScopusArticle(reciter.model.scopus.ScopusArticle scopusArticle) {
        this.scopusArticle = scopusArticle;
    }
}
