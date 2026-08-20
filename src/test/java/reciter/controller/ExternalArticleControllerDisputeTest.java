package reciter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reciter.controller.ExternalArticleController.DisputeRequest;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.service.AnalysisService;
import reciter.service.ExternalArticleService;
import reciter.service.IdentityService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;

/** Covers the PATCH /reciter/external-article/by/uid dispute lifecycle (DISPUTE/RETRACT/RESOLVE). */
@ExtendWith(MockitoExtension.class)
public class ExternalArticleControllerDisputeTest {

    @Mock
    private ExternalArticleService externalArticleService;
    @Mock
    private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
    @Mock
    private AnalysisService analysisService;
    @Mock
    private IdentityService identityService;

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

    @Test
    public void disputeSetsFieldsAndSuppresses() {
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(existing);
        ExternalArticle disputed = new ExternalArticle();
        disputed.setDisputedBy("dnr4021");
        disputed.setSuppressed(Boolean.TRUE);
        when(externalArticleService.dispute(eq(existing), eq("dnr4021"), eq("not mine")))
                .thenReturn(disputed);

        DisputeRequest body = new DisputeRequest();
        body.setAction("DISPUTE");
        body.setDisputeNote("not mine");

        ResponseEntity<Object> response = externalArticleController.updateExternalArticleDispute(
                "dnr4021", "SCOPUS:85142207731", "dnr4021", body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(disputed, response.getBody());
        verify(externalArticleService).dispute(existing, "dnr4021", "not mine");
        verify(externalArticleService, never()).resolveDispute(any(), any(), any());
    }

    @Test
    public void retractResolvesAsRetracted() {
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(existing);
        when(externalArticleService.resolveDispute(existing, "dnr4021", "RETRACTED")).thenReturn(existing);

        DisputeRequest body = new DisputeRequest();
        body.setAction("RETRACT");

        ResponseEntity<Object> response = externalArticleController.updateExternalArticleDispute(
                "dnr4021", "SCOPUS:85142207731", "dnr4021", body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(externalArticleService).resolveDispute(existing, "dnr4021", "RETRACTED");
    }

    @Test
    public void resolveByCuratorMarksCleared() {
        when(externalArticleService.find("dnr4021", "SCOPUS:85142207731")).thenReturn(existing);
        when(externalArticleService.resolveDispute(existing, "curator99", "CLEARED")).thenReturn(existing);

        DisputeRequest body = new DisputeRequest();
        body.setAction("resolve"); // lower-case on the wire should still work

        ResponseEntity<Object> response = externalArticleController.updateExternalArticleDispute(
                "dnr4021", "SCOPUS:85142207731", "curator99", body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(externalArticleService).resolveDispute(existing, "curator99", "CLEARED");
    }

    @Test
    public void notFoundWhenRowMissing() {
        when(externalArticleService.find("dnr4021", "SCOPUS:nope")).thenReturn(null);

        DisputeRequest body = new DisputeRequest();
        body.setAction("DISPUTE");

        ResponseEntity<Object> response = externalArticleController.updateExternalArticleDispute(
                "dnr4021", "SCOPUS:nope", "dnr4021", body);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(externalArticleService, never()).dispute(any(), any(), any());
    }

    @Test
    public void missingActingUidIsBadRequest() {
        DisputeRequest body = new DisputeRequest();
        body.setAction("DISPUTE");

        ResponseEntity<Object> response = externalArticleController.updateExternalArticleDispute(
                "dnr4021", "SCOPUS:85142207731", "  ", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(externalArticleService, never()).find(any(), any());
    }

    @Test
    public void invalidActionIsBadRequest() {
        DisputeRequest body = new DisputeRequest();
        body.setAction("DELETE_EVERYTHING");

        ResponseEntity<Object> response = externalArticleController.updateExternalArticleDispute(
                "dnr4021", "SCOPUS:85142207731", "dnr4021", body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(externalArticleService, never()).find(any(), any());
    }
}
