package reciter.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import reciter.model.identity.AuthorName;

@RunWith(MockitoJUnitRunner.class)
public class AuthorNameSanitizationUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSanitizeArticleAuthorNames() {
		//fail("Not yet implemented");
	}

	@Test
	public final void testSanitizeIdentityAuthorNames() {
		//fail("Not yet implemented");
	}

	@Test
	public final void testCheckToIgnoreNameVariants() {
		Map<AuthorName, AuthorName> sanitizedIdentityAuthorMap = new HashMap<AuthorName, AuthorName>(); 
		AuthorNameSanitizationUtils authorNameSanitizationUtils = new AuthorNameSanitizationUtils();
		sanitizedIdentityAuthorMap.put(new AuthorName("Joerg-Patrick", null, "Stuebgen"), new AuthorName("JoergPatrick", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("Jo", null, "Stuebgen"), new AuthorName("Jo", null, "Stuebgen"));
		authorNameSanitizationUtils.checkToIgnoreNameVariants(sanitizedIdentityAuthorMap);
		//Check for AuthorName firstName starts with other AuthorName and middle name is null or empty
		assertEquals("Removed one name", 1, sanitizedIdentityAuthorMap.size());
		sanitizedIdentityAuthorMap.clear();
		sanitizedIdentityAuthorMap.put(new AuthorName("Joerg-Patrick", null, "Stuebgen"), new AuthorName("JoergPatrick", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("JoE", null, "Stuebgen"), new AuthorName("JoE", null, "Stuebgen"));
		authorNameSanitizationUtils.checkToIgnoreNameVariants(sanitizedIdentityAuthorMap);
		assertEquals("Case ensitive check", 1, sanitizedIdentityAuthorMap.size());
		
		
		sanitizedIdentityAuthorMap.clear();
		sanitizedIdentityAuthorMap.put(new AuthorName("Joerg-Patrick", null, "Stuebgen"), new AuthorName("JoergPatrick", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("JoE", null, "Stuebgen"), new AuthorName("JoE", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("Jo", null, "Stuebgen"), new AuthorName("Jo", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("Joseph", "Patrick", "Stuebgen"), new AuthorName("Joseph", "Patrick", "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("Jose", "Patrick", "Stuebgen"), new AuthorName("Jose", "Patrick", "Stuebgen"));
		authorNameSanitizationUtils.checkToIgnoreNameVariants(sanitizedIdentityAuthorMap);
		assertEquals("All check including case sensitive, first case & second case", 3, sanitizedIdentityAuthorMap.size());
	}

	// #704: alc4061's identity — primary "Alberto Mario" with an EMPTY middle name plus alternates
	// "Alberto Mario" / null and "Alberto" / null. Two rules fire in one pass (blank-middle and
	// starts-with) and the old iterator-mutating loop threw IllegalStateException from a second
	// remove() without an intervening next(), 500-ing every feature-generator call for the uid.
	// Built with setters, not the 3-arg constructor: that constructor normalises a null middle
	// name to "", while sanitizeIdentityAuthorNames only sets a middle name when the alias has
	// one — so production really does mix "" and null, which is what trips the two rules at once.
	@Test
	public final void testCheckToIgnoreNameVariantsDoesNotThrowWhenTwoRulesFireInOnePass() {
		Map<AuthorName, AuthorName> names = new HashMap<AuthorName, AuthorName>();
		AuthorNameSanitizationUtils utils = new AuthorNameSanitizationUtils();
		names.put(name("Alberto Mario", "", "Ceballos Arroyo"), name("AlbertoMario", "", "CeballosArroyo"));
		names.put(name("Alberto Mario", null, "Ceballos Arroyo"), name("AlbertoMario", null, "CeballosArroyo"));
		names.put(name("Alberto", null, "Ceballos Arroyo"), name("Alberto", null, "CeballosArroyo"));
		assertEquals(3, names.size());
		utils.checkToIgnoreNameVariants(names);
		assertEquals("blank-middle and prefix variants both dropped, one full name kept", 1, names.size());
		AuthorName kept = names.values().iterator().next();
		assertEquals("AlbertoMario", kept.getFirstName());
		assertNull(kept.getMiddleName());

		// a chain where every variant is a prefix of the next and one carries a blank middle:
		// every pair matches a rule, in every order — must never throw, must never remove the
		// last name, and the longest full name is the one left standing.
		names.clear();
		names.put(name("A", null, "Last"), name("A", null, "Last"));
		names.put(name("Ab", null, "Last"), name("Ab", null, "Last"));
		names.put(name("Abc", "", "Last"), name("Abc", "", "Last"));
		names.put(name("Abc", null, "Last"), name("Abc", null, "Last"));
		assertEquals(4, names.size());
		utils.checkToIgnoreNameVariants(names);
		assertEquals(1, names.size());
		assertEquals("Abc", names.values().iterator().next().getFirstName());
		assertNull(names.values().iterator().next().getMiddleName());
	}

	private static AuthorName name(String first, String middle, String last) {
		AuthorName n = new AuthorName();
		n.setFirstName(first);
		n.setMiddleName(middle);
		n.setLastName(last);
		return n;
	}

	@Test
	public final void testGenerateSuffixRegex() {
		//fail("Not yet implemented");
	}

}
