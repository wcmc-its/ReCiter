package reciter.engine.analysis;

import lombok.Data;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamoDBDocument
public class ReCiterArticleAuthorFeature {
    private int rank;
    private String lastName;
    private String firstName;
    private String initials;
    private List<ReCiterArticleAffiliationFeature> affiliations;
    private boolean isTargetAuthor;
}
