package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;
import lombok.ToString;
import reciter.model.identity.AuthorName;

@Data
@ToString
@DynamoDBDocument
public class AuthorNameEvidence {
    private AuthorName institutionalAuthorName;
    private AuthorName articleAuthorName;
}
