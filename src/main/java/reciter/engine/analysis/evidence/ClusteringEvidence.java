package reciter.engine.analysis.evidence;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClusteringEvidence {
    private List<String> meshMajors = new ArrayList<>();
    private List<String> journals = new ArrayList<>();
    private List<Long> cites = new ArrayList<>();
    private List<Long> bibliographicCoupling = new ArrayList<>();
    private List<Long> citedBy = new ArrayList<>();
}
