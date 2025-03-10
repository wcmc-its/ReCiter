package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.PubMedArticle;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class PubMedArticleRepository{
	
	private final DynamoDbTable<PubMedArticle> myEntityTable;

    public PubMedArticleRepository(DynamoDbEnhancedClient enhancedClient) {
        this.myEntityTable = enhancedClient.table("PubMedArticle", TableSchema.fromBean(PubMedArticle.class));
    }
    
    public void save(PubMedArticle entity) {
        myEntityTable.putItem(entity);
    }
    
    public void saveAll(List<PubMedArticle> entities) {
        entities.forEach(entity -> myEntityTable.putItem(entity));
    }

    public Optional<PubMedArticle> findById(Long pmid) {
        return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(pmid))));
    }
    
    
    public Iterable<PubMedArticle> findAll() {
        return myEntityTable.scan().items();
    }


    public void deleteById(String id) {
    	PubMedArticle entity = new PubMedArticle();
        //entity.setId(id);
        myEntityTable.deleteItem(entity);
    }
    
    public void deleteAll() {
        myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
    }

    public List<PubMedArticle> findBySomeAttribute(String attributeValue) {
        return myEntityTable.query(r -> r.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(attributeValue))))
                .items()
                .stream()
                .toList();
    }
    public long getItemCount() {
        return myEntityTable.scan().items().spliterator().getExactSizeIfKnown();
    }
    
    public List<PubMedArticle> findAllById(List<Long> pmids) {
        List<PubMedArticle> entities = new ArrayList<>();
        for (Long uid : pmids) {
            entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
        }
        return entities;
    }
}
