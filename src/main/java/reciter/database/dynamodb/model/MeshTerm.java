package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DynamoDBTable(tableName = "MeshTerm")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeshTerm {
	
	@DynamoDBHashKey(attributeName = "mesh")
    private String mesh;
	@DynamoDBAttribute(attributeName = "count")
    private long count;
}
