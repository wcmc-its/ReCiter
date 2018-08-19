package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@DynamoDBTable(tableName = "ScienceMetrixDepartmentCategory")
@NoArgsConstructor
@AllArgsConstructor
public class ScienceMetrixDepartmentCategory {
	
	private Long pk;
	private double logOddsRatio;
	private String primaryDepartment;
	private String scienceMetrixJournalSubfield;
	private Long scienceMetrixJournalSubfieldId;
	
	@DynamoDBHashKey(attributeName = "pk")
	public Long getPk() {
		return pk;
	}
	
	public void setPk(Long pk) {
		this.pk = pk;
	}
	
	@DynamoDBAttribute(attributeName = "logOddsRatio")
	public double getLogOddsRatio() {
		return logOddsRatio;
	}
	public void setLogOddsRatio(double logOddsRatio) {
		this.logOddsRatio = logOddsRatio;
	}
	
	@DynamoDBAttribute(attributeName = "primaryDepartment")
	public String getPrimaryDepartment() {
		return primaryDepartment;
	}
	public void setPrimaryDepartment(String primaryDepartment) {
		this.primaryDepartment = primaryDepartment;
	}
	
	@DynamoDBAttribute(attributeName = "scienceMetrixJournalSubfield")
	public String getScienceMetrixJournalSubfield() {
		return scienceMetrixJournalSubfield;
	}
	public void setScienceMetrixJournalSubfield(String scienceMetrixJournalSubfield) {
		this.scienceMetrixJournalSubfield = scienceMetrixJournalSubfield;
	}
	
	@DynamoDBAttribute(attributeName = "scienceMetrixJournalSubfieldId")
	public Long getScienceMetrixJournalSubfieldId() {
		return scienceMetrixJournalSubfieldId;
	}
	public void setScienceMetrixJournalSubfieldId(Long scienceMetrixJournalSubfieldId) {
		this.scienceMetrixJournalSubfieldId = scienceMetrixJournalSubfieldId;
	}
	
	
}
