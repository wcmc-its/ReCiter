package reciter.xml.retriever.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;

/**
 * Covers the blank-middleName hazard in deriveAdditionalName (an Identity primaryName
 * can carry middleName "" — non-null — which AuthorName's constructor substrings, so it
 * must behave exactly like null) and the crash-recording contract of
 * retrieveArticlesByDateRange: false means a retrieval worker died, while a clean run
 * that simply found nothing still returns true.
 */
public class AliasReCiterRetrievalEngineTest {

	private final AliasReCiterRetrievalEngine engine = new AliasReCiterRetrievalEngine();

	/** AuthorName("Manuel", "", ...) throws from the constructor, so set the blank after. */
	private static AuthorName nameWithBlankMiddle(String firstName, String middleName, String lastName) {
		AuthorName name = new AuthorName(firstName, null, lastName);
		name.setMiddleName(middleName);
		return name;
	}

	private static Set<String> flattened(Set<AuthorName> names) {
		return names.stream()
				.map(n -> n.getFirstName() + "|" + n.getMiddleName() + "|" + n.getLastName())
				.collect(Collectors.toSet());
	}

	@Test
	public void blankMiddleNameDerivesCompoundSurnameHalvesWithoutThrowing() {
		// The mah4006 shape: compound surname, middleName "" (empty string, non-null).
		Set<AuthorName> derived = engine.deriveAdditionalName(
				nameWithBlankMiddle("Manuel", "", "Hidalgo Medina"));

		assertEquals(2, derived.size());
		assertEquals(Set.of("Hidalgo", "Medina"),
				derived.stream().map(AuthorName::getLastName).collect(Collectors.toSet()));
	}

	@Test
	public void blankMiddleNameDerivesExactlyWhatNullDoes() {
		Set<AuthorName> fromBlank = engine.deriveAdditionalName(
				nameWithBlankMiddle("Manuel", "", "Hidalgo Medina"));
		Set<AuthorName> fromWhitespace = engine.deriveAdditionalName(
				nameWithBlankMiddle("Manuel", " ", "Hidalgo Medina"));
		Set<AuthorName> fromNull = engine.deriveAdditionalName(
				new AuthorName("Manuel", null, "Hidalgo Medina"));

		assertEquals(flattened(fromNull), flattened(fromBlank));
		assertEquals(flattened(fromNull), flattened(fromWhitespace));
	}

	@Test
	public void blankMiddleNameIsNotUsedAsAFirstNameSubstitute() {
		// The single-initial branch promotes middleName to firstName; a blank one would
		// hand AuthorName's constructor an empty firstName to substring. Like null, it
		// must derive nothing.
		assertTrue(engine.deriveAdditionalName(nameWithBlankMiddle("W", "", "Bracken")).isEmpty());
		assertTrue(engine.deriveAdditionalName(new AuthorName("W", null, "Bracken")).isEmpty());
	}

	@Test
	public void realMiddleNamesStillDeriveUnchanged() {
		// AuthorName(_, null, _) stores middleName as "".
		Set<AuthorName> derived = engine.deriveAdditionalName(new AuthorName("W", "Clay", "Bracken"));
		assertEquals(Set.of("Clay||Bracken"), flattened(derived));
	}

	@Test
	public void crashedRetrievalWorkerIsRecordedAndReportedAsFailure() throws IOException {
		// A bare engine has no services wired, so the worker thread dies on an unchecked
		// NPE at the top of retrieveData — the ForkJoinPool stores the throwable without
		// rethrowing it, so the recorded uid is the only trace the caller can observe.
		Identity identity = new Identity();
		identity.setUid("mah4006");

		assertFalse(engine.retrieveArticlesByDateRange(Collections.singletonList(identity),
				new Date(), new Date(), RetrievalRefreshFlag.ALL_PUBLICATIONS));
	}

	@Test
	public void runWithNoRetrievalWorkStillReportsSuccess() throws IOException {
		// FALSE dispatches no retrieval, so no worker can fail: the gate is "a worker
		// crashed", never "nothing was retrieved".
		Identity identity = new Identity();
		identity.setUid("mah4006");

		assertTrue(engine.retrieveArticlesByDateRange(Collections.singletonList(identity),
				new Date(), new Date(), RetrievalRefreshFlag.FALSE));
	}
}
