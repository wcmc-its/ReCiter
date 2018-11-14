package reciter.engine.analysis.evidence;

import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.ToString;
import reciter.model.identity.AuthorName;
import reciter.model.identity.KnownRelationship.RelationshipType;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
    private double relationshipMatchModifierManager;
    private double relationshipMatchModifierManagerSeniorAuthor;
    
}
