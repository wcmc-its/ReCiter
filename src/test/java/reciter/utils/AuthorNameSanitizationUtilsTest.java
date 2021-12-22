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

	@Test
	public final void testGenerateSuffixRegex() {
		//fail("Not yet implemented");
	}

}
