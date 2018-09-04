package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.opencsv.bean.CsvBindByPosition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBTable(tableName = "ScienceMetrix")
@NoArgsConstructor
@AllArgsConstructor
public class ScienceMetrix {
	
	@CsvBindByPosition(position=0)
	@DynamoDBHashKey(attributeName = "smsid")
    private Long smsid;
	
	@CsvBindByPosition(position=3)
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "eissn-index")
    @DynamoDBAttribute(attributeName = "eissn")
    private String eissn;
	
	@CsvBindByPosition(position=2)
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "issn-index")
    @DynamoDBAttribute(attributeName = "issn")
    private String issn;
	
	@CsvBindByPosition(position=1)
	@DynamoDBAttribute(attributeName = "publicationName")
    private String publicationName;
	
	@CsvBindByPosition(position=7)
	@DynamoDBAttribute(attributeName = "scienceMatrixSubfieldId")
    private String scienceMatrixSubfieldId;
	
	@CsvBindByPosition(position=4)
	@DynamoDBAttribute(attributeName = "scienceMetrixDomain")
    private String scienceMetrixDomain;
	
	@CsvBindByPosition(position=5)
	@DynamoDBAttribute(attributeName = "scienceMetrixField")
    private String scienceMetrixField;
	
	@CsvBindByPosition(position=6)
	@DynamoDBAttribute(attributeName = "scienceMetrixSubfield")
    private String scienceMetrixSubfield;

}
