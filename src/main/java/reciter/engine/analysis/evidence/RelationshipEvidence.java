package reciter.engine.analysis.evidence;

import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;
import lombok.ToString;
import reciter.model.identity.AuthorName;

@Data
@ToString
@DynamoDBDocument
public class RelationshipEvidence {
    private AuthorName relationshipName;
    private Set<String> relationshipType;
    private String relationshipMatchType;
    private double relationshipMatchingScore;
    private double relationshipVerboseMatchModifierScore;
    private double relationshipMatchModifierMentor;
    private double relationshipMatchModifierMentorSeniorAuthor;
    
}
