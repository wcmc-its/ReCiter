package reciter.engine.analysis;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;

@Data
@DynamoDBDocument
public class ReCiterArticleAffiliationFeature {
    private String affiliationPubmed;
    private String affiliationScopus;
    private String affiliationScopusId;
}
