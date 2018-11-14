package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamoDBDocument
public class AcceptedRejectedEvidence {
	
	private Double feedbackScoreAccepted;
	private Double feedbackScoreRejected;
	private Double feedbackScoreNull;
}
