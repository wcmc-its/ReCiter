package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "PubMedArticle")
public class PubMedArticle {
    private long pmid;
    private reciter.model.pubmed.PubMedArticle pubMedArticle;

    public PubMedArticle() {}

    public PubMedArticle(long pmid, reciter.model.pubmed.PubMedArticle pubMedArticle) {
        this.pmid = pmid;
        this.pubMedArticle = pubMedArticle;
    }

    @DynamoDBHashKey(attributeName = "pmid")
    public long getPmid() {
        return pmid;
    }

    @DynamoDBAttribute(attributeName = "pubmedarticle")
    public reciter.model.pubmed.PubMedArticle getPubmedArticle() {
        return pubMedArticle;
    }

    public void setPmid(long pmid) {
        this.pmid = pmid;
    }

    public void setPubMedArticle(reciter.model.pubmed.PubMedArticle pubMedArticle) {
        this.pubMedArticle = pubMedArticle;
    }
}
