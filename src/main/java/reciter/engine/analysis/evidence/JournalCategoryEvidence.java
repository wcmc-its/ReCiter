package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@DynamoDBDocument
public class JournalCategoryEvidence {
	
	private String journalSubfieldScienceMetrixLabel;
	private int journalSubfieldScienceMetrixID;
	private String journalSubfieldDepartment;
	private double journalSubfieldScore;
}
