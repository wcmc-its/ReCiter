package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;
import reciter.model.identity.OrganizationalUnit;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@Data
@ToString
@DynamoDBDocument
public class AffiliationEvidence {
	
	private List<TargetAuthorScopusAffiliation> scopusTargetAuthorAffiliation;
	private TargetAuthorPubmedAffiliation pubmedTargetAuthorAffiliation;
	private NonTargetAuthorScopusAffiliation scopusNonTargetAuthorAffiliation;
	
	public enum InstitutionalAffiliationSource {
		SCOPUS,
		PUBMED
	}
	
	public enum InstitutionalAffiliationMatchType {
		POSITIVE_MATCH_INDIVIDUAL,
		POSITIVE_MATCH_INSTITUTION,
		NULL_MATCH,
		NO_MATCH
	}
}
