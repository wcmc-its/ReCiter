/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.tfidf;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import reciter.utils.ReCiterStringUtil;

public class ReCiterStringUtilTest {

	@Test
	public void testTokenize() {
		String s = "King's College School of Medicine and Dentistry, University of London (United Kingdom) 1974";
		String[] tokens = ReCiterStringUtil.tokenize(s);
		
		assertEquals(13, tokens.length, "Size of tokens: ");
		assertEquals("Kings", tokens[0], "0th elem=");
		assertEquals("College", tokens[1], "1th elem=");
		assertEquals("School", tokens[2], "2th elem=");
		assertEquals("of", tokens[3], "3th elem=");
		assertEquals("Medicine", tokens[4], "4th elem=");
		assertEquals("and", tokens[5], "5th elem=");
		assertEquals("Dentistry", tokens[6], "6th elem=");
		assertEquals("University", tokens[7], "7th elem=");
		assertEquals("of", tokens[8], "8th elem=");
		assertEquals("London", tokens[9], "9th elem=");
		assertEquals("United", tokens[10], "10th elem=");
		assertEquals("Kingdom", tokens[11], "11th elem=");
		assertEquals("1974", tokens[12], "12th elem=");
	}
	
	@Test
	public void testComputeNumberOfOverlapTokens() {
		String s1 = "King's College School of Medicine and Dentistry, University of London (United Kingdom) 1974";
		String s2 = "King's College Hospital King's College Hospital London United Kingdom";
		
		int numOverlap = ReCiterStringUtil.computeNumberOfOverlapTokens(s1, s2);
		assertEquals(5, numOverlap, "Number of overlaps: ");
	}
	
	@Test
	public void testLevenshteinDistance1() {
		String s1 = "Antony";
		String s2 = "Anthony";
		int dist = ReCiterStringUtil.levenshteinDistance(s1, s2);
		assertEquals(1, dist, "Distance should be 1");
	}
	
	@Test
	public void testLevenshteinDistance2() {
		String s1 = "Bi-Sen";
		String s2 = "BiSen";
		int dist = ReCiterStringUtil.levenshteinDistance(s1, s2);
		assertEquals(1, dist, "Distance should be 1");
	}
	
	@Test
	public void testLevenshteinDistance3() {
		String s1 = "Jeffery";
		String s2 = "Jeffrey";
		int dist = ReCiterStringUtil.levenshteinDistance(s1, s2);
		assertEquals(2, dist, "Distance should be 2");
	}
	
	@Test
	public void testDeAccent1() {
		String s = "å";
		String deAccentedS = ReCiterStringUtil.deAccent(s);
	    assertEquals("a", deAccentedS, "equal");
	}
	
	@Test
	public void testDeAccent2() {
		String s = "Ibáñez";
		String deAccentedS = ReCiterStringUtil.deAccent(s);
		assertEquals("Ibanez", deAccentedS, "equal");
	}
	
	@Test
	public void testDeAccent3() {
		String s = "Guzmán";
		String deAccentedS = ReCiterStringUtil.deAccent(s);
		 assertEquals("Guzman", deAccentedS, "equal");	}
	
	@Test
	public void testDeAccent4() {
		String s = "ö";
		String deAccentedS = ReCiterStringUtil.deAccent(s);
		 assertEquals("o", deAccentedS, "equal");
	}

	@Test
	public void testStripBackslashesWithBackslashDash() {
		String s = "Non\\-alcoholic fatty liver disease";
		String result = ReCiterStringUtil.stripBackslashes(s);
		assertEquals("Non-alcoholic fatty liver disease", result, "Backslash-dash should be stripped");
	}

	@Test
	public void testStripBackslashesNoBackslashes() {
		String s = "Normal title without backslashes";
		String result = ReCiterStringUtil.stripBackslashes(s);
		assertEquals("Normal title without backslashes", result, "String without backslashes should be unchanged");
	}

	@Test
	public void testStripBackslashesNull() {
		String result = ReCiterStringUtil.stripBackslashes(null);
		assertNull(result, "Null input should return null");
	}

	@Test
	public void testStripBackslashesEmpty() {
		String result = ReCiterStringUtil.stripBackslashes("");
		assertEquals("", result, "Empty string should return empty");
	}
}
