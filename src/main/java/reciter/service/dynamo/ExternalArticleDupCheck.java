package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import reciter.database.dynamodb.model.ExternalArticle;
import reciter.engine.analysis.ReCiterArticleFeature;

/**
 * Duplicate detection for manually added external-source articles.
 *
 * BLOCKED: the work already exists in the person's PubMed-domain record (gold
 * standard or retrieved candidates, matched by PMID or DOI) or was already added
 * from another external source. No override.
 *
 * WARNING: a fuzzy title+year collision. Overridable with force=true at the API.
 */
public final class ExternalArticleDupCheck {

    public enum Level {
        OK, WARNING, BLOCKED
    }

    public static final class Match {
        private final String type;
        private final String matchedId;
        private final String detail;

        Match(String type, String matchedId, String detail) {
            this.type = type;
            this.matchedId = matchedId;
            this.detail = detail;
        }

        public String getType() {
            return type;
        }

        public String getMatchedId() {
            return matchedId;
        }

        public String getDetail() {
            return detail;
        }
    }

    public static final class Result {
        private final Level level;
        private final List<Match> matches;

        Result(Level level, List<Match> matches) {
            this.level = level;
            this.matches = matches;
        }

        public Level getLevel() {
            return level;
        }

        public List<Match> getMatches() {
            return matches;
        }
    }

    private ExternalArticleDupCheck() {
    }

    public static Result check(ExternalArticle candidate,
                               List<Long> knownPmids,
                               List<Long> rejectedPmids,
                               List<ReCiterArticleFeature> candidateArticles,
                               List<ExternalArticle> existingExternal) {
        List<Match> blocked = new ArrayList<>();
        List<Match> warnings = new ArrayList<>();
        String candidateDoi = normalizeDoi(candidate.getDoi());
        String candidateTitle = normalizeTitle(candidate.getTitle());
        String candidateYear = year(candidate.getPubDate());

        for (ExternalArticle existing : safe(existingExternal)) {
            if (existing.getArticleId() != null && existing.getArticleId().equals(candidate.getArticleId())) {
                blocked.add(new Match("ALREADY_ADDED", existing.getArticleId(),
                        "This external article was already added for this person."));
            } else if (candidate.getPmid() != null && candidate.getPmid().equals(existing.getPmid())) {
                blocked.add(new Match("PMID_MATCH_EXTERNAL", existing.getArticleId(),
                        "An external article with the same PMID was already added from " + existing.getSourceType() + "."));
            } else if (candidateDoi != null && candidateDoi.equals(normalizeDoi(existing.getDoi()))) {
                blocked.add(new Match("DOI_MATCH_EXTERNAL", existing.getArticleId(),
                        "An external article with the same DOI was already added from " + existing.getSourceType() + "."));
            } else if (titleYearCollision(candidateTitle, candidateYear,
                    normalizeTitle(existing.getTitle()), year(existing.getPubDate()))) {
                warnings.add(new Match("TITLE_YEAR_MATCH_EXTERNAL", existing.getArticleId(),
                        "An external article with a matching title and year was already added: " + existing.getTitle()));
            }
        }

        if (candidate.getPmid() != null && knownPmids != null && knownPmids.contains(candidate.getPmid())) {
            blocked.add(new Match("PMID_IN_GOLD_STANDARD", String.valueOf(candidate.getPmid()),
                    "This PMID is already accepted in the person's gold standard."));
        }
        if (candidate.getPmid() != null && rejectedPmids != null && rejectedPmids.contains(candidate.getPmid())) {
            blocked.add(new Match("PMID_REJECTED_IN_GOLD_STANDARD", String.valueOf(candidate.getPmid()),
                    "This PMID was explicitly rejected by a curator; do not re-add it from an external source."));
        }

        for (ReCiterArticleFeature feature : safe(candidateArticles)) {
            if (candidate.getPmid() != null && feature.getPmid() == candidate.getPmid()) {
                blocked.add(new Match("PMID_IN_CANDIDATES", String.valueOf(feature.getPmid()),
                        "This PMID exists in the person's PubMed candidate set; adjudicate it via normal feedback instead."));
            } else if (candidateDoi != null && candidateDoi.equals(normalizeDoi(feature.getDoi()))) {
                blocked.add(new Match("DOI_MATCH", String.valueOf(feature.getPmid()),
                        "A PubMed article with the same DOI exists in the person's candidate set (PMID " + feature.getPmid() + ")."));
            } else if (titleYearCollision(candidateTitle, candidateYear,
                    normalizeTitle(feature.getArticleTitle()), year(feature.getPublicationDateStandardized()))) {
                warnings.add(new Match("TITLE_YEAR_MATCH", String.valueOf(feature.getPmid()),
                        "A PubMed article with a matching title and year exists (PMID " + feature.getPmid() + "): " + feature.getArticleTitle()));
            }
        }

        if (!blocked.isEmpty()) {
            blocked.addAll(warnings);
            return new Result(Level.BLOCKED, blocked);
        }
        if (!warnings.isEmpty()) {
            return new Result(Level.WARNING, warnings);
        }
        return new Result(Level.OK, new ArrayList<>());
    }

    /**
     * Supersede matching (#660): returns the accepted PMID that supersedes this
     * external row — by direct PMID first, then by normalized DOI — or null if none.
     */
    public static Long matchSupersedingPmid(ExternalArticle row,
                                            List<Long> acceptedPmids,
                                            Map<Long, String> acceptedPmidToDoi) {
        if (row.getPmid() != null && acceptedPmids != null && acceptedPmids.contains(row.getPmid())) {
            return row.getPmid();
        }
        String rowDoi = normalizeDoi(row.getDoi());
        if (rowDoi == null || acceptedPmidToDoi == null) {
            return null;
        }
        for (Map.Entry<Long, String> accepted : acceptedPmidToDoi.entrySet()) {
            if (rowDoi.equals(normalizeDoi(accepted.getValue()))) {
                return accepted.getKey();
            }
        }
        return null;
    }

    // ponytail: exact match on normalized title + same year. Upgrade to token-overlap
    // similarity if real-world misses (subtitle variants, punctuation-heavy titles) show up.
    private static boolean titleYearCollision(String titleA, String yearA, String titleB, String yearB) {
        if (titleA == null || titleB == null || !titleA.equals(titleB)) {
            return false;
        }
        // Identical titles collide unless both years are known and differ.
        return yearA == null || yearB == null || yearA.equals(yearB);
    }

    /** Lowercase, trim, strip any doi.org URL prefix. Returns null for blank input. */
    static String normalizeDoi(String doi) {
        if (doi == null) {
            return null;
        }
        String normalized = doi.trim().toLowerCase(Locale.ROOT)
                .replaceFirst("^https?://(dx\\.)?doi\\.org/", "");
        return normalized.isEmpty() ? null : normalized;
    }

    /** Lowercase, collapse all non-alphanumerics to single spaces. Returns null for blank input. */
    static String normalizeTitle(String title) {
        if (title == null) {
            return null;
        }
        String normalized = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    /** First 4-digit year found in a date string, or null. */
    static String year(String date) {
        if (date == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{4}").matcher(date);
        return m.find() ? m.group() : null;
    }

    private static <T> List<T> safe(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
