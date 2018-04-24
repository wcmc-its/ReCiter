package reciter.service.dynamo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.database.dynamodb.repository.DynamoDbGoldStandardRepository;

@Service("DynamoDbGoldStandardService")
public class DynamoDbGoldStandardService implements IDynamoDbGoldStandardService {

    @Autowired
    private DynamoDbGoldStandardRepository dynamoDbGoldStandardRepository;

    @Override
    public void save(GoldStandard goldStandard) {
        dynamoDbGoldStandardRepository.save(goldStandard);
    }

    @Override
    public GoldStandard findByUid(String uid) {
        return dynamoDbGoldStandardRepository.findOne(uid);
    }

	@Override
	public void save(List<GoldStandard> goldStandard) {
		dynamoDbGoldStandardRepository.save(goldStandard);
		
	}
}
