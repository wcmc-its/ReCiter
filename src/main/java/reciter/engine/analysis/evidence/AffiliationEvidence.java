package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class AffiliationEvidence {
    private List<String> institutionalAffiliations;
    private List<String> emails;
    private List<String> departments;
    private String articleAffiliation;
}
