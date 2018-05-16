package reciter.engine.analysis;

import lombok.Data;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@Data
@DynamoDBDocument
public class ReCiterFeature {
    private String personIdentifier;
    private Date dateAdded;
    private Date dateUpdated;
    private String mode;
    private double overallAccuracy;
    private double precision;
    private double recall;
    private List<Long> inGoldStandardButNotRetrieved;
    private int countSuggestedArticles;
    private List<ReCiterArticleFeature> reCiterArticleFeatures;

}
