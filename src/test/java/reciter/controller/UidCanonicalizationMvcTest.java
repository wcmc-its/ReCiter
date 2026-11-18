package reciter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.database.dynamodb.model.FeedbackLog;
import reciter.service.AnalysisService;
import reciter.service.ESearchResultService;
import reciter.service.ExternalArticleService;
import reciter.service.FeedbackLogService;
import reciter.service.IdentityService;
import reciter.service.dynamo.FeedbackLogQueryService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;

/**
 * Uids are canonicalized (trimmed, lowercased unless an external cohort id) on
 * the way in, and only what is still invalid afterwards is rejected with 400.
 * Standalone MockMvc so the controller advice mapping is exercised.
 */
@ExtendWith(MockitoExtension.class)
public class UidCanonicalizationMvcTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

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
	@Mock
	private ESearchResultService eSearchResultService;
	@Mock
	private FeedbackLogQueryService feedbackLogQueryService;

	@InjectMocks
	private IdentityController identityController;
	@InjectMocks
	private ReCiterController reCiterController;
	@InjectMocks
	private ExternalArticleController externalArticleController;

	@BeforeEach
	public void setUp() {
		ReflectionTestUtils.setField(identityController, "mandatoryFields", "firstName,lastName,firstInitial");
		ReflectionTestUtils.setField(reCiterController, "uidsMaxCount", 100);
	}

	private MockMvc mockMvc(Object controller) {
		return MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build();
	}

	private static Identity identity(String uid) {
		Identity identity = new Identity();
		identity.setUid(uid);
		AuthorName name = new AuthorName();
		name.setFirstName("Jane");
		name.setLastName("Doe");
		name.setFirstInitial("J");
		identity.setAlternateNames(new ArrayList<>(Collections.singletonList(name)));
		return identity;
	}

	@Test
	public void postIdentityStoresCanonicalUid() throws Exception {
		mockMvc(identityController).perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(identity(" Abl9002 ")))).andExpect(status().isOk());

		ArgumentCaptor<Identity> saved = ArgumentCaptor.forClass(Identity.class);
		verify(identityService).save(saved.capture());
		assertEquals("abl9002", saved.getValue().getUid());
	}

	@Test
	public void postIdentityKeepsExternalCohortCase() throws Exception {
		mockMvc(identityController).perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(identity("fredhutch_Rachel.Rau ")))).andExpect(status().isOk());

		ArgumentCaptor<Identity> saved = ArgumentCaptor.forClass(Identity.class);
		verify(identityService).save(saved.capture());
		assertEquals("fredhutch_Rachel.Rau", saved.getValue().getUid());
	}

	@Test
	public void postIdentityWithEmailUidIsBadRequest() throws Exception {
		mockMvc(identityController)
				.perform(post("/reciter/identity/").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(identity("kims7@mskcc.org"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value(Matchers.containsString("kims7@mskcc.org")));

		verify(identityService, never()).save(any(Identity.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void putIdentitiesSavesOnlyValidItemsUnderCanonicalUid() throws Exception {
		Identity noFirstName = identity("ok1234");
		noFirstName.getAlternateNames().get(0).setFirstName(" ");
		List<Identity> body = Arrays.asList(identity("ggi9001 "), identity("Mahender Jangari"), noFirstName,
				identity("HAF9018"));

		mockMvc(identityController).perform(put("/reciter/save/identities/").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))).andExpect(status().isOk())
				.andExpect(jsonPath("$.received").value(4)).andExpect(jsonPath("$.saved").value(2))
				.andExpect(jsonPath("$.skipped.length()").value(2))
				.andExpect(jsonPath("$.skipped[0].uid").value("Mahender Jangari"))
				.andExpect(jsonPath("$.skipped[1].uid").value("ok1234"));

		ArgumentCaptor<List<Identity>> saved = ArgumentCaptor.forClass(List.class);
		verify(identityService).save(saved.capture());
		assertEquals(Arrays.asList("ggi9001", "haf9018"),
				saved.getValue().stream().map(Identity::getUid).collect(java.util.stream.Collectors.toList()));
	}

	@Test
	public void putIdentitiesWithNoValidItemsSavesNothing() throws Exception {
		mockMvc(identityController).perform(put("/reciter/save/identities/").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Collections.singletonList(identity("Hello, world!")))))
				.andExpect(status().isOk());

		verify(identityService, never()).save(anyList());
	}

	@Test
	public void identityLookupUsesCanonicalUid() throws Exception {
		when(identityService.findByUid("dlamb")).thenReturn(identity("dlamb"));

		mockMvc(identityController).perform(get("/reciter/find/identity/by/uid").param("uid", "DLAMB"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.uid").value("dlamb"));
	}

	@Test
	public void identityBatchLookupDropsInvalidUids() throws Exception {
		when(identityService.findByUids(Collections.singletonList("abl9002")))
				.thenReturn(Collections.singletonList(identity("abl9002")));

		mockMvc(identityController)
				.perform(post("/reciter/find/identities/by/uids/").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Arrays.asList("Abl9002", "a b"))))
				.andExpect(status().isOk()).andExpect(jsonPath("$[0].uid").value("abl9002"));
	}

	@Test
	public void identityBatchGetLookupDropsInvalidUids() throws Exception {
		when(identityService.findByUids(Collections.singletonList("abl9002")))
				.thenReturn(Collections.singletonList(identity("abl9002")));

		mockMvc(identityController).perform(get("/reciter/find/identity/by/uids/").param("uids", "Abl9002")
				.param("uids", "kims7@mskcc.org")).andExpect(status().isOk());

		verify(identityService).findByUids(Collections.singletonList("abl9002"));
	}

	@Test
	public void eSearchResultLookupUsesCanonicalUid() throws Exception {
		mockMvc(reCiterController).perform(get("/reciter/esearchresult/{uid}", "Arn4002"))
				.andExpect(status().isNotFound());

		verify(eSearchResultService).findByUid("arn4002");
	}

	@Test
	public void feedbackLogLookupUsesCanonicalUid() throws Exception {
		when(feedbackLogQueryService.getAuditHistory("arn4002")).thenReturn(Collections.emptyList());

		mockMvc(reCiterController).perform(get("/reciter/feedback-log/{uid}", "Arn4002"))
				.andExpect(status().isOk());

		verify(feedbackLogQueryService).getAuditHistory("arn4002");
	}

	@Test
	public void postGoldStandardSavesUnderCanonicalUid() throws Exception {
		GoldStandard gs = new GoldStandard();
		gs.setUid("SHP2010 ");
		gs.setKnownPmids(new ArrayList<>(Collections.singletonList(1L)));

		mockMvc(reCiterController).perform(post("/reciter/goldstandard").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(gs))).andExpect(status().isOk());

		ArgumentCaptor<GoldStandard> saved = ArgumentCaptor.forClass(GoldStandard.class);
		verify(dynamoDbGoldStandardService).save(saved.capture(), eq(GoldStandardUpdateFlag.UPDATE), any(), any(),
				anyInt());
		assertEquals("shp2010", saved.getValue().getUid());
		verify(externalArticleService).reconcileWithGoldStandard("shp2010");
	}

	@Test
	public void putGoldStandardWithInvalidUidWritesNothing() throws Exception {
		GoldStandard ok = new GoldStandard();
		ok.setUid("Jod2033");
		GoldStandard bad = new GoldStandard();
		bad.setUid("tfm9003@nyp.org");

		mockMvc(reCiterController).perform(put("/reciter/goldstandard").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Arrays.asList(ok, bad)))).andExpect(status().isBadRequest());

		verify(dynamoDbGoldStandardService, never()).save(anyList(), any(), any(), any());
	}

	@Test
	public void goldStandardLookupUsesCanonicalUid() throws Exception {
		GoldStandard gs = new GoldStandard();
		gs.setUid("hlloyd");
		when(dynamoDbGoldStandardService.findByUid("hlloyd")).thenReturn(gs);

		mockMvc(reCiterController).perform(get("/reciter/goldstandard/{uid}", "Hlloyd")).andExpect(status().isOk())
				.andExpect(jsonPath("$.uid").value("hlloyd"));
	}

	@Test
	public void featureGeneratorLooksUpCanonicalUid() throws Exception {
		when(identityService.findByUid("arn4002")).thenReturn(null);

		mockMvc(reCiterController).perform(get("/reciter/feature-generator/by/uid").param("uid", " Arn4002"))
				.andExpect(status().isNotFound());

		verify(identityService).findByUid("arn4002");
	}

	@Test
	public void featureGeneratorWithInvalidUidIsBadRequest() throws Exception {
		mockMvc(reCiterController).perform(get("/reciter/feature-generator/by/uid").param("uid", "kims7@mskcc.org"))
				.andExpect(status().isBadRequest());

		verify(identityService, never()).findByUid(anyString());
	}

	@Test
	public void featureGeneratorGroupWithInvalidUidIsBadRequest() throws Exception {
		mockMvc(reCiterController)
				.perform(post("/reciter/feature-generator/by/group").param("totalStandardizedArticleScore", "30")
						.param("maxArticlesPerPerson", "10").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(Arrays.asList("abl9002", "Hello, world!"))))
				.andExpect(status().isBadRequest());

		verify(analysisService, never()).findByUids(anyList());
	}

	@Test
	public void externalArticleListUsesCanonicalUid() throws Exception {
		when(externalArticleService.findByUid("abl9002")).thenReturn(Collections.emptyList());

		mockMvc(externalArticleController).perform(get("/reciter/external-article/by/uid").param("uid", "Abl9002 "))
				.andExpect(status().isOk());

		verify(externalArticleService).findByUid("abl9002");
	}

	@Test
	public void externalArticleDeleteLogsCanonicalActor() throws Exception {
		ExternalArticle existing = new ExternalArticle();
		existing.setUid("abl9002");
		existing.setArticleId("SCOPUS:1");
		when(externalArticleService.find("abl9002", "SCOPUS:1")).thenReturn(existing);

		mockMvc(externalArticleController).perform(delete("/reciter/external-article/by/uid").param("uid", "Abl9002")
				.param("articleId", "SCOPUS:1").param("actorPersonIdentifier", " PAA2013 ")).andExpect(status().isOk());

		ArgumentCaptor<FeedbackLog> logged = ArgumentCaptor.forClass(FeedbackLog.class);
		verify(feedbackLogService).recordAction(logged.capture());
		assertEquals("abl9002", logged.getValue().getUid());
		assertEquals("paa2013", logged.getValue().getActorPersonIdentifier());
	}

	@Test
	public void externalArticleDeleteWithoutActorStillWorks() throws Exception {
		ExternalArticle existing = new ExternalArticle();
		existing.setUid("abl9002");
		existing.setArticleId("SCOPUS:1");
		when(externalArticleService.find("abl9002", "SCOPUS:1")).thenReturn(existing);

		mockMvc(externalArticleController).perform(delete("/reciter/external-article/by/uid").param("uid", "abl9002")
				.param("articleId", "SCOPUS:1")).andExpect(status().isOk());

		ArgumentCaptor<FeedbackLog> logged = ArgumentCaptor.forClass(FeedbackLog.class);
		verify(feedbackLogService).recordAction(logged.capture());
		assertEquals(null, logged.getValue().getActorPersonIdentifier());
	}

	@Test
	public void externalArticleFeedbackLogsCanonicalActor() throws Exception {
		when(identityService.findByUid("dnr4021")).thenReturn(identity("dnr4021"));
		when(feedbackLogService.recordAction(any(FeedbackLog.class))).thenReturn(true);

		mockMvc(externalArticleController).perform(patch("/reciter/external-article/feedback")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uid\":\"DNR4021\",\"articleId\":\"SCOPUS:85142207731\",\"action\":\"REJECTED\","
						+ "\"actorPersonIdentifier\":\"Paa2013 \"}"))
				.andExpect(status().isOk());

		ArgumentCaptor<FeedbackLog> logged = ArgumentCaptor.forClass(FeedbackLog.class);
		verify(feedbackLogService).recordAction(logged.capture());
		assertEquals("dnr4021", logged.getValue().getUid());
		assertEquals("paa2013", logged.getValue().getActorPersonIdentifier());
	}

	@Test
	public void externalArticleFeedbackWithInvalidActorIsBadRequest() throws Exception {
		mockMvc(externalArticleController).perform(patch("/reciter/external-article/feedback")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"uid\":\"dnr4021\",\"articleId\":\"SCOPUS:85142207731\",\"action\":\"REJECTED\","
						+ "\"actorPersonIdentifier\":\"paa2013@med.cornell.edu\"}"))
				.andExpect(status().isBadRequest());

		verify(feedbackLogService, never()).recordAction(any());
	}

	@Test
	public void externalArticleAddWithInvalidUidIsBadRequest() throws Exception {
		mockMvc(externalArticleController)
				.perform(post("/reciter/external-article/by/uid").param("uid", "a@b").contentType(MediaType.APPLICATION_JSON)
						.content("{\"articleId\":\"SCOPUS:1\"}"))
				.andExpect(status().isBadRequest());

		verify(identityService, never()).findByUid(anyString());
		verify(externalArticleService, never()).save(any());
	}
}
