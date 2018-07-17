package reciter.engine.analysis.evidence;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@DynamoDBDocument
public class ArticleCountEvidence {
	private double countArticlesRetrieved;
	private double articleCountScore;
}
