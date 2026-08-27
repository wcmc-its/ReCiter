package reciter.database.dynamodb.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * One skip event for a CWID that the institutional client's CTSC path declined to
 * process — a lightweight audit trail so a skipped identity can be traced back to
 * why, when, and by which source. Partition key {@code cwid}, sort key
 * {@code timestamp} (ISO-8601 UTC), so lookups by cwid come back naturally ordered
 * by when the skip happened. Table name "CwidSkipAudit" (PascalCase, matching the
 * Identity/GoldStandard/ESearchResult table-naming convention).
 */
@DynamoDbBean
public class CwidSkipAudit {

	private String cwid;
	private String timestamp;
	private String skipReason;
	private String source;
	private String processingStatus;
	private String errorDetails;

	@DynamoDbPartitionKey
	@DynamoDbAttribute("cwid")
	public String getCwid() {
		return cwid;
	}

	public void setCwid(String cwid) {
		this.cwid = cwid;
	}

	@DynamoDbSortKey
	@DynamoDbAttribute("timestamp")
	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	@DynamoDbAttribute("skipReason")
	public String getSkipReason() {
		return skipReason;
	}

	public void setSkipReason(String skipReason) {
		this.skipReason = skipReason;
	}

	@DynamoDbAttribute("source")
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@DynamoDbAttribute("processingStatus")
	public String getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(String processingStatus) {
		this.processingStatus = processingStatus;
	}

	@DynamoDbAttribute("errorDetails")
	public String getErrorDetails() {
		return errorDetails;
	}

	public void setErrorDetails(String errorDetails) {
		this.errorDetails = errorDetails;
	}
}
