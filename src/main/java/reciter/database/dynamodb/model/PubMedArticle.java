package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

@DynamoDBTable(tableName = "PubMedArticle")
@Data
@AllArgsConstructor
public class PubMedArticle {
    private final long pmid;
    private final reciter.model.pubmed.PubMedArticle pubMedArticle;

    @DynamoDBHashKey(attributeName = "pmid")
    public long getPmid() {
        return pmid;
    }

    @DynamoDBAttribute(attributeName = "pubmedarticle")
    public reciter.model.pubmed.PubMedArticle getPubmedArticle() {
        return pubMedArticle;
    }
}
