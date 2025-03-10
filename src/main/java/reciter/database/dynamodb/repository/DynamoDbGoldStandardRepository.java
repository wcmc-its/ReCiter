package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.GoldStandard;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class DynamoDbGoldStandardRepository {
	
	 private final DynamoDbTable<GoldStandard> myEntityTable;

	    public DynamoDbGoldStandardRepository(DynamoDbEnhancedClient enhancedClient) {
	        this.myEntityTable = enhancedClient.table("GoldStandard", TableSchema.fromBean(GoldStandard.class));
	    }

	    public void save(GoldStandard entity) {
	        myEntityTable.putItem(entity);
	    }

	    public void saveAll(List<GoldStandard> entities) {
	        entities.forEach(entity -> myEntityTable.putItem(entity));
	    }

	    public Optional<GoldStandard> findById(String id) {
	        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
	    }

	    public Iterable<GoldStandard> findAll() {
	        return myEntityTable.scan().items();
	    }

	    public void deleteById(String id) {
	    	GoldStandard entity = new GoldStandard();
	        myEntityTable.deleteItem(entity);
	    }

	    public List<GoldStandard> findBySomeAttribute(String attributeValue) {
	        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
	                .items()
	                .stream()
	                .toList();
	    }

	    public long getItemCount() {
	        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
	    }

	    public List<GoldStandard> findAllById(List<String> uids) {
	        List<GoldStandard> entities = new ArrayList<>();
	        for (String uid : uids) {
	            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
	        }
	        return entities;
	    }
}
