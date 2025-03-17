package reciter.database.dynamodb.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import reciter.database.dynamodb.model.MeshTerm;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class DynamoMeshTermRepository
{
	private final DynamoDbTable<MeshTerm> myEntityTable;

    public DynamoMeshTermRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("MeshTerm", TableSchema.fromBean(MeshTerm.class));
    }

    public void save(MeshTerm entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(List<MeshTerm> entities) {
        entities.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<MeshTerm> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<MeshTerm> findAll() {
        return myEntityTable.scan().items();
    }

    public void deleteById(String id) {
    	MeshTerm entity = new MeshTerm();
        myEntityTable.deleteItem(entity);
    }

    public List<MeshTerm> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long getItemCount() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
}
	