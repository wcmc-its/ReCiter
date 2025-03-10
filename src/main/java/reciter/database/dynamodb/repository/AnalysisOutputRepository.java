package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.AnalysisOutput;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class AnalysisOutputRepository  {

	
	private final DynamoDbTable<AnalysisOutput> myEntityTable;

    public AnalysisOutputRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("AnalysisOutput", TableSchema.fromBean(AnalysisOutput.class));
    }
    
    public void save(AnalysisOutput entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(List<AnalysisOutput> entities) {
        entities.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<AnalysisOutput> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<AnalysisOutput> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	AnalysisOutput entity = new AnalysisOutput();
        //entity.setId(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<AnalysisOutput> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long getItemCount() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<AnalysisOutput> findAllById(List<String> uids) {
        List<AnalysisOutput> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
}
