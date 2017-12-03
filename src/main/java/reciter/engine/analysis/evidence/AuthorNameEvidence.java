package reciter.engine.analysis.evidence;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class AuthorNameEvidence {
    private String institutionalAuthorName;
    private String articleAuthorName;
}
