package reciter.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import reciter.api.parameters.FilterFeedbackType;
import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.api.parameters.UseGoldStandard;
import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.engine.StrategyParameters;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.engine.analysis.ReCiterArticleFeature.PublicationFeedback;
import reciter.engine.analysis.ReCiterFeature;
import reciter.model.identity.Identity;
import reciter.service.AnalysisService;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@ExtendWith(MockitoExtension.class)
public class ReCiterControllerTest {

	@Mock
	private ESearchResultService eSearchResultService;

	@Mock
	private PubMedService pubMedService;

	@Mock
	private ReCiterRetrievalEngine aliasReCiterRetrievalEngine;

	@Mock
	private IdentityService identityService;

	@Mock
	private ScopusService scopusService;

	@Mock
	private StrategyParameters strategyParameters;

	@Mock
	private AnalysisService analysisService;

	@Mock
	private IDynamoDbGoldStandardService dynamoDbGoldStandardService;

	@InjectMocks
	private ReCiterController reCiterController;

	private GoldStandard validGoldStandard;
	private List<GoldStandard> validGoldStandardList;
	private List<Identity> identityList;
	private String testUid = "test123";
	private Identity identity;
	private ESearchResult testESearchResult;
	private List<String> testUids;
	private List<AnalysisOutput> analysisOutputList;
	private Double testScore = 0.9;
	private int maxArticles = 5;
	private List<String> personTypes;
	private AnalysisOutput analysisOutput;
	private ReCiterFeature reCiterFeature;
	private GoldStandard goldStandard;
	private List<ReCiterArticleFeature> articleFeatures;
	private Double defaultScoreThreshold = 0.7;

	@BeforeEach
	public void setUp() {
		// Set the necessary properties
		ReflectionTestUtils.setField(reCiterController, "useScopusArticles", true);
		ReflectionTestUtils.setField(reCiterController, "totalArticleScoreStandardizedDefault", 0.7);
		ReflectionTestUtils.setField(reCiterController, "nameIgnoredCoAuthors", "Smith, J;Doe, J");
		ReflectionTestUtils.setField(reCiterController, "keywordsMax", 10.0);
		ReflectionTestUtils.setField(reCiterController, "uidsMaxCount", 50);
		// Set the default score threshold
		ReflectionTestUtils.setField(reCiterController, "totalArticleScoreStandardizedDefault", defaultScoreThreshold);

		// Create test data
		validGoldStandard = createValidGoldStandard(testUid);

		// Create a list of valid gold standards
		validGoldStandardList = new ArrayList<>();
		validGoldStandardList.add(validGoldStandard);
		validGoldStandardList.add(createValidGoldStandard("test456"));

		// Create identity list for retrieveArticles method
		identityList = new ArrayList<>();
		identity = new Identity();
		
		identity.setUid(testUid);
		identityList.add(identity);

		Identity identity2 = new Identity();
		identity2.setUid("test456");
		identityList.add(identity2);

		testESearchResult = new ESearchResult();
		testESearchResult.setUid(testUid);
		testESearchResult.setRetrievalDate(Instant.now());
		testUids = Arrays.asList(testUid, "test456");
		personTypes = Arrays.asList("faculty", "student");

		// Create article features
		articleFeatures = new ArrayList<>();

		// Article with score above threshold and NULL feedback
		ReCiterArticleFeature article1 = new ReCiterArticleFeature();
		article1.setPmid(12345L);
		article1.setAuthorshipLikelihoodScore(8.5);
		article1.setUserAssertion(PublicationFeedback.NULL);

		// Article with ACCEPTED feedback
		ReCiterArticleFeature article2 = new ReCiterArticleFeature();
		article2.setPmid(23456L);
		article2.setAuthorshipLikelihoodScore(9.2);
		article2.setUserAssertion(PublicationFeedback.ACCEPTED);

		// Article with REJECTED feedback
		ReCiterArticleFeature article3 = new ReCiterArticleFeature();
		article3.setPmid(34567L);
		article3.setAuthorshipLikelihoodScore(5.1);
		article3.setUserAssertion(PublicationFeedback.REJECTED);

		// Article with score below threshold and NULL feedback
		ReCiterArticleFeature article4 = new ReCiterArticleFeature();
		article4.setPmid(45678L);
		article4.setAuthorshipLikelihoodScore(3.5);
		article4.setUserAssertion(PublicationFeedback.NULL);

		articleFeatures.add(article1);
		articleFeatures.add(article2);
		articleFeatures.add(article3);
		articleFeatures.add(article4);

		// Create gold standard data
		goldStandard = new GoldStandard();
		goldStandard.setUid(testUid);
		goldStandard.setKnownPmids(Arrays.asList(23456L));

		// Create ReCiterFeature
		reCiterFeature = new ReCiterFeature();
		reCiterFeature.setPersonIdentifier(testUid);
		reCiterFeature.setReCiterArticleFeatures(new ArrayList<>(articleFeatures));
		reCiterFeature.setCountSuggestedArticles(articleFeatures.size());
		reCiterFeature.setCountPendingArticles(1L);
		reCiterFeature.setPrecision(0.66);
		reCiterFeature.setRecall(0.75);
		reCiterFeature.setOverallAccuracy(0.7);

		// Create analysis output
		analysisOutput = new AnalysisOutput();
		analysisOutput.setUid(testUid);
		analysisOutput.setReCiterFeature(reCiterFeature);

		// Create analysis output list
		analysisOutputList = new ArrayList<>();
		for (String uid : testUids) {
			AnalysisOutput analysisOutput = new AnalysisOutput();
			ReCiterFeature reCiterFeature = new ReCiterFeature();
			reCiterFeature.setPersonIdentifier(uid);

			List<ReCiterArticleFeature> articleFeatures = new ArrayList<>();
			// Create articles with scores above threshold
			for (int i = 0; i < 10; i++) {
				ReCiterArticleFeature article = new ReCiterArticleFeature();
				article.setPmid((long) (10000 + i));
				article.setUserAssertion(PublicationFeedback.NULL);
				article.setAuthorshipLikelihoodScore(1.0); // Set score above threshold
				articleFeatures.add(article);
			}
			reCiterFeature.setReCiterArticleFeatures(articleFeatures);
			analysisOutput.setReCiterFeature(reCiterFeature);
			analysisOutputList.add(analysisOutput);
		}
	}

