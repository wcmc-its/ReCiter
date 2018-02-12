package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class GrantEvidence {
    private List<Grant> grants;
}
