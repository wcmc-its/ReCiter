package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@Data
@ToString
@DynamoDBDocument
public class PositiveEvidence {
    private AffiliationEvidence affiliationEvidence;
    private AuthorNameEvidence authorNameEvidence;
    private GrantEvidence grantEvidence;
    private List<RelationshipEvidence> relationshipEvidences;
    private EducationYearEvidence educationYearEvidence;
    private ClusteringEvidence clusteringEvidence;
}
