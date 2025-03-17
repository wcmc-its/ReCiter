package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.UserFeedback;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
	
@Repository
public class UserFeedbackRepository {
	
	private final DynamoDbTable<UserFeedback> myEntityTable;

    public UserFeedbackRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("AnalysisOutput", TableSchema.fromBean(UserFeedback.class));
    }
    
    public void save(UserFeedback entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(List<UserFeedback> entities) {
        entities.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<UserFeedback> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<UserFeedback> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	UserFeedback entity = new UserFeedback();
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<UserFeedback> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long getItemCount() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<UserFeedback> findAllById(List<String> uids) {
        List<UserFeedback> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
}
