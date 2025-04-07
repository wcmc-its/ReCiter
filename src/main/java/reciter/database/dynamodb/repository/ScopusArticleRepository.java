package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ScopusArticle;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class ScopusArticleRepository {

	private final DynamoDbTable<ScopusArticle> myEntityTable;

	public ScopusArticleRepository(DynamoDbEnhancedClient enhancedClient) {
		this.myEntityTable = enhancedClient.table("ScopusArticle", TableSchema.fromBean(ScopusArticle.class));
	}

	public void save(ScopusArticle entity) {
		myEntityTable.putItem(entity);
	}

	public void saveAll(List<ScopusArticle> entities) {
		entities.forEach(entity -> myEntityTable.putItem(entity));
	}

	public Optional<ScopusArticle> findById(String id) {
		return Optional.ofNullable(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(id))));
	}

	public void deleteAll() {
		myEntityTable.scan().items().forEach(entity -> myEntityTable.deleteItem(entity));
	}

	public List<ScopusArticle> findAllById(List<String> uids) {
		List<ScopusArticle> entities = new ArrayList<>();
		for (String uid : uids) {
			entities.add(myEntityTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
		}
		return entities;
	}
}
