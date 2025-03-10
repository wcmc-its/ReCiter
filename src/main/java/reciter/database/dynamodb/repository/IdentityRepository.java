package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import reciter.database.dynamodb.model.Identity;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class IdentityRepository {
	private final DynamoDbTable<Identity> myEntityTable;

    public IdentityRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("Identity", TableSchema.fromBean(Identity.class));
    }
    
    public void save(Identity entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(List<Identity> entities) {
        entities.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<Identity> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<Identity> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	Identity entity = new Identity();
        //entity.setId(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<Identity> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long count() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<Identity> findAllById(List<String> uids) {
        List<Identity> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }

}