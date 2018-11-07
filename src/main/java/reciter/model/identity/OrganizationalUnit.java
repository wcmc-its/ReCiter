package reciter.model.identity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;

@DynamoDBDocument
public class OrganizationalUnit {
	
	private String organizationalUnitLabel;
	@DynamoDBTyped(DynamoDBAttributeType.S)
	private OrganizationalUnitType organizationalUnitType;
	
	public OrganizationalUnit() {
		
	}
	
	public OrganizationalUnit(String organizationalUnitLabel, OrganizationalUnitType organizationalUnitType) {
		this.organizationalUnitLabel = organizationalUnitLabel;
		this.organizationalUnitType = organizationalUnitType;
	}
	
	public String getOrganizationalUnitLabel() {
		return organizationalUnitLabel;
	}
	public void setOrganizationalUnitLabel(String organizationalUnitLabel) {
		this.organizationalUnitLabel = organizationalUnitLabel;
	}
	public OrganizationalUnitType getOrganizationalUnitType() {
		return organizationalUnitType;
	}
	public void setOrganizationalUnitType(OrganizationalUnitType organizationalUnitType) {
		this.organizationalUnitType = organizationalUnitType;
	}
	
	public enum OrganizationalUnitType {
		DEPARTMENT,
		DIVISION,
		PROGRAM
	}
	
	

}