	private GoldStandard createValidGoldStandard(String uid) {

		GoldStandard goldStandard = new GoldStandard();
		goldStandard.setUid(uid);
		goldStandard.setKnownPmids(Arrays.asList(12345L, 67890L));
		goldStandard.setRejectedPmids(Arrays.asList(54321L, 98765L));
		return goldStandard;
	}

	@Test
	public final void testReCiterFeatureGeneratorGroupFilters() {
		List<String> identityInstitutions = Arrays.asList("Weill Cornell Medical College",
				"New York-Presbyterian Hospital", "Hamad Medical Corporation");
		List<String> filterInstitution = Arrays.asList("New York-Presbyterian Hospital", "MSKCC");
		List<String> identityOrgUnits = Arrays.asList("Pediatrics", "General Internal Medicine (Medicine)",
				"Doctor of Medicine");
		List<String> filterOrgUnits = Arrays.asList("Pediatrics");
		List<String> identityPersonTypes = Arrays.asList("academic", "affiliate-nyp-epic", "affiliate");
		List<String> filterPersonTypes = Arrays.asList("affiliate-nyp-epic");

		// Valid filters match
		assertFalse(Collections.disjoint(identityInstitutions, filterInstitution));
		assertFalse(Collections.disjoint(identityOrgUnits, filterOrgUnits));
		assertFalse(Collections.disjoint(identityPersonTypes, filterPersonTypes));

		if (identityPersonTypes != null && !identityPersonTypes.isEmpty()
				&& !Collections.disjoint(identityPersonTypes, filterPersonTypes) && identityInstitutions != null
				&& !identityInstitutions.isEmpty() && !Collections.disjoint(identityInstitutions, filterInstitution)
				&& identityOrgUnits != null && !identityOrgUnits.isEmpty()
				&& !Collections.disjoint(identityOrgUnits, filterOrgUnits)) {
			System.out.println("Filter for both orgunits and personType");
		}

		// No match filter
		identityOrgUnits = Arrays.asList("Pediatrics", "General Internal Medicine (Medicine)", "Doctor of Medicine");
		filterOrgUnits = Arrays.asList("Neurology");
		assertTrue(Collections.disjoint(identityOrgUnits, filterOrgUnits));
	}

