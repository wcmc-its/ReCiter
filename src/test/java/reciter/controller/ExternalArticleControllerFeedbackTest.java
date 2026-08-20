package reciter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reciter.controller.ExternalArticleController.FeedbackRequest;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.database.dynamodb.model.FeedbackLog;
import reciter.model.identity.Identity;
import reciter.service.AnalysisService;
import reciter.service.ExternalArticleService;
import reciter.service.FeedbackLogService;
import reciter.service.IdentityService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;

/** Covers PATCH /reciter/external-article/feedback (ACCEPTED/REJECTED/PENDING via FeedbackLog). */
@ExtendWith(MockitoExtension.class)
public class ExternalArticleControllerFeedbackTest {

    @Mock
    private ExternalArticleService externalArticleService;
    @Mock
    private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
    @Mock
    private AnalysisService analysisService;
    @Mock
    private IdentityService identityService;
    @Mock
    private FeedbackLogService feedbackLogService;

    @InjectMocks
    private ExternalArticleController externalArticleController;

    private ExternalArticle existing;

    @BeforeEach
    public void setUp() {
        existing = new ExternalArticle();
        existing.setUid("dnr4021");
        existing.setArticleId("SCOPUS:85142207731");
        existing.setSuppressed(Boolean.FALSE);
    }

    private FeedbackRequest request(String action, String actor) {
        FeedbackRequest body = new FeedbackRequest();
        body.setUid("dnr4021");
        body.setArticleId("SCOPUS:85142207731");
        body.setAction(action);
        body.setActorPersonIdentifier(actor);
        return body;
    }

    @Test
    public void rejectedSuppressesRowAndLogs() {
        when(identityService.findByUid("dnr4021")).thenReturn(new Identity());
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(existing);

        FeedbackRequest body = request("REJECTED", "dnr4021");
        body.setNote("not mine");
        ResponseEntity<Object> response = externalArticleController.recordExternalArticleFeedback(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, existing.getSuppressed());
        verify(externalArticleService).save(existing);

        ArgumentCaptor<FeedbackLog> logged = ArgumentCaptor.forClass(FeedbackLog.class);
        verify(feedbackLogService).recordAction(logged.capture());
        assertEquals("REJECTED", logged.getValue().getFeedback());
        assertEquals("dnr4021", logged.getValue().getActorPersonIdentifier());
        assertEquals("not mine", logged.getValue().getNote());
        assertEquals(0, logged.getValue().getCuratedBy());
    }

    @Test
    public void acceptedUnsuppressesRow() {
        existing.setSuppressed(Boolean.TRUE);
        when(identityService.findByUid("dnr4021")).thenReturn(new Identity());
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(existing);

        // lower-case on the wire should still work
        ResponseEntity<Object> response =
                externalArticleController.recordExternalArticleFeedback(request("accepted", "curator99"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, existing.getSuppressed());
        verify(externalArticleService).save(existing);
        verify(feedbackLogService).recordAction(any(FeedbackLog.class));
    }

    @Test
    public void acceptedLeavesSupersedeSuppressionAlone() {
        existing.setSuppressed(Boolean.TRUE);
        existing.setSupersededByPmid(12345L);
        when(identityService.findByUid("dnr4021")).thenReturn(new Identity());
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(existing);

        ResponseEntity<Object> response =
                externalArticleController.recordExternalArticleFeedback(request("ACCEPTED", "curator99"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, existing.getSuppressed());
        verify(externalArticleService, never()).save(any());
        verify(feedbackLogService).recordAction(any(FeedbackLog.class));
    }

    @Test
    public void pendingLogsWithoutTouchingRow() {
        when(identityService.findByUid("dnr4021")).thenReturn(new Identity());
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(existing);

        ResponseEntity<Object> response =
                externalArticleController.recordExternalArticleFeedback(request("PENDING", "curator99"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(externalArticleService, never()).save(any());
        verify(feedbackLogService).recordAction(any(FeedbackLog.class));
    }

    @Test
    public void feedbackOnNeverAddedCandidateStillLogs() {
        when(identityService.findByUid("dnr4021")).thenReturn(new Identity());
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(null);

        ResponseEntity<Object> response =
                externalArticleController.recordExternalArticleFeedback(request("REJECTED", "curator99"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(externalArticleService, never()).save(any());
        verify(feedbackLogService).recordAction(any(FeedbackLog.class));
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(Boolean.FALSE, body.get("rowExists"));
    }

    @Test
    public void unknownUidIsNotFoundAndNothingLogged() {
        when(identityService.findByUid("dnr4021")).thenReturn(null);

        ResponseEntity<Object> response =
                externalArticleController.recordExternalArticleFeedback(request("REJECTED", "curator99"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(feedbackLogService, never()).recordAction(any());
    }

    @Test
    public void invalidActionIsBadRequest() {
        ResponseEntity<Object> response =
                externalArticleController.recordExternalArticleFeedback(request("DELETE_EVERYTHING", "curator99"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(feedbackLogService, never()).recordAction(any());
    }

    @Test
    public void missingActorIsBadRequest() {
        ResponseEntity<Object> response =
                externalArticleController.recordExternalArticleFeedback(request("REJECTED", "  "));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(feedbackLogService, never()).recordAction(any());
        verify(externalArticleService, never()).find(any(), any());
    }

    @Test
    public void malformedArticleIdIsBadRequest() {
        FeedbackRequest body = request("REJECTED", "curator99");
        body.setArticleId("85142207731"); // no source prefix

        ResponseEntity<Object> response = externalArticleController.recordExternalArticleFeedback(body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(feedbackLogService, never()).recordAction(any());
    }
}
