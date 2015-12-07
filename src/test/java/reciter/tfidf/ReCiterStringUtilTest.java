package reciter.tfidf;

import static org.junit.Assert.*;

import org.junit.Test;

public class ReCiterStringUtilTest {

	@Test
	public void testTokenize() {
		String s = "King's College School of Medicine and Dentistry, University of London (United Kingdom) 1974";
		String[] tokens = ReCiterStringUtil.tokenize(s);
		
		assertEquals("Size of tokens: ", 13, tokens.length);
		assertEquals("0th elem=", "Kings", tokens[0]);
		assertEquals("1th elem=", "College", tokens[1]);
		assertEquals("2th elem=", "School", tokens[2]);
		assertEquals("3th elem=", "of", tokens[3]);
		assertEquals("4th elem=", "Medicine", tokens[4]);
		assertEquals("5th elem=", "and", tokens[5]);
		assertEquals("6th elem=", "Dentistry", tokens[6]);
		assertEquals("7th elem=", "University", tokens[7]);
		assertEquals("8th elem=", "of", tokens[8]);
		assertEquals("9th elem=", "London", tokens[9]);
		assertEquals("10th elem=", "United", tokens[10]);
		assertEquals("1th elem=", "Kingdom", tokens[11]);
		assertEquals("12th elem=", "1974", tokens[12]);
	}
	
	@Test
	public void testComputeNumberOfOverlapTokens() {
		String s1 = "King's College School of Medicine and Dentistry, University of London (United Kingdom) 1974";
		String s2 = "King's College Hospital King's College Hospital London United Kingdom";
		
		int numOverlap = ReCiterStringUtil.computeNumberOfOverlapTokens(s1, s2);
		assertEquals("Number of overlaps: ", 5, numOverlap);
	}
}
