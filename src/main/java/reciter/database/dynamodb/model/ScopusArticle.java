package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@DynamoDBTable(tableName = "ScopusArticle")
public class ScopusArticle {
	
	@DynamoDBHashKey(attributeName = "id")
    private String id;
	 @DynamoDBAttribute(attributeName = "scopusarticle")
    private reciter.model.scopus.ScopusArticle scopusArticle;

    public ScopusArticle() {}

    public ScopusArticle(String id, reciter.model.scopus.ScopusArticle scopusArticle) {
        this.id = id;
        this.scopusArticle = scopusArticle;
    }
}
