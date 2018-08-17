package reciter.engine.analysis;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamoDBDocument
public class ReCiterArticleAffiliationFeature {
    private String affiliationPubmed;
    private String affiliationScopus;
    private String affiliationScopusId;
}
