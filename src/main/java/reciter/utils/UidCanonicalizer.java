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
 * <li>trim surrounding whitespace;</li>
 * <li>lowercase, unless the id belongs to an external validation cohort
 * ({@code usc_}, {@code fh_}, {@code fredhutch_}, {@code uc*_} such as
 * {@code ucsf_} or {@code ucla_}), whose ids can legitimately carry capitals
 * (e.g. {@code fredhutch_Rachel.Rau}). The prefix is matched
 * case-insensitively and only the prefix itself is lowercased, so
 * {@code UCSF_AbC} becomes {@code ucsf_AbC};</li>
 * <li>reject only what is still invalid: empty, internal whitespace, an
 * {@code @}, or any character outside {@code [a-z0-9._-]}
 * ({@code [A-Za-z0-9._-]} for external cohort ids).</li>
 * </ol>
 *
 * There is deliberately no WCM CWID shape check, so Cornell netids and
 * external cohort ids keep working.
 */
public final class UidCanonicalizer {

	private static final Logger log = LoggerFactory.getLogger(UidCanonicalizer.class);

	private static final Pattern EXTERNAL_COHORT_PREFIX = Pattern.compile("^(usc|fh|fredhutch|uc[a-z]*)_",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern CANONICAL = Pattern.compile("^[a-z0-9._-]+$");
	private static final Pattern CANONICAL_EXTERNAL = Pattern.compile("^[A-Za-z0-9._-]+$");

	private UidCanonicalizer() {
	}

	/**
	 * @return the canonical uid
	 * @throws InvalidUidException if the uid is null, empty, or still invalid
	 *                             after trimming and lowercasing
	 */
	public static String canonicalize(String uid) {
		if (uid == null) {
			throw new InvalidUidException(null, "uid is required");
		}
		String trimmed = uid.strip();
		if (trimmed.isEmpty()) {
			throw new InvalidUidException(uid, "uid is empty");
		}
		Matcher prefix = EXTERNAL_COHORT_PREFIX.matcher(trimmed);
		boolean external = prefix.find();
		String canonical = external
				? prefix.group().toLowerCase(Locale.ROOT) + trimmed.substring(prefix.end())
				: trimmed.toLowerCase(Locale.ROOT);
		if (!(external ? CANONICAL_EXTERNAL : CANONICAL).matcher(canonical).matches()) {
			throw new InvalidUidException(uid,
					"uid may contain only letters, digits, '.', '_' and '-' (no spaces or '@')");
		}
		return canonical;
	}

	/** Canonicalizes every uid in the list; the first invalid one throws. */
	public static List<String> canonicalize(List<String> uids) {
		if (uids == null) {
			return null;
		}
		List<String> canonical = new ArrayList<>(uids.size());
		for (String uid : uids) {
			canonical.add(canonicalize(uid));
		}
		return canonical;
	}

	/**
	 * Lookup variant for uid lists: canonicalizes every uid and drops (and logs)
	 * the ones that are still invalid instead of failing the whole batch, so a
	 * read with one stale email-shaped uid still returns the rest.
	 */
	public static List<String> canonicalizeDroppingInvalid(List<String> uids, String caller) {
		if (uids == null) {
			return null;
		}
		List<String> canonical = new ArrayList<>(uids.size());
		for (String uid : uids) {
			try {
				canonical.add(canonicalize(uid));
			} catch (InvalidUidException e) {
				log.warn("{}: dropping invalid uid from lookup: {}", caller, e.getMessage());
			}
		}
		return canonical;
	}

	/**
	 * Canonicalizes an optional uid-valued field (e.g. an actor CWID): null or
	 * blank stays null, anything else must canonicalize or it throws.
	 */
	public static String canonicalizeOptional(String uid) {
		return uid == null || uid.isBlank() ? null : canonicalize(uid);
	}

	/** True when the (already trimmed) uid starts with an external validation cohort prefix (any case). */
	public static boolean isExternalCohort(String uid) {
		return uid != null && EXTERNAL_COHORT_PREFIX.matcher(uid).find();
	}

	/** A uid that is still invalid after canonicalizing. Mapped to HTTP 400. */
	public static class InvalidUidException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private final String uid;

		public InvalidUidException(String uid, String reason) {
			super("Invalid uid '" + uid + "': " + reason);
			this.uid = uid;
		}

		public String getUid() {
			return uid;
		}
	}
}
