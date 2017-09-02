package reciter.engine.analysis;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
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
