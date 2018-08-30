package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBTable(tableName = "ScienceMetrixDepartmentCategory")
@NoArgsConstructor
@AllArgsConstructor
public class ScienceMetrixDepartmentCategory {
	
	@DynamoDBHashKey(attributeName = "pk")
	private Long pk;
	@DynamoDBAttribute(attributeName = "logOddsRatio")
	private double logOddsRatio;
	@DynamoDBAttribute(attributeName = "primaryDepartment")
	private String primaryDepartment;
	@DynamoDBAttribute(attributeName = "scienceMetrixJournalSubfield")
	private String scienceMetrixJournalSubfield;
	@DynamoDBAttribute(attributeName = "scienceMetrixJournalSubfieldId")
	private Long scienceMetrixJournalSubfieldId;
	
}
