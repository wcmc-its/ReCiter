package reciter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reciter.database.dynamodb.model.CwidSkipAudit;
import reciter.service.CwidSkipAuditService;

/** Covers POST /reciter/cwid-skip-audit and GET /reciter/cwid-skip-audit/{cwid}. */
@ExtendWith(MockitoExtension.class)
public class CwidSkipAuditControllerTest {

	@Mock
	private CwidSkipAuditService cwidSkipAuditService;

	@InjectMocks
	private CwidSkipAuditController cwidSkipAuditController;

	private CwidSkipAudit request(String cwid, String skipReason) {
		CwidSkipAudit body = new CwidSkipAudit();
		body.setCwid(cwid);
		body.setSkipReason(skipReason);
		body.setSource("ctsc");
		body.setProcessingStatus("SKIPPED");
		return body;
	}

	@Test
	public void savePersistsRecordAndReturnsIt() {
		CwidSkipAudit body = request("abc1001", "inactive in CTSC feed");
		body.setTimestamp("2026-08-27T12:00:00Z");

		ResponseEntity<Object> response = cwidSkipAuditController.save(body);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ArgumentCaptor<CwidSkipAudit> saved = ArgumentCaptor.forClass(CwidSkipAudit.class);
		verify(cwidSkipAuditService).save(saved.capture());
		assertEquals("abc1001", saved.getValue().getCwid());
		assertEquals("inactive in CTSC feed", saved.getValue().getSkipReason());
		assertEquals("2026-08-27T12:00:00Z", saved.getValue().getTimestamp());
		assertEquals(body, response.getBody());
	}

	@Test
	public void saveFillsInMissingTimestamp() {
		CwidSkipAudit body = request("abc1001", "inactive in CTSC feed");
		assertNull(body.getTimestamp());

		ResponseEntity<Object> response = cwidSkipAuditController.save(body);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ArgumentCaptor<CwidSkipAudit> saved = ArgumentCaptor.forClass(CwidSkipAudit.class);
		verify(cwidSkipAuditService).save(saved.capture());
		assertNotNull(saved.getValue().getTimestamp());
		assertFalse(saved.getValue().getTimestamp().isBlank());
	}

	@Test
	public void saveNullBodyIsBadRequest() {
		ResponseEntity<Object> response = cwidSkipAuditController.save(null);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(cwidSkipAuditService, never()).save(any());
	}

	@Test
	public void saveBlankCwidIsBadRequest() {
		CwidSkipAudit body = request("  ", "inactive in CTSC feed");

		ResponseEntity<Object> response = cwidSkipAuditController.save(body);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(cwidSkipAuditService, never()).save(any());
	}

	@Test
	public void saveBlankSkipReasonIsBadRequest() {
		CwidSkipAudit body = request("abc1001", " ");

		ResponseEntity<Object> response = cwidSkipAuditController.save(body);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		verify(cwidSkipAuditService, never()).save(any());
	}

	@Test
	public void findByCwidReturnsList() {
		CwidSkipAudit record = request("abc1001", "inactive in CTSC feed");
		record.setTimestamp("2026-08-27T12:00:00Z");
		when(cwidSkipAuditService.findByCwid("abc1001")).thenReturn(List.of(record));

		ResponseEntity<List<CwidSkipAudit>> response = cwidSkipAuditController.findByCwid("abc1001");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(1, response.getBody().size());
		assertEquals("abc1001", response.getBody().get(0).getCwid());
	}

	@Test
	public void findByCwidReturnsEmptyListWhenNoneRecorded() {
		when(cwidSkipAuditService.findByCwid("nobody")).thenReturn(Collections.emptyList());

		ResponseEntity<List<CwidSkipAudit>> response = cwidSkipAuditController.findByCwid("nobody");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().isEmpty());
	}
}
