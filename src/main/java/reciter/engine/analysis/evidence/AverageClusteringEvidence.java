package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@DynamoDBDocument
public class AverageClusteringEvidence {
	private double totalArticleScoreNonStandardized;
	private double clusterScoreAverage;
	private double clusterScoreDiscrepancy;
}
