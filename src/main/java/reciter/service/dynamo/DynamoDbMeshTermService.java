package reciter.service.dynamo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reciter.database.dynamodb.model.MeshTerm;
import reciter.database.dynamodb.repository.DynamoMeshTermRepository;
import reciter.service.IDynamoDbMeshTermService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service("dynamoDbMeshTermService")
public class DynamoDbMeshTermService implements IDynamoDbMeshTermService {

    @Autowired
    private DynamoMeshTermRepository dynamoMeshTermRepository;

    @Override
    public void save(List<MeshTerm> meshTerms) {
        //dynamoMeshTermRepository.saveAll(meshTerms);
    	dynamoMeshTermRepository.save(meshTerms);
    }

    @Override
    public List<MeshTerm> findAll() {
        Iterable<MeshTerm> meshTermIterable = dynamoMeshTermRepository.findAll();
        List<MeshTerm> meshTerms = new ArrayList<>();
        Iterator<MeshTerm> iterator = meshTermIterable.iterator();
        while (iterator.hasNext()) {
            MeshTerm meshTerm = iterator.next();
            meshTerms.add(meshTerm);
        }
        return meshTerms;
    }
}
