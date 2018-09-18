package reciter.engine.analysis.evidence;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.ToString;
import reciter.engine.analysis.evidence.AffiliationEvidence.InstitutionalAffiliationSource;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
@DynamoDBDocument
public class NonTargetAuthorScopusAffiliation {
	
	@DynamoDBTyped(DynamoDBAttributeType.S)
	private InstitutionalAffiliationSource nonTargetAuthorInstitutionalAffiliationSource;
	private List<String> nonTargetAuthorInstitutionalAffiliationMatchKnownInstitution;
	private List<String> nonTargetAuthorInstitutionalAffiliationMatchCollaboratingInstitution; 
	private double nonTargetAuthorInstitutionalAffiliationScore;
}
