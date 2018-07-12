package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@DynamoDBDocument
public class OrganizationalUnitEvidence {
	
	private String identityOrganizationalUnit;
	private String articleAffiliation;
	private double organizationalUnitMatchingScore;
	private String organizationalUnitModifier;
	private double organizationalUnitModifierScore;

}
