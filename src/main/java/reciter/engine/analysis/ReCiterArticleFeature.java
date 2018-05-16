package reciter.engine.analysis;

import lombok.Data;
import reciter.engine.analysis.evidence.PositiveEvidence;

import java.util.Date;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@Data
@DynamoDBDocument
public class ReCiterArticleFeature {
    private long pmid;
    private double score;
    private boolean userAssertion;
    private Date pubDate;
    private String journalTitleVerbose;
    private String journalTitleISOabbreviation;
    private String articleTitle;
    private List<ReCiterArticleAuthorFeature> reCiterArticleAuthorFeatures;
    private String volume;
    private String issue;
    private String pages;
    private String pmcid;
    private String doi;
    private PositiveEvidence positiveEvidence;
}
