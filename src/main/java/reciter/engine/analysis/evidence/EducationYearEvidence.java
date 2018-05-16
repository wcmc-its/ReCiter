package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@DynamoDBDocument
public class EducationYearEvidence {
    private int discrepancyDegreeYearBachelor;
    private int discrepancyDegreeYearTerminal;
}