	@Test
	public void testUpdateGoldStandardSuccess() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(any(GoldStandard.class), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<?> response = reCiterController.updateGoldStandard(validGoldStandard,
				GoldStandardUpdateFlag.UPDATE);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandard, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandard, GoldStandardUpdateFlag.UPDATE);
	}

	@Test
	public void testUpdateGoldStandardNullFlag() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(any(GoldStandard.class), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<?> response = reCiterController.updateGoldStandard(validGoldStandard, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandard, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandard, GoldStandardUpdateFlag.UPDATE);
	}

	@Test
	public void testUpdateGoldStandardDeleteFlag() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(any(GoldStandard.class), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<?> response = reCiterController.updateGoldStandard(validGoldStandard,
				GoldStandardUpdateFlag.DELETE);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandard, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandard, GoldStandardUpdateFlag.DELETE);
	}

	@Test
	public void testUpdateGoldStandardRefreshFlag() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(any(GoldStandard.class), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<?> response = reCiterController.updateGoldStandard(validGoldStandard,
				GoldStandardUpdateFlag.REFRESH);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandard, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandard, GoldStandardUpdateFlag.REFRESH);
	}

	@Test
	public void testUpdateGoldStandardNullGoldStandard() {
		GoldStandard goldStandard = null;
		// Act
		ResponseEntity<?> response = reCiterController.updateGoldStandard(goldStandard, GoldStandardUpdateFlag.UPDATE);

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("The api requires a GoldStandard model", response.getBody());
		verify(dynamoDbGoldStandardService, never()).save(any(GoldStandard.class), any(GoldStandardUpdateFlag.class));
	}

	@Test
	public void testUpdateGoldStandardNullUid() {
		// Arrange
		GoldStandard invalidGoldStandard = new GoldStandard();
		// UID is null

		// Act
		ResponseEntity<?> response = reCiterController.updateGoldStandard(invalidGoldStandard,
				GoldStandardUpdateFlag.UPDATE);

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("The api requires a valid uid to be passed with GoldStandard model", response.getBody());
		verify(dynamoDbGoldStandardService, never()).save(any(GoldStandard.class), any(GoldStandardUpdateFlag.class));
	}

	@Test
	public void testUpdateGoldStandardListSuccess() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(anyList(), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<List<GoldStandard>> response = reCiterController.updateGoldStandard(validGoldStandardList,
				GoldStandardUpdateFlag.UPDATE);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandardList, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandardList, GoldStandardUpdateFlag.UPDATE);
	}

	@Test
	public void testUpdateGoldStandardListNullFlag() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(anyList(), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<List<GoldStandard>> response = reCiterController.updateGoldStandard(validGoldStandardList, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandardList, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandardList, GoldStandardUpdateFlag.UPDATE);
	}

	@Test
	public void testUpdateGoldStandardListDeleteFlag() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(anyList(), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<List<GoldStandard>> response = reCiterController.updateGoldStandard(validGoldStandardList,
				GoldStandardUpdateFlag.DELETE);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandardList, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandardList, GoldStandardUpdateFlag.DELETE);
	}

	@Test
	public void testUpdateGoldStandardListRefreshFlag() {
		// Arrange
		doNothing().when(dynamoDbGoldStandardService).save(anyList(), any(GoldStandardUpdateFlag.class));

		// Act
		ResponseEntity<List<GoldStandard>> response = reCiterController.updateGoldStandard(validGoldStandardList,
				GoldStandardUpdateFlag.REFRESH);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandardList, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).save(validGoldStandardList, GoldStandardUpdateFlag.REFRESH);
	}

	@Test
	public void testRetrieveGoldStandardByUidSuccess() {
		// Arrange
		when(dynamoDbGoldStandardService.findByUid("test123")).thenReturn(validGoldStandard);

		// Act
		ResponseEntity<GoldStandard> response = reCiterController.retrieveGoldStandardByUid("test123");

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(validGoldStandard, response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).findByUid("test123");
	}

	@Test
	public void testRetrieveGoldStandardByUidNotFound() {
		// Arrange
		when(dynamoDbGoldStandardService.findByUid("nonexistent")).thenReturn(null);

		// Act
		ResponseEntity<GoldStandard> response = reCiterController.retrieveGoldStandardByUid("nonexistent");

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNull(response.getBody());
		verify(dynamoDbGoldStandardService, times(1)).findByUid("nonexistent");
	}

	@Test
	public void testRetrieveArticlesSuccess() throws IOException {
		// Arrange
		when(identityService.findAll()).thenReturn(identityList);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticles(RetrievalRefreshFlag.ALL_PUBLICATIONS);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findAll();

		// Verify that retrieveArticlesByDateRange was called with correct parameters
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(eq(identityList), any(Date.class),
				any(Date.class), eq(RetrievalRefreshFlag.ALL_PUBLICATIONS));
	}

	@Test
	public void testRetrieveArticlesWithoutRefreshFlag() throws IOException {
		LocalDate now = LocalDate.now();
		// Arrange
		when(identityService.findAll()).thenReturn(identityList);

		LocalDate startDate = now.withDayOfMonth(1);
		LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticles(null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findAll();
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(eq(identityList),
				eq(Date.valueOf(startDate)), eq(Date.valueOf(endDate)), isNull());
	}

	@Test
	public void testRetrieveArticlesWhenIOExceptionOccurs() throws IOException {
		// Arrange
		when(identityService.findAll()).thenReturn(identityList);
		doThrow(new IOException("Junit Test exception")).when(aliasReCiterRetrievalEngine)
				.retrieveArticlesByDateRange(any(), any(), any(), any());

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticles(null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findAll();
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(any(), any(), any(), any());
	}

	@Test
	public void testRetrieveArticlesWithEmptyIdentityList() throws IOException {
		// Arrange
		when(identityService.findAll()).thenReturn(new ArrayList<>());

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticles(null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findAll();
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(eq(new ArrayList<>()),
				any(Date.class), any(Date.class), isNull());
	}

	@Test
	public void testRetrieveArticlesByUidIdentityNotFound() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(null);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid, null);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertTrue(response.getBody().toString().contains("not found in the Identity table"));
		verify(identityService, times(1)).findByUid(testUid);
	}

	@Test
	public void testRetrieveArticlesByUidUseCache() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(eSearchResultService.findByUid(testUid.trim())).thenReturn(testESearchResult);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid, RetrievalRefreshFlag.FALSE);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().toString().contains("cached results"));
		verify(identityService, times(1)).findByUid(testUid);
		verify(eSearchResultService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRetrieveArticlesByUidRefreshAllPublications() throws IOException {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(eSearchResultService.findByUid(testUid.trim())).thenReturn(testESearchResult);

		// This is causing the error -
		// aliasReCiterRetrievalEngine.retrieveArticlesByDateRange likely returns a
		// value
		// Replace doNothing() with when().thenReturn() pattern
		when(aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(anyList(), any(Date.class), any(Date.class),
				any(RetrievalRefreshFlag.class))).thenReturn(true);

		// If eSearchResultService.delete is void, this is fine
		doNothing().when(eSearchResultService).delete(anyString());

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid,
				RetrievalRefreshFlag.ALL_PUBLICATIONS);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody().toString().contains("Successfully retrieved all candidate articles"));
		verify(identityService, times(1)).findByUid(testUid);
		verify(eSearchResultService, times(1)).findByUid(testUid.trim());
		verify(eSearchResultService, times(1)).delete(testUid.trim());
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(anyList(), any(Date.class),
				any(Date.class), eq(RetrievalRefreshFlag.ALL_PUBLICATIONS));
	}

	@Test
	public void testRetrieveArticlesByUidRefreshAllPublicationsNoExistingESearchResult() throws IOException {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(eSearchResultService.findByUid(testUid.trim())).thenReturn(null);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid,
				RetrievalRefreshFlag.ALL_PUBLICATIONS);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findByUid(testUid);
		verify(eSearchResultService, times(1)).findByUid(testUid.trim());
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(anyList(), any(Date.class),
				any(Date.class), eq(RetrievalRefreshFlag.ALL_PUBLICATIONS));
	}

	@Test
	public void testRetrieveArticlesByUidOnlyNewPublications() throws IOException {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(eSearchResultService.findByUid(testUid.trim())).thenReturn(testESearchResult);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid,
				RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findByUid(testUid);
		verify(eSearchResultService, times(2)).findByUid(testUid.trim());
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(anyList(), any(Date.class),
				any(Date.class), eq(RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS));
	}

	@Test
	public void testRetrieveArticlesByUidOnlyNewPublicationsNoExistingESearchResult() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(eSearchResultService.findByUid(testUid.trim())).thenReturn(testESearchResult);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid,
				RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findByUid(testUid);
		verify(eSearchResultService, times(2)).findByUid(testUid.trim());
	}

	@Test
	public void testRetrieveArticlesByUidIOExceptionDuringRetrieval() throws IOException {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(eSearchResultService.findByUid(testUid.trim())).thenReturn(null);
		doThrow(new IOException("Test retrieval exception")).when(aliasReCiterRetrievalEngine)
				.retrieveArticlesByDateRange(anyList(), any(Date.class), any(Date.class),
						any(RetrievalRefreshFlag.class));

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid,
				RetrievalRefreshFlag.ALL_PUBLICATIONS);

		// Assert
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().toString().contains("failed to retrieve articles"));
		verify(identityService, times(1)).findByUid(testUid);
		verify(eSearchResultService, times(1)).findByUid(testUid.trim());
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(anyList(), any(Date.class),
				any(Date.class), any(RetrievalRefreshFlag.class));
	}

	@Test
	public void testRetrieveArticlesByUidNullRefreshFlagNoExistingESearchResult() throws IOException {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(eSearchResultService.findByUid(testUid.trim())).thenReturn(null);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveArticlesByUid(testUid, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(identityService, times(1)).findByUid(testUid);
		verify(eSearchResultService, times(1)).findByUid(testUid.trim());
		verify(aliasReCiterRetrievalEngine, times(1)).retrieveArticlesByDateRange(anyList(), any(Date.class),
				any(Date.class), eq(RetrievalRefreshFlag.ALL_PUBLICATIONS));
	}

	@Test
	public void testRetrieveBulkFeatureGeneratorWithUids() {
		// Arrange
		when(analysisService.findByUids(testUids)).thenReturn(analysisOutputList);

		// Act
		ResponseEntity<?> response = reCiterController.retrieveBulkFeatureGenerator(testUids, null, null, null,
				testScore, maxArticles);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody() instanceof List);
		List<ReCiterFeature> resultFeatures = (List<ReCiterFeature>) response.getBody();
		assertEquals(2, resultFeatures.size());
		// Verify each feature has max 5 articles (as specified by maxArticles)
		for (ReCiterFeature feature : resultFeatures) {
			assertTrue(feature.getReCiterArticleFeatures().size() <= maxArticles);
		}
		verify(analysisService, times(1)).findByUids(testUids);
	}

	@Test
	public void testRetrieveBulkFeatureGeneratorBadRequest() {
		// Act
		ResponseEntity<?> response = reCiterController.retrieveBulkFeatureGenerator(null, null, null, null, testScore,
				maxArticles);

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(identityService, never()).findAll();
		verify(analysisService, never()).findByUids(anyList());
	}

	@Test
	public void testRetrieveBulkFeatureGeneratorTooManyUids() {
		// Arrange
		List<String> tooManyUids = new ArrayList<>();
		for (int i = 0; i < 51; i++) {
			tooManyUids.add("test" + i);
		}

		// Act
		ResponseEntity<?> response = reCiterController.retrieveBulkFeatureGenerator(tooManyUids, null, null, null,
				testScore, maxArticles);

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(analysisService, never()).findByUids(anyList());
	}

	@Test
	public void testRetrieveBulkFeatureGeneratorEmptyIdentities() {
		// Arrange
		when(identityService.findAll()).thenReturn(Collections.emptyList());

		// Act
		ResponseEntity<?> response = reCiterController.retrieveBulkFeatureGenerator(null, personTypes, null, null,
				testScore, maxArticles);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		verify(identityService, times(1)).findAll();
		verify(analysisService, never()).findByUids(anyList());
	}

	@Test
	public void testRetrieveBulkFeatureGeneratorNoPublicationsFound() {
		// Arrange
		when(analysisService.findByUids(testUids)).thenReturn(Collections.emptyList());

		// Act
		ResponseEntity<?> response = reCiterController.retrieveBulkFeatureGenerator(testUids, null, null, null,
				testScore, maxArticles);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertTrue(response.getBody().toString().contains("There is no publications data for the group"));
		verify(analysisService, times(1)).findByUids(testUids);
	}

	@Test
	public void testRetrieveBulkFeatureGeneratorServiceException() {
		// Arrange
		when(identityService.findAll()).thenThrow(new RuntimeException("Database error"));

		// Act
		ResponseEntity<?> response = reCiterController.retrieveBulkFeatureGenerator(null, personTypes, null, null,
				testScore, maxArticles);

		// Assert
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertTrue(response.getBody().toString().contains("Issue with the request"));
	}

	@Test
	public void testRunFeatureGeneratorNoIdentityFound() {
		// Arrange
		String uid = "test989";
		when(identityService.findByUid(uid)).thenReturn(null);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(uid, null, null, null, false, null);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("The uid provided '" + uid + "' was not found in the Identity table", response.getBody());
		verify(identityService, times(1)).findByUid(uid);
	}

	@Test
	public void testRunFeatureGeneratorExistingAnalysisNoRefresh() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(analysisService.findByUid(testUid)).thenReturn(analysisOutput);
		when(dynamoDbGoldStandardService.findByUid(testUid)).thenReturn(goldStandard);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(testUid, testScore,
				UseGoldStandard.AS_EVIDENCE, FilterFeedbackType.ALL, false, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		ReCiterFeature result = (ReCiterFeature) response.getBody();

		// Should include articles that are:
		// 1. Above score threshold with NULL feedback OR
		// 2. Any score with ACCEPTED feedback OR
		// 3. Any score with REJECTED feedback
		assertEquals(4, result.getReCiterArticleFeatures().size()); // in for loop one also +1 total 1+3

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid);
		verify(dynamoDbGoldStandardService, times(1)).findByUid(testUid);
	}

	@Test
	public void testRunFeatureGeneratorExistingAnalysisAcceptedOnly() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(analysisService.findByUid(testUid)).thenReturn(analysisOutput);
		when(dynamoDbGoldStandardService.findByUid(testUid)).thenReturn(goldStandard);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(testUid, testScore,
				UseGoldStandard.AS_EVIDENCE, FilterFeedbackType.ACCEPTED_ONLY, false, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		ReCiterFeature result = (ReCiterFeature) response.getBody();

		assertEquals(1, result.getReCiterArticleFeatures().size());
		assertEquals(PublicationFeedback.ACCEPTED, result.getReCiterArticleFeatures().get(0).getUserAssertion());
		assertEquals(23456L, result.getReCiterArticleFeatures().get(0).getPmid());
	}

	@Test
	public void testRunFeatureGeneratorExistingAnalysisRejectedOnly() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(analysisService.findByUid(testUid)).thenReturn(analysisOutput);
		when(dynamoDbGoldStandardService.findByUid(testUid)).thenReturn(goldStandard);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(testUid, testScore,
				UseGoldStandard.AS_EVIDENCE, FilterFeedbackType.REJECTED_ONLY, false, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		ReCiterFeature result = (ReCiterFeature) response.getBody();

		assertEquals(1, result.getReCiterArticleFeatures().size());
		assertEquals(PublicationFeedback.REJECTED, result.getReCiterArticleFeatures().get(0).getUserAssertion());
		assertEquals(34567L, result.getReCiterArticleFeatures().get(0).getPmid());
	}

	@Test
	public void testRunFeatureGeneratorExistingAnalysisAcceptedAndNull() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(analysisService.findByUid(testUid)).thenReturn(analysisOutput);
		when(dynamoDbGoldStandardService.findByUid(testUid)).thenReturn(goldStandard);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(testUid, testScore,
				UseGoldStandard.AS_EVIDENCE, FilterFeedbackType.ACCEPTED_AND_NULL, false, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		ReCiterFeature result = (ReCiterFeature) response.getBody();

		// Should include articles that are:
		// 1. Above score threshold with NULL feedback OR here two null so 2+1
		// 2. Any score with ACCEPTED feedback
		assertEquals(3, result.getReCiterArticleFeatures().size());

		// Verify the PMIDs of included articles
		List<Long> pmids = new ArrayList<>();
		for (ReCiterArticleFeature article : result.getReCiterArticleFeatures()) {
			pmids.add(article.getPmid());
		}

		assertTrue(pmids.contains(12345L)); // NULL feedback above threshold
		assertTrue(pmids.contains(23456L)); // ACCEPTED feedback
		assertTrue(pmids.contains(45678L));// NULL feedback above threshold
	}

	@Test
	public void testRunFeatureGeneratorExistingAnalysisRejectedAndNull() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(analysisService.findByUid(testUid)).thenReturn(analysisOutput);
		when(dynamoDbGoldStandardService.findByUid(testUid)).thenReturn(goldStandard);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(testUid, testScore,
				UseGoldStandard.AS_EVIDENCE, FilterFeedbackType.REJECTED_AND_NULL, false, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		ReCiterFeature result = (ReCiterFeature) response.getBody();

		// Should include articles that are:
		// 1. Above score threshold with NULL feedback OR
		// 2. Any score with REJECTED feedback
		assertEquals(3, result.getReCiterArticleFeatures().size());

		// Verify the PMIDs of included articles
		List<Long> pmids = new ArrayList<>();
		for (ReCiterArticleFeature article : result.getReCiterArticleFeatures()) {
			pmids.add(article.getPmid());
		}

		assertTrue(pmids.contains(45678L)); // NULL feedback above threshold
		assertTrue(pmids.contains(12345L)); // NULL feedback above threshold
		assertTrue(pmids.contains(34567L)); // REJECTED feedback
	}

	@Test
	public void testRunFeatureGeneratorExistingAnalysisAcceptedAndRejected() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(analysisService.findByUid(testUid)).thenReturn(analysisOutput);
		when(dynamoDbGoldStandardService.findByUid(testUid)).thenReturn(goldStandard);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(testUid, testScore,
				UseGoldStandard.AS_EVIDENCE, FilterFeedbackType.ACCEPTED_AND_REJECTED, false, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		ReCiterFeature result = (ReCiterFeature) response.getBody();

		// Should include articles with ACCEPTED or REJECTED feedback
		assertEquals(2, result.getReCiterArticleFeatures().size());

		// Verify the PMIDs of included articles
		List<Long> pmids = new ArrayList<>();
		for (ReCiterArticleFeature article : result.getReCiterArticleFeatures()) {
			pmids.add(article.getPmid());
		}

		assertTrue(pmids.contains(23456L)); // ACCEPTED feedback
		assertTrue(pmids.contains(34567L)); // REJECTED feedback
	}

	@Test
	public void testRunFeatureGeneratorExistingAnalysisNullOnly() {
		// Arrange
		when(identityService.findByUid(testUid)).thenReturn(identity);
		when(analysisService.findByUid(testUid)).thenReturn(analysisOutput);
		when(dynamoDbGoldStandardService.findByUid(testUid)).thenReturn(goldStandard);

		// Act
		ResponseEntity<?> response = reCiterController.runFeatureGenerator(testUid, testScore,
				UseGoldStandard.AS_EVIDENCE, FilterFeedbackType.NULL, false, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		ReCiterFeature result = (ReCiterFeature) response.getBody();

		// Should include only articles with NULL feedback and score above threshold
		assertEquals(2, result.getReCiterArticleFeatures().size());
		assertEquals(12345L, result.getReCiterArticleFeatures().get(0).getPmid());
		assertEquals(PublicationFeedback.NULL, result.getReCiterArticleFeatures().get(0).getUserAssertion());
	}

	@Test
	public void testRunArticleRetrievalByUidIdentityNotFound() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(null);

		// Act
		ResponseEntity<ReCiterFeature> response = reCiterController.runArticleRetrievalByUid(testUid, null, null);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("The uid provided '" + testUid + "' was not found in the Identity table", response.getBody());
		verify(identityService, times(1)).findByUid(testUid);
	}

	@Test
	public void testRunArticleRetrievalByUidNullPointerException() {
		// Arrange
		when(identityService.findByUid(anyString())).thenThrow(new NullPointerException());

		// Act
		ResponseEntity<ReCiterFeature> response = reCiterController.runArticleRetrievalByUid(testUid, null, null);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("The uid provided '" + testUid + "' was not found in the Identity table", response.getBody());
		verify(identityService, times(1)).findByUid(testUid);
	}

	@Test
	public void testRunArticleRetrievalByUidNoAnalysisDataFound() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(null);

		// Act
		ResponseEntity<ReCiterFeature> response = reCiterController.runArticleRetrievalByUid(testUid, null, null);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("There is no publications data for uid " + testUid
				+ ". Please wait while feature-generator re-runs tonight.", response.getBody());
		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidDefaultFilter() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: articles with score >= threshold and NULL feedback, or any
		// feedback (ACCEPTED or REJECTED)
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> (article.getAuthorshipLikelihoodScore() >= defaultScoreThreshold
						&& article.getUserAssertion() == PublicationFeedback.NULL)
						|| article.getUserAssertion() == PublicationFeedback.ACCEPTED
						|| article.getUserAssertion() == PublicationFeedback.REJECTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		// Verify evidence field is null
		for (ReCiterArticleFeature feature : response.getBody().getReCiterArticleFeatures()) {
			assertEquals(null, feature.getEvidence());
		}

		// Verify metrics fields are null
		assertEquals(null, response.getBody().getInGoldStandardButNotRetrieved());
		assertEquals(null, response.getBody().getPrecision());
		assertEquals(null, response.getBody().getRecall());
		assertEquals(null, response.getBody().getOverallAccuracy());

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidCustomScoreThreshold() {
		// Arrange
		Double customScoreThreshold = 0.5;
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: articles with score >= customScoreThreshold and NULL
		// feedback, or any feedback (ACCEPTED or REJECTED)
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> (article.getAuthorshipLikelihoodScore() >= customScoreThreshold
						&& article.getUserAssertion() == PublicationFeedback.NULL)
						|| article.getUserAssertion() == PublicationFeedback.ACCEPTED
						|| article.getUserAssertion() == PublicationFeedback.REJECTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, customScoreThreshold, null);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidFilterAcceptedOnly() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: only ACCEPTED articles
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> article.getUserAssertion() == PublicationFeedback.ACCEPTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, FilterFeedbackType.ACCEPTED_ONLY);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		// Verify all articles have ACCEPTED feedback
		for (ReCiterArticleFeature feature : response.getBody().getReCiterArticleFeatures()) {
			assertEquals(PublicationFeedback.ACCEPTED, feature.getUserAssertion());
		}

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidFilterRejectedOnly() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: only REJECTED articles
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> article.getUserAssertion() == PublicationFeedback.REJECTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, FilterFeedbackType.REJECTED_ONLY);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		// Verify all articles have REJECTED feedback
		for (ReCiterArticleFeature feature : response.getBody().getReCiterArticleFeatures()) {
			assertEquals(PublicationFeedback.REJECTED, feature.getUserAssertion());
		}

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidFilterAcceptedAndNull() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: articles with score >= threshold and NULL feedback, or
		// ACCEPTED
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> (article.getAuthorshipLikelihoodScore() >= defaultScoreThreshold
						&& article.getUserAssertion() == PublicationFeedback.NULL)
						|| article.getUserAssertion() == PublicationFeedback.ACCEPTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, FilterFeedbackType.ACCEPTED_AND_NULL);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidFilterRejectedAndNull() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: articles with score >= threshold and NULL feedback, or
		// REJECTED
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> (article.getAuthorshipLikelihoodScore() >= defaultScoreThreshold
						&& article.getUserAssertion() == PublicationFeedback.NULL)
						|| article.getUserAssertion() == PublicationFeedback.REJECTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, FilterFeedbackType.REJECTED_AND_NULL);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidFilterAcceptedAndRejected() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: only ACCEPTED or REJECTED articles
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> article.getUserAssertion() == PublicationFeedback.ACCEPTED
						|| article.getUserAssertion() == PublicationFeedback.REJECTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, FilterFeedbackType.ACCEPTED_AND_REJECTED);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidFilterNull() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: only NULL feedback articles with score >= threshold
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> article.getAuthorshipLikelihoodScore() >= defaultScoreThreshold
						&& article.getUserAssertion() == PublicationFeedback.NULL)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, FilterFeedbackType.NULL);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		// Verify all articles have NULL feedback
		for (ReCiterArticleFeature feature : response.getBody().getReCiterArticleFeatures()) {
			assertEquals(PublicationFeedback.NULL, feature.getUserAssertion());
		}

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

	@Test
	public void testRunArticleRetrievalByUidAllFilterType() {
		// Arrange
		when(identityService.findByUid(anyString())).thenReturn(identity);
		when(analysisService.findByUid(anyString())).thenReturn(analysisOutput);

		// Expected results: same as default filter - articles with score >= threshold
		// and NULL feedback, or any feedback
		List<ReCiterArticleFeature> expectedFeatures = articleFeatures.stream()
				.filter(article -> (article.getAuthorshipLikelihoodScore() >= defaultScoreThreshold
						&& article.getUserAssertion() == PublicationFeedback.NULL)
						|| article.getUserAssertion() == PublicationFeedback.ACCEPTED
						|| article.getUserAssertion() == PublicationFeedback.REJECTED)
				.collect(Collectors.toList());

		// Act
		ResponseEntity<ReCiterFeature> response = (ResponseEntity<ReCiterFeature>) reCiterController
				.runArticleRetrievalByUid(testUid, null, FilterFeedbackType.ALL);

		// Assert
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(expectedFeatures.size(), response.getBody().getReCiterArticleFeatures().size());

		verify(identityService, times(1)).findByUid(testUid);
		verify(analysisService, times(1)).findByUid(testUid.trim());
	}

}
