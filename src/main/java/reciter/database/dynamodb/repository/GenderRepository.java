package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.Gender;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class GenderRepository {
	private final DynamoDbTable<Gender> myEntityTable;

    public GenderRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("Gender", TableSchema.fromBean(Gender.class));
    }
    
    public void save(Gender entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(Collection<Gender> institutionAfids) {
        institutionAfids.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<Gender> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<Gender> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	Gender entity = new Gender();
        //entity.setId(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<Gender> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long count() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<Gender> findAllById(List<String> uids) {
        List<Gender> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
    
    public boolean existsById(String genderSource) {
        // Use the partition key (genderSource) to check if the item exists
    	Gender entity = myEntityTable.getItem(r -> r.key(k -> k.partitionValue(genderSource)));
        return entity != null;  // Return true if the item exists, false otherwise
    }


}
