package reciter.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import reciter.utils.UidCanonicalizer.InvalidUidException;

class UidCanonicalizerTest {

	@ParameterizedTest(name = "canonicalize(\"{0}\") -> \"{1}\"")
	@CsvSource(delimiter = '|', value = {
			// already canonical: unchanged
			"abl9002|abl9002",
			"paa2013|paa2013",
			// wrong case and stray whitespace seen in prod Identity/GoldStandard
			"Abl9002|abl9002",
			"DLAMB|dlamb",
			"HAF9018|haf9018",
			"Hlloyd|hlloyd",
			"'ggi9001 '|ggi9001",
			"' Isk9008'|isk9008",
			"'\tJih9026\n'|jih9026",
			// Unicode whitespace (em space / ideographic space) is stripped too
			"' Abl9002　'|abl9002",
			// no WCM-shape check: Cornell netids and other shapes keep working
			"ab123|ab123",
			"Jason.Carmel|jason.carmel",
			"str2025-mi|str2025-mi",
			"_lsw8099|_lsw8099",
			// "uc_" alone is not a cohort prefix (needs a campus), so it is an ordinary uid
			"UC_Abc|uc_abc",
			// external validation cohorts keep their case, but are stripped
			"fredhutch_Rachel.Rau|fredhutch_Rachel.Rau",
			"' fredhutch_Rachel.Rau '|fredhutch_Rachel.Rau",
			"fh_catherine.albert|fh_catherine.albert",
			"usc_123|usc_123",
			"ucsf_AbC|ucsf_AbC",
			"ucla_Xy-1|ucla_Xy-1",
			// an upper-case cohort prefix is still a cohort id: only the prefix is lowercased
			"UCSF_AbC|ucsf_AbC",
			"FREDHUTCH_A.B|fredhutch_A.B",
			"Usc_123|usc_123",
	})
	void canonicalizes(String input, String expected) {
		assertEquals(expected, UidCanonicalizer.canonicalize(input));
	}

	@ParameterizedTest(name = "rejects invalid uid: \"{0}\"")
	@ValueSource(strings = {
			"",
			"   ",
			"kims7@mskcc.org",
			"ldn9002@nyp.org",
			"Mahender Jangari",
			"Hello, world!",
			"abc 123",
			"fredhutch_Rachel Rau",
			"usc_a@b",
			"abc/123",
			"abcé",
			// bare external cohort prefixes: no identifier after the prefix
			"usc_",
			"fh_",
			"fredhutch_",
			"ucsf_",
			" UCLA_ ",
	})
	void rejectsWhatIsStillInvalid(String input) {
		InvalidUidException e = assertThrows(InvalidUidException.class, () -> UidCanonicalizer.canonicalize(input));
		assertTrue(e.getMessage().startsWith("Invalid uid '"));
		assertEquals(input, e.getUid());
	}

	@Test
	void rejectsNull() {
		assertThrows(InvalidUidException.class, () -> UidCanonicalizer.canonicalize((String) null));
	}

	@Test
	void externalPrefixMatchesAnyCase() {
		// The prefix is matched case-insensitively; only the prefix is lowercased.
		assertEquals("fh_Abc", UidCanonicalizer.canonicalize("FH_Abc"));
		assertTrue(UidCanonicalizer.isExternalCohort("FH_Abc"));
		assertTrue(UidCanonicalizer.isExternalCohort("ucdavis_x"));
		// "uc" without an underscore-terminated campus prefix is an ordinary uid.
		assertFalse(UidCanonicalizer.isExternalCohort("ucx2001"));
		assertFalse(UidCanonicalizer.isExternalCohort("uc_x"));
		// The prefix must be at the start.
		assertFalse(UidCanonicalizer.isExternalCohort("xusc_1"));
		assertFalse(UidCanonicalizer.isExternalCohort(null));
	}

