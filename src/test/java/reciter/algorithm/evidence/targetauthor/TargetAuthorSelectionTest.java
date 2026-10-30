package reciter.algorithm.evidence.targetauthor;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.Test;

import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

/**
 * Unit tests for the {@link TargetAuthorSelection} disambiguation cascade.
 *
 * Each {@code check*} step is exercised directly (no mocks, no AWS): an article-side
 * author set is matched against a list of sanitized identity names and the method
 * returns the number of authors it marks as the target author. {@code matchCount == 0}
 * drives the "fresh" branch of every step; a step returns &gt; 1 when more than one
 * author matches, which the orchestrator treats as ambiguity and passes to the next
 * (stricter) step.
 */
public class TargetAuthorSelectionTest {


	// Plain instances: these are data holders, not mocks. Spying concrete JDK
	// collections (ArrayList/HashSet) under JDK 17 + Mockito/Objenesis instantiates
	// them without running their constructors, leaving elementData/map null -> NPE.
	TargetAuthorSelection targetAuthorSelection = new TargetAuthorSelection();

	List<AuthorName> sanitizedIdentityAuthors = new ArrayList<AuthorName>();

	Set<Entry<ReCiterAuthor, ReCiterAuthor>> multipleMarkedTargetAuthor = new HashSet<Map.Entry<ReCiterAuthor,ReCiterAuthor>>();

	// ---- helpers -----------------------------------------------------------

	/** Builds a one-author article set. The cascade reads entry.getValue() as the
	 *  article author and entry.getKey() as the original author; both point at the
	 *  same instance here since the tests only assert the returned match count. */
	private static Set<Entry<ReCiterAuthor, ReCiterAuthor>> articleSet(AuthorName name) {
		ReCiterAuthor author = new ReCiterAuthor(name, null);
		Set<Entry<ReCiterAuthor, ReCiterAuthor>> set = new HashSet<>();
		set.add(new AbstractMap.SimpleEntry<>(author, author));
		return set;
	}

	private static List<AuthorName> identityNames(AuthorName... names) {
		return new ArrayList<>(Arrays.asList(names));
	}

	private static Set<Entry<ReCiterAuthor, ReCiterAuthor>> empty() {
		return new HashSet<>();
	}

	// ---- step 0: email ------------------------------------------------------

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

	// ---- step 1: last + middle + first --------------------------------------

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
		assertEquals(1, targetAuthorSelection.checkExactLastMiddleFirstNameMatch(sanitizedAuthorSet,sanitizedIdentityAuthors, 0, multipleMarkedTargetAuthor), "author name match");
		
		sanitizedIdentityAuthors.clear();

