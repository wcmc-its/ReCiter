package reciter.controller;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reciter.database.dynamodb.model.CwidSkipAudit;
import reciter.service.CwidSkipAuditService;

/**
 * Audit trail for CWIDs that the institutional client's CTSC skip paths declined
 * to process. Write-once records: the institutional client POSTs a skip event,
 * and a CWID's history can be looked up by GET for troubleshooting.
 */
@Tag(name = "CwidSkipAuditController", description = "Audit trail for CWIDs skipped by the institutional client's CTSC paths.")
@RestController
public class CwidSkipAuditController {

	private static final Logger log = LoggerFactory.getLogger(CwidSkipAuditController.class);

	@Autowired
	private CwidSkipAuditService cwidSkipAuditService;

	@Operation(summary = "Record a CWID skip-audit event",
			description = "Records that a CWID was skipped during institutional-client processing, along with the "
					+ "reason, source system, processing status, and any error details. If timestamp is not "
					+ "provided, the server stamps it with the current time (ISO-8601 UTC).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "CwidSkipAudit record created successfully"),
			@ApiResponse(responseCode = "400", description = "The request body is missing or invalid")
	})
	@PostMapping(value = "/reciter/cwid-skip-audit", produces = "application/json")
	public ResponseEntity<Object> save(@RequestBody CwidSkipAudit cwidSkipAudit) {
		if (cwidSkipAudit == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The api requires a CwidSkipAudit model");
		} else if (cwidSkipAudit.getCwid() == null || cwidSkipAudit.getCwid().isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("The api requires a valid cwid to be passed with the CwidSkipAudit model");
		} else if (cwidSkipAudit.getSkipReason() == null || cwidSkipAudit.getSkipReason().isBlank()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("The api requires a valid skipReason to be passed with the CwidSkipAudit model");
		}
		if (cwidSkipAudit.getTimestamp() == null || cwidSkipAudit.getTimestamp().isBlank()) {
			cwidSkipAudit.setTimestamp(Instant.now().toString());
		}
		cwidSkipAuditService.save(cwidSkipAudit);
		log.info("Recorded CwidSkipAudit for cwid={} at timestamp={}", cwidSkipAudit.getCwid(),
				cwidSkipAudit.getTimestamp());
		return ResponseEntity.ok(cwidSkipAudit);
	}

	@Operation(summary = "Find CWID skip-audit records by cwid",
			description = "Returns all skip-audit records for the given cwid, ordered by timestamp. Returns an "
					+ "empty array when the cwid has no recorded skips.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "CwidSkipAudit records for the cwid (possibly empty)")
	})
	@GetMapping(value = "/reciter/cwid-skip-audit/{cwid}", produces = "application/json")
	public ResponseEntity<List<CwidSkipAudit>> findByCwid(@PathVariable("cwid") String cwid) {
		return ResponseEntity.ok(cwidSkipAuditService.findByCwid(cwid));
	}
}
