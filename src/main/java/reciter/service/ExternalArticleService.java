package reciter.service;

import java.util.List;

import reciter.database.dynamodb.model.ExternalArticle;

public interface ExternalArticleService {

    void save(ExternalArticle externalArticle);

    List<ExternalArticle> findByUid(String uid);

    ExternalArticle find(String uid, String articleId);

    void delete(String uid, String articleId);

    /**
     * Supersede rule: suppress non-suppressed external rows whose PMID or DOI
     * matches an accepted PMID (DOIs resolved via the person's Analysis output).
     * Never throws; returns the number of rows suppressed.
     */
    int supersedeByAcceptedPmids(String uid, List<Long> acceptedPmids);
}
