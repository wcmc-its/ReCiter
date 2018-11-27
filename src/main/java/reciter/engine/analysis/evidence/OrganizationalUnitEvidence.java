package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.ToString;
import reciter.model.identity.OrganizationalUnit.OrganizationalUnitType;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
@DynamoDBDocument
public class OrganizationalUnitEvidence {
	
	private String identityOrganizationalUnit;
	private String articleAffiliation;
	@DynamoDBTyped(DynamoDBAttributeType.S)
	private OrganizationalUnitType organizationalUnitType;
	private double organizationalUnitMatchingScore;
	private String organizationalUnitModifier;
	private double organizationalUnitModifierScore;

}
