package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "ESearchResult")
public class ESearchResult {
	
	@DynamoDBHashKey(attributeName = "uid")
    private String uid;
	@DynamoDBAttribute(attributeName = "retrievalDate")
    private Date retrievalDate;
    @DynamoDBAttribute(attributeName = "esearchpmids")
    private List<ESearchPmid> eSearchPmids;
}
