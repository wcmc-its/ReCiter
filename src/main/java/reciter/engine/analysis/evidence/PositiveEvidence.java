package reciter.engine.analysis.evidence;

import lombok.Data;

@Data
public class PositiveEvidence {
    private AffiliationEvidence affiliationEvidence;
    private AuthorNameEvidence authorNameEvidence;
    private GrantEvidence grantEvidence;
    private RelationshipEvidence relationshipEvidence;
    private EducationYearEvidence educationYearEvidence;
    private ClusteringEvidence clusteringEvidence;
}
