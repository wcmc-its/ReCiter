package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ApplicationUser;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class ApplicationUserRepository {
	private final DynamoDbTable<ApplicationUser> myEntityTable;

    public ApplicationUserRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("ApplicationUser", TableSchema.fromBean(ApplicationUser.class));
    }
    
    public void save(ApplicationUser entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(List<ApplicationUser> entities) {
        entities.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<ApplicationUser> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<ApplicationUser> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	ApplicationUser entity = new ApplicationUser();
        //entity.setId(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<ApplicationUser> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long getItemCount() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<ApplicationUser> findAllById(List<String> uids) {
        List<ApplicationUser> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
    
    public ApplicationUser findOne(String id) {
    	return myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id)));
        
    }
}
