package reciter.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import reciter.utils.UidCanonicalizer.InvalidUidException;

public class UidCanonicalizerTest {

	@ParameterizedTest
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
			// no WCM-shape check: Cornell netids and other shapes keep working
			"ab123|ab123",
			"Jason.Carmel|jason.carmel",
			"str2025-mi|str2025-mi",
			"_lsw8099|_lsw8099",
			// external validation cohorts keep their case, but are trimmed
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
	public void canonicalizes(String input, String expected) {
		assertEquals(expected, UidCanonicalizer.canonicalize(input));
	}

	@ParameterizedTest
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
	})
	public void rejectsWhatIsStillInvalid(String input) {
		InvalidUidException e = assertThrows(InvalidUidException.class, () -> UidCanonicalizer.canonicalize(input));
		assertTrue(e.getMessage().startsWith("Invalid uid '"));
		assertEquals(input, e.getUid());
	}

	@Test
	public void trimsTabsAndNewlines() {
		assertEquals("jih9026", UidCanonicalizer.canonicalize("\tJih9026\n"));
	}

	@Test
	public void rejectsNull() {
		assertThrows(InvalidUidException.class, () -> UidCanonicalizer.canonicalize((String) null));
	}

	@Test
	public void externalPrefixMatchesAnyCase() {
		// The prefix is matched case-insensitively; only the prefix is lowercased.
		assertEquals("fh_Abc", UidCanonicalizer.canonicalize("FH_Abc"));
		assertTrue(UidCanonicalizer.isExternalCohort("FH_Abc"));
		assertTrue(UidCanonicalizer.isExternalCohort("ucdavis_x"));
		// "uc" without an underscore-terminated campus prefix is an ordinary uid.
		assertFalse(UidCanonicalizer.isExternalCohort("ucx2001"));
	}

	@Test
	public void canonicalizesLists() {
		assertEquals(Arrays.asList("abl9002", "ggi9001", "fredhutch_A.B"),
				UidCanonicalizer.canonicalize(Arrays.asList("Abl9002", "ggi9001 ", "fredhutch_A.B")));
		assertNull(UidCanonicalizer.canonicalize((java.util.List<String>) null));
		assertThrows(InvalidUidException.class,
				() -> UidCanonicalizer.canonicalize(Arrays.asList("abl9002", "kims7@mskcc.org")));
	}

	@Test
	public void lookupListsDropInvalidUids() {
		assertEquals(Arrays.asList("abl9002", "ggi9001"), UidCanonicalizer.canonicalizeDroppingInvalid(
				Arrays.asList("Abl9002", "kims7@mskcc.org", " ggi9001", "a b"), "test"));
		assertNull(UidCanonicalizer.canonicalizeDroppingInvalid(null, "test"));
	}

	@Test
	public void optionalUidFields() {
		assertNull(UidCanonicalizer.canonicalizeOptional(null));
		assertNull(UidCanonicalizer.canonicalizeOptional("  "));
		assertEquals("dnr4021", UidCanonicalizer.canonicalizeOptional(" DNR4021 "));
		assertThrows(InvalidUidException.class, () -> UidCanonicalizer.canonicalizeOptional("a@b"));
	}
}
