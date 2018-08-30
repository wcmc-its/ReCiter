package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@DynamoDBTable(tableName = "PubMedArticle")
public class PubMedArticle {
	
	@DynamoDBHashKey(attributeName = "pmid")
    private Long pmid;
	@DynamoDBAttribute(attributeName = "pubmedarticle")
    private reciter.model.pubmed.PubMedArticle pubMedArticle;

    public PubMedArticle() {}

    public PubMedArticle(Long pmid, reciter.model.pubmed.PubMedArticle pubMedArticle) {
        this.pmid = pmid;
        this.pubMedArticle = pubMedArticle;
    }
}
