package reciter.engine.analysis;

import lombok.Data;

import java.util.List;

@Data
public class ReCiterArticleAuthorFeature {
    private int rank;
    private String lastName;
    private String firstName;
    private String initials;
    private List<ReCiterArticleAffiliationFeature> affiliations;
    private boolean isTargetAuthor;
}
