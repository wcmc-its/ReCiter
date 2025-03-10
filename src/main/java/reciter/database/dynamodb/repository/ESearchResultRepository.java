package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ESearchResult;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class ESearchResultRepository  {
	
	private final DynamoDbTable<ESearchResult> myEntityTable;

    public ESearchResultRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("ESearchResult", TableSchema.fromBean(ESearchResult.class));
    }
    
    public void save(ESearchResult entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(List<ESearchResult> entities) {
        entities.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<ESearchResult> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<ESearchResult> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	ESearchResult entity = new ESearchResult();
        //entity.setId(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<ESearchResult> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long getItemCount() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<ESearchResult> findAllById(List<String> uids) {
        List<ESearchResult> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
}
