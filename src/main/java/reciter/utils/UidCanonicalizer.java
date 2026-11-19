package reciter.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Canonical form of a person uid (CWID) at every write and lookup boundary.
 *
 * DynamoDB keys are exact strings, so {@code Abl9002}, {@code abl9002} and
 * {@code "abl9002 "} are three different people to ReCiter while ReCiterDB's
 * case-insensitive MySQL collation folds them into duplicate person rows.
 * Callers are converted on the way in rather than rejected:
 *
 * <ol>
 * <li>strip surrounding whitespace (Unicode-aware);</li>
 * <li>lowercase, unless the id belongs to an external validation cohort
 * ({@code usc_}, {@code fh_}, {@code fredhutch_}, {@code uc<campus>_} such as
 * {@code ucsf_} or {@code ucla_}), whose ids can legitimately carry capitals
 * (e.g. {@code fredhutch_Rachel.Rau}). The prefix is matched
 * case-insensitively and only the prefix itself is lowercased, so
 * {@code UCSF_AbC} becomes {@code ucsf_AbC};</li>
 * <li>reject only what is still invalid: empty, internal whitespace, an
 * {@code @}, any character outside {@code [a-z0-9._-]}
 * ({@code [A-Za-z0-9._-]} for external cohort ids), or a bare cohort prefix
 * with no identifier after it (e.g. {@code usc_}).</li>
 * </ol>
 *
 * There is deliberately no WCM CWID shape check, so Cornell netids and
 * external cohort ids keep working.
 */
public final class UidCanonicalizer {

	private static final Logger log = LoggerFactory.getLogger(UidCanonicalizer.class);

	private static final Pattern EXTERNAL_COHORT_PREFIX = Pattern.compile("^(?:usc|fh|fredhutch|uc[a-z]+)_",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CANONICAL = Pattern.compile("^[a-z0-9._-]+$");
	private static final Pattern CANONICAL_EXTERNAL = Pattern.compile("^[A-Za-z0-9._-]+$");
	/** Control characters (CR, LF, tab, ...) plus the Unicode next-line, line and paragraph separators. */
	private static final Pattern LOG_UNSAFE = Pattern.compile("[\\p{Cntrl}\\u0085\\u2028\\u2029]");

	private UidCanonicalizer() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	/**
	 * Canonicalizes one uid.
	 *
	 * @param uid the raw uid as received; must not be null
	 * @return the canonical uid
	 * @throws InvalidUidException if the uid is null, empty, a bare external
	 *                             cohort prefix, or still invalid after stripping
	 *                             and lowercasing
	 */
	public static String canonicalize(String uid) {
		if (uid == null) {
			throw new InvalidUidException(null, "uid is required");
		}
		String stripped = uid.strip();
		if (stripped.isEmpty()) {
			throw new InvalidUidException(uid, "uid is empty");
		}
		Matcher prefix = EXTERNAL_COHORT_PREFIX.matcher(stripped);
		boolean external = prefix.lookingAt();
		if (external && prefix.end() == stripped.length()) {
			throw new InvalidUidException(uid, "external cohort uid has no identifier after the prefix");
		}
		String canonical = external
				? prefix.group().toLowerCase(Locale.ROOT) + stripped.substring(prefix.end())
				: stripped.toLowerCase(Locale.ROOT);
		if (!(external ? CANONICAL_EXTERNAL : CANONICAL).matcher(canonical).matches()) {
			throw new InvalidUidException(uid,
					"uid may contain only letters, digits, '.', '_' and '-' (no spaces or '@')");
		}
		return canonical;
	}

	/**
	 * Canonicalizes every uid in the list; the first invalid one throws.
	 *
	 * @param uids the raw uids; may be null, which is treated as an empty list
	 *             (controllers pass optional request lists, where absent and
	 *             empty mean the same thing)
	 * @return an unmodifiable list of canonical uids, in input order; empty when
	 *         {@code uids} is null
	 * @throws InvalidUidException on the first uid that is null or still invalid
	 */
	public static List<String> canonicalize(List<String> uids) {
		if (uids == null) {
			return List.of();
		}
		List<String> canonical = new ArrayList<>(uids.size());
		for (String uid : uids) {
			canonical.add(canonicalize(uid));
		}
		return List.copyOf(canonical);
	}

	/**
	 * Lookup variant for uid lists: canonicalizes every uid and drops (and logs)
	 * the ones that are still invalid instead of failing the whole batch, so a
	 * read with one stale email-shaped uid still returns the rest.
	 *
	 * @param uids   the raw uids; may be null, which is treated as an empty list
	 * @param caller a short name of the calling endpoint, used in the log line
	 * @return an unmodifiable list of the canonical uids that were valid, in
	 *         input order; empty when {@code uids} is null
	 */
	public static List<String> canonicalizeDroppingInvalid(List<String> uids, String caller) {
		if (uids == null) {
			return List.of();
		}
		List<String> canonical = new ArrayList<>(uids.size());
		for (String uid : uids) {
			try {
				canonical.add(canonicalize(uid));
			} catch (InvalidUidException e) {
				// The exception message already carries a log-safe copy of the uid.
				log.warn("{}: dropping invalid uid from lookup: {}", sanitizeForLog(caller), e.getMessage());
			}
		}
		return List.copyOf(canonical);
	}

	/**
	 * Canonicalizes an optional uid-valued field (e.g. an actor CWID).
	 *
	 * @param uid the raw value; may be null or blank
	 * @return null when {@code uid} is null or blank, else the canonical uid
	 * @throws InvalidUidException if a non-blank value is still invalid
	 */
	public static String canonicalizeOptional(String uid) {
		return uid == null || uid.isBlank() ? null : canonicalize(uid);
	}

	/**
	 * Whether the uid starts with an external validation cohort prefix (any case).
	 *
	 * @param uid an already stripped uid; may be null
	 * @return true when {@code uid} starts with {@code usc_}, {@code fh_},
	 *         {@code fredhutch_} or {@code uc<campus>_}; false for null
	 */
	public static boolean isExternalCohort(String uid) {
		return uid != null && EXTERNAL_COHORT_PREFIX.matcher(uid).lookingAt();
	}

	/**
	 * Makes untrusted text safe to put in a log line or error message by
	 * replacing control characters (CR, LF, tab, ...) and Unicode line
	 * separators with {@code _}, so a uid cannot forge extra log lines (CWE-117).
	 *
	 * @param value the untrusted value; may be null
	 * @return the sanitized value, or {@code "null"} when {@code value} is null
	 */
	public static String sanitizeForLog(String value) {
		return value == null ? "null" : LOG_UNSAFE.matcher(value).replaceAll("_");
	}

	/**
	 * A uid that is still invalid after canonicalizing. Mapped to HTTP 400. The
	 * message carries a log-safe copy of the uid; {@link #getUid()} returns it raw.
	 */
	public static class InvalidUidException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final String uid;

		/**
		 * @param uid    the raw uid that was rejected; may be null
		 * @param reason why it was rejected
		 */
		public InvalidUidException(String uid, String reason) {
			this(uid, reason, null);
		}

		/**
		 * @param uid    the raw uid that was rejected; may be null
		 * @param reason why it was rejected
		 * @param cause  the underlying cause; may be null
		 */
		public InvalidUidException(String uid, String reason, Throwable cause) {
			super("Invalid uid '" + sanitizeForLog(uid) + "': " + reason, cause);
			this.uid = uid;
		}

		/**
		 * @return the raw uid as received (not sanitized); may be null
		 */
		public String getUid() {
			return uid;
		}
	}
}
