package reciter.service.dynamo;

import reciter.database.dynamodb.model.GoldStandard;

public interface IDynamoDbGoldStandardService {
    void save(GoldStandard goldStandard);
    GoldStandard findByUid(String uid);
}
