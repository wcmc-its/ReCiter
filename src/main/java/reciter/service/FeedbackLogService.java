package reciter.service;

import reciter.database.dynamodb.model.FeedbackLog;

/**
 * Writes curator action records to the {@code FeedbackLog} DynamoDB table.
 *
 * <p>Schema (Phase 32 D-01, D-02):
 * <ul>
 *   <li>{@code uid} (S, PK)</li>
 *   <li>{@code sk} (S, SK) = {@code "<epoch_seconds>#<8-hex-digit-suffix>"}</li>
 *   <li>{@code articleId} (S)</li>
 *   <li>{@code feedback} (S) ∈ {ACCEPTED, REJECTED, PENDING}</li>
 *   <li>{@code curatedBy} (N) — userID from PM</li>
 *   <li>{@code src} (S) = "MAN" (PM UI is the only source for this table going forward)</li>
 *   <li>{@code createTimestamp} (N), {@code modifyTimestamp} (N)</li>
 * </ul>
 *
 * <p>The 8-hex-digit suffix is derived from {@code System.nanoTime()} and gives
 * uniqueness within a single Java process; the conditional {@code attribute_not_exists(sk)}
 * catches cross-process collisions and the caller retries with a fresh suffix.
 */
public interface FeedbackLogService {

    /**
     * Curator action feedback values. See Phase 32 D-04.
     */
    enum Feedback {
        ACCEPTED, REJECTED, PENDING
    }

    /**
     * Record a single curator action.
     *
     * @param uid                 person identifier
     * @param pmid                PubMed ID
     * @param feedback            ACCEPTED, REJECTED, or PENDING
     * @param curatedBy           userID of the curator (or 0 if unknown — e.g., when the
     *                            action is inferred from a GoldStandard list-replace API call
     *                            that doesn't carry a userID)
     * @param actionEpochSeconds  curator action time, epoch seconds (used in sk and timestamps)
     */
    void recordAction(FeedbackLog feedbackLog);
}
