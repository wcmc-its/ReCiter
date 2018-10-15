package reciter.service.dynamo;

import java.util.List;

import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.database.dynamodb.model.GoldStandard;

public interface IDynamoDbGoldStandardService {
    void save(GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag);
    void save(List<GoldStandard> goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag);
    GoldStandard findByUid(String uid);
    List<GoldStandard> findByUids(List<String> uid);
}
