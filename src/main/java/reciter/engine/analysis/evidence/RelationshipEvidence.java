package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;
import reciter.model.identity.AuthorName;

@Data
@ToString
public class RelationshipEvidence {
    private AuthorName relationshipName;
    private String relationshipType;
}
