package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.ToString;
import reciter.engine.analysis.evidence.AffiliationEvidence.InstitutionalAffiliationMatchType;
import reciter.engine.analysis.evidence.AffiliationEvidence.InstitutionalAffiliationSource;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
@DynamoDBDocument
public class TargetAuthorPubmedAffiliation {
	@DynamoDBTyped(DynamoDBAttributeType.S)
	private InstitutionalAffiliationSource targetAuthorInstitutionalAffiliationSource;
	private String targetAuthorInstitutionalAffiliationIdentity;
	private String targetAuthorInstitutionalAffiliationArticlePubmedLabel;
	@DynamoDBTyped(DynamoDBAttributeType.S)
	private InstitutionalAffiliationMatchType targetAuthorInstitutionalAffiliationMatchType;
	private double targetAuthorInstitutionalAffiliationMatchTypeScore;
}
