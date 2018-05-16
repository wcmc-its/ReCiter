package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@Data
@ToString
@DynamoDBDocument
public class ClusteringEvidence {
    private List<String> meshMajors;
    private String journal;
    private List<Long> cites = new ArrayList<>();
    private List<Long> bibliographicCoupling = new ArrayList<>();
    private List<Long> citedBy = new ArrayList<>();
}
