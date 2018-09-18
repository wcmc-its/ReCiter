package reciter.engine.analysis;

import lombok.Data;
import reciter.engine.erroranalysis.UseGoldStandard;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel.DynamoDBAttributeType;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamoDBDocument
public class ReCiterFeature {
    private String personIdentifier;
    private Date dateAdded;
    private Date dateUpdated;
    @DynamoDBTyped(DynamoDBAttributeType.S)
    private UseGoldStandard mode;
    private double overallAccuracy;
    private double precision;
    private double recall;
    private List<Long> inGoldStandardButNotRetrieved;
    private int countSuggestedArticles;
    private List<ReCiterArticleFeature> reCiterArticleFeatures;

}