		sanitizedIdentityAuthors.add(new AuthorName("Ayman", "Alhul", "Elmenyar"));
		assertEquals(0, targetAuthorSelection.checkExactLastMiddleFirstNameMatch(sanitizedAuthorSet,sanitizedIdentityAuthors, 0, multipleMarkedTargetAuthor), "no author name match");
	}

	// ---- step 2: last + middle initial + first ------------------------------

	@Test
	public final void testCheckExactLastMiddleInitialFirstNameMatch() {
		// article middle "A" -> middle initial "A"; identity middle "Albert" -> "A"
		Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors = articleSet(new AuthorName("Ayman", "A", "Elmenyar"));
		assertEquals(1,
				targetAuthorSelection.checkExactLastMiddleInitialFirstNameMatch(authors,
						identityNames(new AuthorName("Ayman", "Albert", "Elmenyar")), 0, empty()),
				"middle-initial match");

		assertEquals(0,
				targetAuthorSelection.checkExactLastMiddleInitialFirstNameMatch(
						articleSet(new AuthorName("Ayman", "A", "Elmenyar")),
						identityNames(new AuthorName("Ayman", "Robert", "Elmenyar")), 0, empty()),
				"different middle initial -> no match");
        
	}

	// ---- step 3: last + first ----------------------------------------------

	@Test
	public final void testCheckExactLastFirstNameMatch() {
		assertEquals(1,targetAuthorSelection.checkExactLastFirstNameMatch(articleSet(new AuthorName("Ayman", null, "Elmenyar")),
						identityNames(new AuthorName("Ayman", null, "Elmenyar")), 0, empty()),"exact last+first match");

		assertEquals(0,targetAuthorSelection.checkExactLastFirstNameMatch(articleSet(new AuthorName("Ayman", null, "Elmenyar")),
						identityNames(new AuthorName("Bob", null, "Elmenyar")), 0, empty()),"different first name -> no match");
	}

	// ---- step 4: last + (article first ⊂ identity first) --------------------

	@Test
	public final void testCheckExactLastFirstNamePartialSubstringIdentityMatch() {
		// article "Rob" is a substring of identity "Robert"
		assertEquals(1,targetAuthorSelection.checkExactLastFirstNamePartialSubstringIdentityMatch(articleSet(new AuthorName("Rob", null, "Smith")),
						identityNames(new AuthorName("Robert", null, "Smith")), 0, empty()),"article first is substring of identity first");

		assertEquals(0,targetAuthorSelection.checkExactLastFirstNamePartialSubstringIdentityMatch(articleSet(new AuthorName("Rob", null, "Smith")),
	                    identityNames(new AuthorName("Alice", null, "Smith")), 0, empty()),"not a substring -> no match");
	}

	// ---- step 5: last + (identity first ⊂ article first) --------------------

	@Test
	public final void testCheckExactLastFirstNamePartialIdentityPartialSubstringMatch() {
		// identity "Rob" is a substring of article "Robert"
		assertEquals(1,targetAuthorSelection.checkExactLastFirstNamePartialIdentityPartialSubstringMatch(articleSet(new AuthorName("Robert", null, "Smith")),
						identityNames(new AuthorName("Rob", null, "Smith")), 0, empty()),"identity first is substring of article first");

		assertEquals(0,targetAuthorSelection.checkExactLastFirstNamePartialIdentityPartialSubstringMatch(articleSet(new AuthorName("Robert", null, "Smith")),
						identityNames(new AuthorName("Alice", null, "Smith")), 0, empty()),"not a substring -> no match");
	}

	// ---- step 6: last + first initial --------------------------------------

	@Test
	public final void testCheckExactLastFirstInitialNameMatch() {
		// both first initials resolve to "A", last names match
		assertEquals(1,
				targetAuthorSelection.checkExactLastFirstInitialNameMatch(
						articleSet(new AuthorName("Andrew", null, "Smith")),
						identityNames(new AuthorName("Alice", null, "Smith")), 0, empty()),
				"first-initial + last match");

		assertEquals(0,
				targetAuthorSelection.checkExactLastFirstInitialNameMatch(
						articleSet(new AuthorName("Andrew", null, "Smith")),
						identityNames(new AuthorName("Bob", null, "Smith")), 0, empty()),
				"different first initial -> no match");
	}

	// ---- step 8: first-initial ↔ middle-initial swap ------------------------

	@Test
	public final void testCheckFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch() {
		// article first/middle initials (X / A) are the swap of identity's (A / X)
		assertEquals(1,
				targetAuthorSelection.checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(
						articleSet(new AuthorName("Xavier", "Andrew", "Smith")),
						identityNames(new AuthorName("Andrew", "Xavier", "Smith")), 0, empty()),
				"initial swap with exact last name matches");

		// middle initial does not swap (B != X) -> no match
		assertEquals(0,
				targetAuthorSelection.checkFirstInitialTomiddleInitialAndmiddleInitialToFirstInitialMatch(
						articleSet(new AuthorName("Xavier", "Andrew", "Smith")),
						identityNames(new AuthorName("Andrew", "Bob", "Smith")), 0, empty()),
				"no swap -> no match");
	}

	// ---- step 9: partial last name + first initial --------------------------

	@Test
	public final void testCheckPartialLastNameFirstInitialMatch() {
		// article last "Somersan-Karakaya" contains identity last "Karakaya"; first initials match (A)
		assertEquals(1,
				targetAuthorSelection.checkPartialLastNameFirstInitialMatch(
						articleSet(new AuthorName("Andrew", null, "Somersan-Karakaya")),
						identityNames(new AuthorName("Alice", null, "Karakaya")), 0, empty()),
				"partial last name + first initial matches");

		assertEquals(0,
				targetAuthorSelection.checkPartialLastNameFirstInitialMatch(
						articleSet(new AuthorName("Andrew", null, "Somersan-Karakaya")),
						identityNames(new AuthorName("Bob", null, "Karakaya")), 0, empty()),
				"first initial mismatch -> no match");
	}

	// ---- step 10: last name only -------------------------------------------

	@Test
	public final void testCheckLastNameExactMatch() {
		// last name only; case-insensitive, first name irrelevant
		assertEquals(1,
				targetAuthorSelection.checkLastNameExactMatch(articleSet(new AuthorName("Andrew", null, "Smith")),
						identityNames(new AuthorName("Alice", null, "smith")), 0, empty()),
				"last name only matches (case-insensitive)");

		assertEquals(0,
				targetAuthorSelection.checkLastNameExactMatch(articleSet(new AuthorName("Andrew", null, "Smith")),
						identityNames(new AuthorName("Alice", null, "Jones")), 0, empty()),
				"different last name -> no match");
	}

	/** Two authors sharing the identity last name -> count of 2, surfacing the
	 *  ambiguity the orchestrator resolves with a later, stricter step. */
	@Test
	public final void testLastNameExactMatch_multipleMatchesSurfaceAmbiguity() {
		ReCiterAuthor a1 = new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null);
		ReCiterAuthor a2 = new ReCiterAuthor(new AuthorName("Alice", null, "Smith"), null);
		Set<Entry<ReCiterAuthor, ReCiterAuthor>> authors = new HashSet<>();
		authors.add(new AbstractMap.SimpleEntry<>(a1, a1));
		authors.add(new AbstractMap.SimpleEntry<>(a2, a2));

		assertEquals(2,
				targetAuthorSelection.checkLastNameExactMatch(authors,
						identityNames(new AuthorName("Ann", null, "Smith")), 0, empty()),
				"two last-name matches -> ambiguity (count 2)");
	}

	// ---- step 11: first name only ------------------------------------------

	@Test
	public final void testCheckFirstNameExactMatch() {
		// first name only; case-insensitive, last name irrelevant
		assertEquals(1,
				targetAuthorSelection.checkFirstNameExactMatch(articleSet(new AuthorName("Ayman", null, "Smith")),
						identityNames(new AuthorName("ayman", null, "Jones")), 0, empty()),
				"first name only matches (case-insensitive)");

		assertEquals(0,
				targetAuthorSelection.checkFirstNameExactMatch(articleSet(new AuthorName("Ayman", null, "Smith")),
						identityNames(new AuthorName("Bob", null, "Smith")), 0, empty()),
				"different first name -> no match");
	}

	// ---- step 12: full article last name ⊂ identity last name ----------------

	@Test
	public final void testCheckLastNameFullArticleToIdentityPartialMatch() {
		// identity last "Somersan-Karakaya" contains article last "Karakaya"
		assertEquals(1,
				targetAuthorSelection.checkLastNameFullArticleToIdentityPartialMatch(
						articleSet(new AuthorName("Andrew", null, "Karakaya")),
						identityNames(new AuthorName("Alice", null, "Somersan-Karakaya")), 0, empty()),
				"article last name is part of identity last name");

		assertEquals(0,
				targetAuthorSelection.checkLastNameFullArticleToIdentityPartialMatch(
						articleSet(new AuthorName("Andrew", null, "Karakaya")),
						identityNames(new AuthorName("Alice", null, "Smith")), 0, empty()),
				"not contained -> no match");
	}

	// ---- list/identity variants --------------------------------------------

	private static Identity identityWith(AuthorName primary) {
		Identity identity = new Identity();
		identity.setPrimaryName(primary);
		identity.setAlternateNames(new ArrayList<AuthorName>());
		return identity;
	}

	@Test
	public final void testCheckLastNamePartMatch() {
		List<ReCiterAuthor> authors = new ArrayList<>();
		authors.add(new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null));

		assertEquals(1,
				targetAuthorSelection.checkLastNamePartMatch(authors,
						identityWith(new AuthorName("Alice", null, "Smith")), 0),
				"article last name contains identity primary last name");

		List<ReCiterAuthor> noMatch = new ArrayList<>();

		noMatch.add(new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null));

		assertEquals(0, targetAuthorSelection.checkLastNamePartMatch(noMatch,
				identityWith(new AuthorName("Alice", null, "Jones")), 0), "unrelated last name -> no match");
	}

	@Test
	public final void testCheckFirstNameMatch() {
		List<ReCiterAuthor> authors = new ArrayList<>();
		authors.add(new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null));

		assertEquals(1,
				targetAuthorSelection.checkFirstNameMatch(authors,
						identityWith(new AuthorName("Andrew", null, "Smith")), 0),
				"article first name contains identity primary first name");

		List<ReCiterAuthor> noMatch = new ArrayList<>();

		noMatch.add(new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null));

		assertEquals(0, targetAuthorSelection.checkFirstNameMatch(noMatch,
				identityWith(new AuthorName("Robert", null, "Smith")), 0), "unrelated first name -> no match");
	}

	@Test
	public final void testCheckFirstInitialMatch() {
		List<ReCiterAuthor> authors = new ArrayList<>();
		authors.add(new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null));

		assertEquals(1, targetAuthorSelection.checkFirstInitialMatch(authors,
				identityWith(new AuthorName("Alice", null, "Jones")), 0), "matching first initial (A == A)");

		List<ReCiterAuthor> noMatch = new ArrayList<>();
		noMatch.add(new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null));
		assertEquals(0, targetAuthorSelection.checkFirstInitialMatch(noMatch,
				identityWith(new AuthorName("Bob", null, "Smith")), 0), "different first initial -> no match");
		
	}

	@Test
	public final void testAssignTargetAuthorFalse() {
		ReCiterAuthor a1 = new ReCiterAuthor(new AuthorName("Andrew", null, "Smith"), null);
		ReCiterAuthor a2 = new ReCiterAuthor(new AuthorName("Alice", null, "Smith"), null);
		a1.setTargetAuthor(true);
		a2.setTargetAuthor(true);

		targetAuthorSelection.assignTargetAuthorFalse(Arrays.asList(a1, a2));

		assertFalse(a1.isTargetAuthor());
		assertFalse(a2.isTargetAuthor());
	}

}