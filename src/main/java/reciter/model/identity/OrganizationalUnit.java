package reciter.model.identity;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@DynamoDBDocument
public class OrganizationalUnit {
	
	private String organizationalUnitLabel;
	private String organizationalUnitType;
	
	public OrganizationalUnit() {
		
	}
	
	public OrganizationalUnit(String organizationalUnitLabel, String organizationalUnitType) {
		this.organizationalUnitLabel = organizationalUnitLabel;
		this.organizationalUnitType = organizationalUnitType;
	}
	
	public String getOrganizationalUnitLabel() {
		return organizationalUnitLabel;
	}
	public void setOrganizationalUnitLabel(String organizationalUnitLabel) {
		this.organizationalUnitLabel = organizationalUnitLabel;
	}
	public String getOrganizationalUnitType() {
		return organizationalUnitType;
	}
	public void setOrganizationalUnitType(String organizationalUnitType) {
		this.organizationalUnitType = organizationalUnitType;
	}
	
	

}
