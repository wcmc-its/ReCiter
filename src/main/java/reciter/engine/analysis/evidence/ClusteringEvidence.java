package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@ToString
@DynamoDBDocument
public class ClusteringEvidence {
    private List<String> meshMajors;
    private String journal;
    private List<Long> cites = new ArrayList<>();
    private List<Long> bibliographicCoupling = new ArrayList<>();
    private List<Long> citedBy = new ArrayList<>();
}
