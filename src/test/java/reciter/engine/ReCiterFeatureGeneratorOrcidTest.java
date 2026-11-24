package reciter.engine;

import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;

import org.junit.Test;

public class ReCiterFeatureGeneratorOrcidTest {

	private static String match(String raw) {
		Matcher matcher = ReCiterFeatureGenerator.ORCID_PATTERN.matcher(raw);
		return matcher.find() ? matcher.group() : null;
	}

	// Both iDs are the examples in ORCID's documentation.
	@Test
	public void keepsAnIdThatEndsInX() {
		assertEquals("0000-0002-1694-233X", match("0000-0002-1694-233X"));
		assertEquals("0000-0002-1694-233X", match("https://orcid.org/0000-0002-1694-233X"));
	}

	@Test
	public void keepsAnIdThatEndsInADigit() {
		assertEquals("0000-0002-1825-0097", match("http://orcid.org/0000-0002-1825-0097"));
	}

	@Test
	public void skipsAnIdWithoutItsCheckCharacter() {
		assertEquals(null, match("0000-0002-1694-233"));
	}
}
