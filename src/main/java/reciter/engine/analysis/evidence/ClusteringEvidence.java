package reciter.engine.analysis.evidence;

import lombok.Data;

import java.util.List;

@Data
public class ClusteringEvidence {
    private List<String> meshMajors;
    private List<String> journals;
    private List<Long> cites;
    private List<Long> bibliographicCoupling;
    private List<Long> citedBy;
}
