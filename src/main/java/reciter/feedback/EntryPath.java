package reciter.feedback;

/**
 * Discriminator for how a curator action originated in Publication Manager.
 *
 * <p>Phase 33 D-14: PM UI emits this on every feedback action so the Java
 * write path can distinguish auto-retrieved-candidate curation from
 * curator-driven PubMed search.
 *
 * <ul>
 *   <li>{@link #CANDIDATE_LIST} — curator clicked ACCEPT/REJECT/PENDING on a row in
 *       the auto-retrieved candidate list. The PMID is already (or will be)
 *       attributed via Phase 33-01's retrieval write path.</li>
 *   <li>{@link #PUBMED_SEARCH} — curator queried PubMed inside PM's UI and accepted
 *       (or rejected) a result. The Java write path treats this as its own
 *       retrieval strategy with {@code rs='PM_UI_SEARCH'} (D-13).</li>
 * </ul>
 */
public enum EntryPath {
    CANDIDATE_LIST,
    PUBMED_SEARCH;

    /**
     * Resolve a string to {@link EntryPath} with case-insensitive matching.
     * Returns {@link #CANDIDATE_LIST} (the default) for null, empty, or unrecognized values.
     */
    public static EntryPath fromString(String s) {
        if (s == null || s.isEmpty()) {
            return CANDIDATE_LIST;
        }
        try {
            return EntryPath.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CANDIDATE_LIST;
        }
    }
}
