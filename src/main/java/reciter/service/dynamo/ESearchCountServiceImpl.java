package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reciter.database.dynamodb.model.ESearchCount;
import reciter.database.dynamodb.repository.ESearchCountRepository;
import reciter.service.ESearchCountService;

@Service
public class ESearchCountServiceImpl implements ESearchCountService {

    @Autowired
    private ESearchCountRepository eSearchCountRepository;

    @Override
    public void save(ESearchCount eSearchCount) {
        eSearchCountRepository.save(eSearchCount);
    }

    @Override
    public ESearchCount findByUid(String uid) {
        return eSearchCountRepository.findById(uid).orElse(null);
    }
}
