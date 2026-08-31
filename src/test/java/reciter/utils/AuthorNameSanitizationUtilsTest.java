package reciter.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import reciter.model.identity.AuthorName;

@ExtendWith(MockitoExtension.class)
public class AuthorNameSanitizationUtilsTest {

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	public static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	public void setUp() throws Exception {
	}

	@AfterEach
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
		assertEquals(1, sanitizedIdentityAuthorMap.size(), "Removed one name");
		sanitizedIdentityAuthorMap.clear();
		sanitizedIdentityAuthorMap.put(new AuthorName("Joerg-Patrick", null, "Stuebgen"), new AuthorName("JoergPatrick", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("JoE", null, "Stuebgen"), new AuthorName("JoE", null, "Stuebgen"));
		authorNameSanitizationUtils.checkToIgnoreNameVariants(sanitizedIdentityAuthorMap);
		assertEquals(1, sanitizedIdentityAuthorMap.size(), "Case sensitive check");
		
		
		sanitizedIdentityAuthorMap.clear();
		sanitizedIdentityAuthorMap.put(new AuthorName("Joerg-Patrick", null, "Stuebgen"), new AuthorName("JoergPatrick", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("JoE", null, "Stuebgen"), new AuthorName("JoE", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("Jo", null, "Stuebgen"), new AuthorName("Jo", null, "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("Joseph", "Patrick", "Stuebgen"), new AuthorName("Joseph", "Patrick", "Stuebgen"));
		sanitizedIdentityAuthorMap.put(new AuthorName("Jose", "Patrick", "Stuebgen"), new AuthorName("Jose", "Patrick", "Stuebgen"));
		authorNameSanitizationUtils.checkToIgnoreNameVariants(sanitizedIdentityAuthorMap);
		assertEquals(3, sanitizedIdentityAuthorMap.size(), "All checks including case sensitive, first case & second case");
	}

	@Test
	public final void testGenerateSuffixRegex() {
		//fail("Not yet implemented");
	}

}
