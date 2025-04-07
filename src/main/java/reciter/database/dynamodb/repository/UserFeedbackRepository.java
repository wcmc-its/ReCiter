package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ScopusArticle;
import reciter.database.dynamodb.model.UserFeedback;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
	
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
    	entity.setUid(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public long getItemCount() {
    	long count = 0;

		for (UserFeedback item : myEntityTable.scan().items()) {
			count++;
		}
		return count;
    }
    
    public List<UserFeedback> findAllById(List<String> uids) {
        List<UserFeedback> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
}
