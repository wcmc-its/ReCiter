package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class ScienceMetrixDepartmentCategoryRepository  {
	
	
	public List<ScienceMetrixDepartmentCategory> findByScienceMetrixJournalSubfieldId(Long subfieldId) {
	    return myEntityTable.query(r -> r.queryConditional(
	            QueryConditional.keyEqualTo(k -> k.partitionValue(subfieldId)))
	        )
	        .items()
	        .stream()
	        .collect(Collectors.toList());
	}

	private final DynamoDbTable<ScienceMetrixDepartmentCategory> myEntityTable;

    public ScienceMetrixDepartmentCategoryRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("ScienceMetrixDepartmentCategory", TableSchema.fromBean(ScienceMetrixDepartmentCategory.class));
    }
    
    public void save(ScienceMetrixDepartmentCategory entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(Collection<ScienceMetrixDepartmentCategory> scienceMetrixDepartmentCategories) {
        scienceMetrixDepartmentCategories.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<ScienceMetrixDepartmentCategory> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<ScienceMetrixDepartmentCategory> findAll() {
        return myEntityTable.scan().items();
    }

    public void deleteById(String id) {
    	ScienceMetrixDepartmentCategory entity = new ScienceMetrixDepartmentCategory();
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<ScienceMetrixDepartmentCategory> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long count() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<ScienceMetrixDepartmentCategory> findAllById(List<String> uids) {
        List<ScienceMetrixDepartmentCategory> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
}
