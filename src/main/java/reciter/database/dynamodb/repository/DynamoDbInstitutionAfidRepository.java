package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.InstitutionAfid;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class DynamoDbInstitutionAfidRepository {
	private final DynamoDbTable<InstitutionAfid> myEntityTable;

    public DynamoDbInstitutionAfidRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("InstitutionAfid", TableSchema.fromBean(InstitutionAfid.class));
    }
    
    public void save(InstitutionAfid entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(Collection<InstitutionAfid> institutionAfids) {
        institutionAfids.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<InstitutionAfid> findById(String id) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
    }
    
    
    public Iterable<InstitutionAfid> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	InstitutionAfid entity = new InstitutionAfid();
        //entity.setId(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<InstitutionAfid> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long count() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<InstitutionAfid> findAllById(List<String> uids) {
        List<InstitutionAfid> entities = new ArrayList<>();
        for (String uid : uids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
}
