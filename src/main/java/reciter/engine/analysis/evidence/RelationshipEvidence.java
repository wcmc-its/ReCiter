package reciter.engine.analysis.evidence;

import lombok.Data;

import java.util.List;

@Data
public class RelationshipEvidence {
    private String relationshipName;
    private List<String> relationshipTypes;
}