	@Test
	void canonicalizesValidList() {
		assertEquals(List.of("abl9002", "ggi9001", "fredhutch_A.B"),
				UidCanonicalizer.canonicalize(List.of("Abl9002", "ggi9001 ", "fredhutch_A.B")));
	}

	@Test
	void canonicalizeListReturnsEmptyListForNullInput() {
		assertEquals(List.of(), UidCanonicalizer.canonicalize((List<String>) null));
	}

	@Test
	void canonicalizeListThrowsOnFirstInvalidUid() {
		assertThrows(InvalidUidException.class,
				() -> UidCanonicalizer.canonicalize(List.of("abl9002", "kims7@mskcc.org")));
	}

	@Test
	void canonicalizeListReturnsUnmodifiableList() {
		List<String> canonical = UidCanonicalizer.canonicalize(List.of("Abl9002"));
		assertThrows(UnsupportedOperationException.class, () -> canonical.add("x"));
	}

	@Test
	void lookupListsDropInvalidUids() {
		assertEquals(List.of("abl9002", "ggi9001"), UidCanonicalizer.canonicalizeDroppingInvalid(
				List.of("Abl9002", "kims7@mskcc.org", " ggi9001", "a b"), "test"));
	}

	@Test
	void lookupListReturnsEmptyListForNullInput() {
		assertEquals(List.of(), UidCanonicalizer.canonicalizeDroppingInvalid(null, "test"));
	}

	@Test
	void lookupListReturnsUnmodifiableList() {
		List<String> canonical = UidCanonicalizer.canonicalizeDroppingInvalid(List.of("Abl9002"), "test");
		assertThrows(UnsupportedOperationException.class, () -> canonical.add("x"));
	}

	@Test
	void optionalUidFieldNullOrBlankIsNull() {
		assertNull(UidCanonicalizer.canonicalizeOptional(null));
		assertNull(UidCanonicalizer.canonicalizeOptional("  "));
	}

	@Test
	void optionalUidFieldIsCanonicalized() {
		assertEquals("dnr4021", UidCanonicalizer.canonicalizeOptional(" DNR4021 "));
	}

	@Test
	void optionalUidFieldStillRejectsInvalid() {
		assertThrows(InvalidUidException.class, () -> UidCanonicalizer.canonicalizeOptional("a@b"));
	}

	@ParameterizedTest(name = "exception message strips control chars from \"{0}\"")
	@ValueSource(strings = {
			"abc\r\n[ERROR] Database compromised",
			"abc\n2026-11-19 INFO forged",
			"abc forged",
			"abc\u0085forged",
			"abc\u0000x",
	})
	void exceptionMessageIsLogSafe(String input) {
		InvalidUidException e = assertThrows(InvalidUidException.class, () -> UidCanonicalizer.canonicalize(input));
		assertFalse(e.getMessage().matches("(?s).*[\\p{Cntrl}\\u0085\\u2028\\u2029].*"), e.getMessage());
		// The raw value is still available to callers that need it.
		assertEquals(input, e.getUid());
	}

	@Test
	void sanitizeForLogReplacesLineBreaks() {
		assertEquals("a__[ERROR] x", UidCanonicalizer.sanitizeForLog("a\r\n[ERROR] x"));
		assertEquals("null", UidCanonicalizer.sanitizeForLog(null));
		assertEquals("abl9002", UidCanonicalizer.sanitizeForLog("abl9002"));
	}

	@Test
	void exceptionKeepsCause() {
		IllegalStateException cause = new IllegalStateException("boom");
		InvalidUidException e = new InvalidUidException("a\nb", "bad", cause);
		assertSame(cause, e.getCause());
		assertEquals("Invalid uid 'a_b': bad", e.getMessage());
		assertEquals("a\nb", e.getUid());
	}

	@Test
	void cannotBeInstantiated() throws Exception {
		Constructor<UidCanonicalizer> constructor = UidCanonicalizer.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException e = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(UnsupportedOperationException.class, e.getCause());
	}
}
