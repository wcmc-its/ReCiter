package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBTable(tableName = "ScienceMetrix")
@NoArgsConstructor
@AllArgsConstructor
public class ScienceMetrix {
	
	@DynamoDBHashKey(attributeName = "smsid")
    private Long smsid;
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "eissn-index")
    @DynamoDBAttribute(attributeName = "eissn")
    private String eissn;
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "issn-index")
    @DynamoDBAttribute(attributeName = "issn")
    private String issn;
	@DynamoDBAttribute(attributeName = "publicationName")
    private String publicationName;
	@DynamoDBAttribute(attributeName = "scienceMatrixSubfieldId")
    private String scienceMatrixSubfieldId;
	@DynamoDBAttribute(attributeName = "scienceMetrixDomain")
    private String scienceMetrixDomain;
	@DynamoDBAttribute(attributeName = "scienceMetrixField")
    private String scienceMetrixField;
	@DynamoDBAttribute(attributeName = "scienceMetrixSubfield")
    private String scienceMetrixSubfield;

}
