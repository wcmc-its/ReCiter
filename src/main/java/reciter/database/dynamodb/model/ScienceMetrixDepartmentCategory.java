package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@DynamoDBTable(tableName = "ScienceMetrixDepartmentCategory")
@NoArgsConstructor
@AllArgsConstructor
public class ScienceMetrixDepartmentCategory {
	
	@CsvBindByPosition(position=0)
	@DynamoDBHashKey(attributeName = "pk")
	private int pk;
	
	@CsvBindByPosition(position=1)
	@DynamoDBAttribute(attributeName = "logOddsRatio")
	private double logOddsRatio;
	
	@CsvBindByPosition(position=2)
	@DynamoDBAttribute(attributeName = "primaryDepartment")
	private String primaryDepartment;
	
	@CsvBindByPosition(position=3)
	@DynamoDBAttribute(attributeName = "scienceMetrixJournalSubfield")
	private String scienceMetrixJournalSubfield;
	
	@CsvBindByPosition(position=4)
	@DynamoDBAttribute(attributeName = "scienceMetrixJournalSubfieldId")
	private int scienceMetrixJournalSubfieldId;
	
}
