package reciter.service;

import java.util.List;

import reciter.database.dynamodb.model.ExternalArticle;

public interface ExternalArticleService {

    void save(ExternalArticle externalArticle);

    List<ExternalArticle> findByUid(String uid);

    ExternalArticle find(String uid, String articleId);

    void delete(String uid, String articleId);

    /**
     * Supersede rule, run after gold standard updates. Re-reads the authoritative
     * gold standard and reconciles both directions: suppresses active rows whose
     * PMID or DOI matches an accepted PMID (DOIs resolved via the person's
     * Analysis output), and un-suppresses rows whose superseding PMID is no
     * longer accepted. Never throws; returns the number of rows changed.
     */
    int reconcileWithGoldStandard(String uid);

    /**
     * Files a dispute: sets disputedBy/disputedAt/disputeNote (permanent, first-write-only
     * in practice) and immediately suppresses the row. Does not touch supersededByPmid, so
     * the row stays immune to reconcileWithGoldStandard's auto-un-suppress path. Saves and
     * returns the mutated row.
     */
    ExternalArticle dispute(ExternalArticle externalArticle, String actingUid, String disputeNote);

    /**
     * Resolves a filed dispute — either the disputing faculty member retracting it
     * ({@code resolution="RETRACTED"}) or a curator clearing it ({@code resolution="CLEARED"}).
     * Un-suppresses the row and records disputeResolvedBy/disputeResolvedAt/disputeResolution;
     * never clears the original dispute fields. Saves and returns the mutated row.
     */
    ExternalArticle resolveDispute(ExternalArticle externalArticle, String actingUid, String resolution);
}
