package reciter.utils;

import reciter.engine.RelationshipEvidenceMixIn;
import reciter.engine.analysis.evidence.RelationshipEvidence;
import tools.jackson.databind.json.JsonMapper;

public class JsonUtils {

	public static JsonMapper configureObjectMapper() {
       
		// Register the mix-in to the RelationshipEvidence class
        return JsonMapper.builder()
                .addMixIn(RelationshipEvidence.class, RelationshipEvidenceMixIn.class)
                .build();
    }
}

