package reciter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.database.dynamodb.model.FeedbackLog;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.service.AnalysisService;
import reciter.service.ESearchResultService;
import reciter.service.ExternalArticleService;
import reciter.service.FeedbackLogService;
import reciter.service.IdentityService;
import reciter.service.dynamo.FeedbackLogQueryService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;

/**
 * Uids are canonicalized (stripped, lowercased unless an external cohort id) on
 * the way in, and only what is still invalid afterwards is rejected with 400.
 * Standalone MockMvc so the controller advice mapping is exercised. One nested
 * class per controller, each with only that controller's mocks and a MockMvc
 * built once per test in {@code @BeforeEach}.
 */
@ExtendWith(MockitoExtension.class)
class UidCanonicalizationMvcTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static MockMvc mockMvc(Object controller) {
		return MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build();
	}

	private static Identity identity(String uid) {
		Identity identity = new Identity();
		identity.setUid(uid);
		AuthorName name = new AuthorName();
		name.setFirstName("Jane");
		name.setLastName("Doe");
		name.setFirstInitial("J");
		identity.setAlternateNames(new ArrayList<>(List.of(name)));
		return identity;
	}

	@Nested
	class IdentityControllerCanonicalization {

		@Mock
		private IdentityService identityService;
		@InjectMocks
		private IdentityController identityController;
		@Captor
		private ArgumentCaptor<Identity> savedIdentity;
		@Captor
		private ArgumentCaptor<List<Identity>> savedIdentities;

		private MockMvc mvc;

		@BeforeEach
		void setUp() {
			ReflectionTestUtils.setField(identityController, "mandatoryFields", "firstName,lastName,firstInitial");
			mvc = mockMvc(identityController);
		}

		@Test
		void postIdentityStoresCanonicalUid() throws Exception {
			mvc.perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(identity(" Abl9002 ")))).andExpect(status().isOk());

			verify(identityService).save(savedIdentity.capture());
			assertEquals("abl9002", savedIdentity.getValue().getUid());
		}

		@Test
		void postIdentityKeepsExternalCohortCase() throws Exception {
			mvc.perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(identity("fredhutch_Rachel.Rau "))))
					.andExpect(status().isOk());

			verify(identityService).save(savedIdentity.capture());
			assertEquals("fredhutch_Rachel.Rau", savedIdentity.getValue().getUid());
		}

		@Test
		void postIdentityWithEmailUidIsBadRequest() throws Exception {
			mvc.perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(identity("kims7@mskcc.org"))))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value(Matchers.containsString("kims7@mskcc.org")));

			verify(identityService, never()).save(any(Identity.class));
		}

		@Test
		void postIdentityWithBareCohortPrefixIsBadRequest() throws Exception {
			mvc.perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(identity("usc_")))).andExpect(status().isBadRequest());

			verify(identityService, never()).save(any(Identity.class));
		}

		@Test
		void badRequestDetailHasNoLineBreaksFromUid() throws Exception {
			mvc.perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(identity("abc\r\n[ERROR] forged"))))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.detail").value(Matchers.containsString("abc__[ERROR] forged")))
					.andExpect(jsonPath("$.detail").value(Matchers.not(Matchers.containsString("\n"))));
		}

		@Test
		void putIdentitiesSavesOnlyValidItemsUnderCanonicalUid() throws Exception {
			Identity noFirstName = identity("ok1234");
			noFirstName.getAlternateNames().get(0).setFirstName(" ");
			List<Identity> body = List.of(identity("ggi9001 "), identity("Mahender Jangari"), noFirstName,
					identity("HAF9018"));

			mvc.perform(put("/reciter/save/identities/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(body))).andExpect(status().isOk())
					.andExpect(jsonPath("$.received").value(4)).andExpect(jsonPath("$.saved").value(2))
					.andExpect(jsonPath("$.skipped.length()").value(2))
					.andExpect(jsonPath("$.skipped[0].uid").value("Mahender Jangari"))
					.andExpect(jsonPath("$.skipped[1].uid").value("ok1234"));

			verify(identityService).save(savedIdentities.capture());
			assertEquals(List.of("ggi9001", "haf9018"),
					savedIdentities.getValue().stream().map(Identity::getUid).toList());
		}

		@Test
		void putIdentitiesWithNoValidItemsSavesNothing() throws Exception {
			mvc.perform(put("/reciter/save/identities/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(List.of(identity("Hello, world!")))))
					.andExpect(status().isOk());

			verify(identityService, never()).save(anyList());
		}

		@Test
		void identityLookupUsesCanonicalUid() throws Exception {
			when(identityService.findByUid("dlamb")).thenReturn(identity("dlamb"));

			mvc.perform(get("/reciter/find/identity/by/uid").param("uid", "DLAMB")).andExpect(status().isOk())
					.andExpect(jsonPath("$.uid").value("dlamb"));
		}

		@Test
		void identityBatchLookupDropsInvalidUids() throws Exception {
			when(identityService.findByUids(List.of("abl9002"))).thenReturn(List.of(identity("abl9002")));

			mvc.perform(post("/reciter/find/identities/by/uids/").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(List.of("Abl9002", "a b")))).andExpect(status().isOk())
					.andExpect(jsonPath("$[0].uid").value("abl9002"));
		}

		@Test
		void identityBatchGetLookupDropsInvalidUids() throws Exception {
			when(identityService.findByUids(List.of("abl9002"))).thenReturn(List.of(identity("abl9002")));

			mvc.perform(get("/reciter/find/identity/by/uids/").param("uids", "Abl9002").param("uids",
					"kims7@mskcc.org")).andExpect(status().isOk());

			verify(identityService).findByUids(List.of("abl9002"));
		}
	}

	@Nested
	class ReCiterControllerCanonicalization {

		@Mock
		private IdentityService identityService;
		@Mock
		private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
		@Mock
		private AnalysisService analysisService;
		@Mock
		private ExternalArticleService externalArticleService;
		@Mock
		private ESearchResultService eSearchResultService;
		@Mock
		private FeedbackLogQueryService feedbackLogQueryService;
		@InjectMocks
		private ReCiterController reCiterController;
		@Captor
		private ArgumentCaptor<GoldStandard> savedGoldStandard;

		private MockMvc mvc;

		@BeforeEach
		void setUp() {
			ReflectionTestUtils.setField(reCiterController, "uidsMaxCount", 100);
			mvc = mockMvc(reCiterController);
		}

		@Test
		void eSearchResultLookupUsesCanonicalUid() throws Exception {
			mvc.perform(get("/reciter/esearchresult/{uid}", "Arn4002")).andExpect(status().isNotFound());

			verify(eSearchResultService).findByUid("arn4002");
		}

		@Test
		void feedbackLogLookupUsesCanonicalUid() throws Exception {
			when(feedbackLogQueryService.getAuditHistory("arn4002")).thenReturn(List.of());

			mvc.perform(get("/reciter/feedback-log/{uid}", "Arn4002")).andExpect(status().isOk());

			verify(feedbackLogQueryService).getAuditHistory("arn4002");
		}

		@Test
		void postGoldStandardSavesUnderCanonicalUid() throws Exception {
			GoldStandard gs = new GoldStandard();
			gs.setUid("SHP2010 ");
			gs.setKnownPmids(new ArrayList<>(List.of(1L)));

			mvc.perform(post("/reciter/goldstandard").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(gs))).andExpect(status().isOk());

			verify(dynamoDbGoldStandardService).save(savedGoldStandard.capture(), eq(GoldStandardUpdateFlag.UPDATE),
					any(), any(), anyInt());
			assertEquals("shp2010", savedGoldStandard.getValue().getUid());
			verify(externalArticleService).reconcileWithGoldStandard("shp2010");
		}

		@Test
		void putGoldStandardWithInvalidUidWritesNothing() throws Exception {
			GoldStandard ok = new GoldStandard();
			ok.setUid("Jod2033");
			GoldStandard bad = new GoldStandard();
			bad.setUid("tfm9003@nyp.org");

			mvc.perform(put("/reciter/goldstandard").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(List.of(ok, bad)))).andExpect(status().isBadRequest());

			verify(dynamoDbGoldStandardService, never()).save(anyList(), any(), any(), any());
		}

		@Test
		void goldStandardLookupUsesCanonicalUid() throws Exception {
			GoldStandard gs = new GoldStandard();
			gs.setUid("hlloyd");
			when(dynamoDbGoldStandardService.findByUid("hlloyd")).thenReturn(gs);

			mvc.perform(get("/reciter/goldstandard/{uid}", "Hlloyd")).andExpect(status().isOk())
					.andExpect(jsonPath("$.uid").value("hlloyd"));
		}

		@Test
		void featureGeneratorLooksUpCanonicalUid() throws Exception {
			when(identityService.findByUid("arn4002")).thenReturn(null);

			mvc.perform(get("/reciter/feature-generator/by/uid").param("uid", " Arn4002"))
					.andExpect(status().isNotFound());

			verify(identityService).findByUid("arn4002");
		}

		@Test
		void featureGeneratorWithInvalidUidIsBadRequest() throws Exception {
			mvc.perform(get("/reciter/feature-generator/by/uid").param("uid", "kims7@mskcc.org"))
					.andExpect(status().isBadRequest());

			verify(identityService, never()).findByUid(anyString());
		}

		@Test
		void featureGeneratorGroupWithInvalidUidIsBadRequest() throws Exception {
			mvc.perform(post("/reciter/feature-generator/by/group").param("totalStandardizedArticleScore", "30")
					.param("maxArticlesPerPerson", "10").contentType(MediaType.APPLICATION_JSON)
					.content(OBJECT_MAPPER.writeValueAsString(List.of("abl9002", "Hello, world!"))))
					.andExpect(status().isBadRequest());

			verify(analysisService, never()).findByUids(anyList());
		}

		@Test
		void featureGeneratorGroupWithoutUidsFallsBackToAllIdentities() throws Exception {
			// No uid body: canonicalize(null) is an empty list, so the group filters
			// apply to every identity exactly as before.
			when(identityService.findAll()).thenReturn(List.of());

			mvc.perform(post("/reciter/feature-generator/by/group").param("totalStandardizedArticleScore", "30")
					.param("maxArticlesPerPerson", "10").param("personType", "academic-faculty"))
					.andExpect(status().isNotFound());

			verify(identityService).findAll();
			verify(analysisService, never()).findByUids(anyList());
		}
	}

	@Nested
	class ExternalArticleControllerCanonicalization {

		@Mock
		private IdentityService identityService;
		@Mock
		private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
		@Mock
		private AnalysisService analysisService;
		@Mock
		private ExternalArticleService externalArticleService;
		@Mock
		private FeedbackLogService feedbackLogService;
		@InjectMocks
		private ExternalArticleController externalArticleController;
		@Captor
		private ArgumentCaptor<FeedbackLog> logged;

		private MockMvc mvc;

		@BeforeEach
		void setUp() {
			mvc = mockMvc(externalArticleController);
		}

		private void stubExisting(String uid, String articleId) {
			ExternalArticle existing = new ExternalArticle();
			existing.setUid(uid);
			existing.setArticleId(articleId);
			when(externalArticleService.find(uid, articleId)).thenReturn(existing);
		}

		@Test
		void externalArticleListUsesCanonicalUid() throws Exception {
			when(externalArticleService.findByUid("abl9002")).thenReturn(List.of());

			mvc.perform(get("/reciter/external-article/by/uid").param("uid", "Abl9002 ")).andExpect(status().isOk());

			verify(externalArticleService).findByUid("abl9002");
		}

		@Test
		void externalArticleDeleteLogsCanonicalActor() throws Exception {
			stubExisting("abl9002", "SCOPUS:1");

			mvc.perform(delete("/reciter/external-article/by/uid").param("uid", "Abl9002").param("articleId", "SCOPUS:1")
					.param("actorPersonIdentifier", " PAA2013 ")).andExpect(status().isOk());

			verify(feedbackLogService).recordAction(logged.capture());
			assertEquals("abl9002", logged.getValue().getUid());
			assertEquals("paa2013", logged.getValue().getActorPersonIdentifier());
		}

		@Test
		void externalArticleDeleteStripsArticleId() throws Exception {
			stubExisting("abl9002", "SCOPUS:1");

			mvc.perform(delete("/reciter/external-article/by/uid").param("uid", "abl9002").param("articleId",
					" SCOPUS:1 ")).andExpect(status().isOk());

			verify(externalArticleService).delete("abl9002", "SCOPUS:1");
		}

		@Test
		void externalArticleDeleteWithoutActorStillWorks() throws Exception {
			stubExisting("abl9002", "SCOPUS:1");

			mvc.perform(delete("/reciter/external-article/by/uid").param("uid", "abl9002").param("articleId",
					"SCOPUS:1")).andExpect(status().isOk());

			verify(feedbackLogService).recordAction(logged.capture());
			assertNull(logged.getValue().getActorPersonIdentifier());
		}

		@Test
		void externalArticleFeedbackLogsCanonicalActor() throws Exception {
			when(identityService.findByUid("dnr4021")).thenReturn(identity("dnr4021"));
			when(feedbackLogService.recordAction(any(FeedbackLog.class))).thenReturn(true);

			mvc.perform(patch("/reciter/external-article/feedback").contentType(MediaType.APPLICATION_JSON).content("""
					{"uid": "DNR4021", "articleId": "SCOPUS:85142207731", "action": "REJECTED",
					 "actorPersonIdentifier": "Paa2013 "}
					""")).andExpect(status().isOk());

			verify(feedbackLogService).recordAction(logged.capture());
			assertEquals("dnr4021", logged.getValue().getUid());
			assertEquals("paa2013", logged.getValue().getActorPersonIdentifier());
		}

		@Test
		void externalArticleFeedbackWithInvalidActorIsBadRequest() throws Exception {
			mvc.perform(patch("/reciter/external-article/feedback").contentType(MediaType.APPLICATION_JSON).content("""
					{"uid": "dnr4021", "articleId": "SCOPUS:85142207731", "action": "REJECTED",
					 "actorPersonIdentifier": "paa2013@med.cornell.edu"}
					""")).andExpect(status().isBadRequest());

			verify(feedbackLogService, never()).recordAction(any());
		}

		@Test
		void externalArticleAddWithInvalidUidIsBadRequest() throws Exception {
			mvc.perform(post("/reciter/external-article/by/uid").param("uid", "a@b")
					.contentType(MediaType.APPLICATION_JSON).content("""
							{"articleId": "SCOPUS:1"}
							""")).andExpect(status().isBadRequest());

			verify(identityService, never()).findByUid(anyString());
			verify(externalArticleService, never()).save(any());
		}
	}
}
