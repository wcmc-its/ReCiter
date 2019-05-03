package reciter.algorithm.evidence.targetauthor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Spy;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

@RunWith(MockitoJUnitRunner.class)
public class TargetAuthorSelectionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		//MockitoAnnotations.initMocks(this);
	}
	
	@Spy
	TargetAuthorSelection targetAuthorSelection = new TargetAuthorSelection();
	
	@Spy
	List<AuthorName> sanitizedIdentityAuthors = new ArrayList<AuthorName>();
	
	@Spy
	Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor = new HashSet<Map.Entry<ReCiterAuthor,ReCiterAuthor>>();

	/*	@Test
		public final void testIdentifyTargetAuthor() {
			fail("Not yet implemented");
		}*/

	/**
	 * This test for email between author affiliation and email from identity email
	 */
	@Test
	public final void testCheckEmailMatch() {
		Identity identity = new Identity();
		identity.setPrimaryName(new AuthorName("Ayman", "A", "Elmenyar"));
		identity.setEmails(Arrays.asList("aae2001@med.cornell.edu"));
		
		Map.Entry<ReCiterAuthor, ReCiterAuthor> entry =
			    new AbstractMap.SimpleEntry<ReCiterAuthor, ReCiterAuthor>(new ReCiterAuthor(new AuthorName("AymanA", null, "El-Menyar"), "aae2001@med.cornell.edu"), new ReCiterAuthor(new AuthorName("AymanA", null, "ElMenyar"), "aae2001@med.cornell.edu"));
		Set<Entry<ReCiterAuthor, ReCiterAuthor>> sanitizedAuthorSet = new HashSet<>(1);
		sanitizedAuthorSet.add(entry);
		
		assertEquals(1, targetAuthorSelection.checkEmailMatch(sanitizedAuthorSet, identity, 0, multipleMarkedTargetAuthor));
	}

	/**
	 * This test for exact last, middle and first name from sanitized identity and author name
	 */
	@Test
	public final void testCheckExactLastMiddleFirstNameMatch() {
		sanitizedIdentityAuthors.add(new AuthorName("Ayman", "A", "Elmenyar"));
		Map.Entry<ReCiterAuthor, ReCiterAuthor> entry =
			    new AbstractMap.SimpleEntry<ReCiterAuthor, ReCiterAuthor>(new ReCiterAuthor(new AuthorName("Ayman", "A.", "El-Menyar"), "aae2001@med.cornell.edu"), new ReCiterAuthor(new AuthorName("Ayman", "A", "ElMenyar"), "aae2001@med.cornell.edu"));
		Set<Entry<ReCiterAuthor, ReCiterAuthor>> sanitizedAuthorSet = new HashSet<>(1);
		sanitizedAuthorSet.add(entry);
		assertEquals("author name match", 1, targetAuthorSelection.checkExactLastMiddleFirstNameMatch(sanitizedAuthorSet, sanitizedIdentityAuthors, 0, multipleMarkedTargetAuthor));
		sanitizedIdentityAuthors.clear();
		
		sanitizedIdentityAuthors.add(new AuthorName("Ayman", "Alhul", "Elmenyar"));
		assertEquals("no author name match", 0, targetAuthorSelection.checkExactLastMiddleFirstNameMatch(sanitizedAuthorSet, sanitizedIdentityAuthors, 0, multipleMarkedTargetAuthor));
	}

	/*@Test
	public final void testCheckExactLastMiddleInitialFirstNameMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckExactLastFirstNameMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckExactLastFirstNamePartialSubstringIdentityMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckExactLastFirstNamePartialIdentityPartialSubstringMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckExactLastFirstInitialNameMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckPartialLastNameFirstInitialMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckLastNameExactMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckFirstNameExactMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckLastNameFullArticleToIdentityPartialMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckLastNamePartMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckFirstNameMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testCheckFirstInitialMatch() {
		fail("Not yet implemented");
	}
	
	@Test
	public final void testAssignTargetAuthorFalse() {
		fail("Not yet implemented");
	}*/

}
