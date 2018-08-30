package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@DynamoDBTable(tableName = "InstitutionAfid")
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class InstitutionAfid {
	
	@DynamoDBHashKey(attributeName = "institution")
    private String institution;
	@DynamoDBAttribute(attributeName = "afids")
    private List<String> afids;
}
