package reciter.engine.analysis.evidence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.ToString;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
@DynamoDBDocument
public class AverageClusteringEvidence {
	private double totalArticleScoreWithoutClustering;
	private double clusterScoreAverage;
	private double clusterReliabilityScore;
	private double clusterScoreModificationOfTotalScore;
}
