package reciter.service.dynamo;

import java.util.List;

import reciter.database.dynamodb.model.GoldStandard;

public interface IDynamoDbGoldStandardService {
    void save(GoldStandard goldStandard);
    void save(List<GoldStandard> goldStandard);
    GoldStandard findByUid(String uid);
}
