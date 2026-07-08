package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ESearchResult;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class ESearchResultRepository {

	private final DynamoDbTable<ESearchResult> eSearchResultTable;

	public ESearchResultRepository(DynamoDbEnhancedClient enhancedClient) {

		this.eSearchResultTable = enhancedClient.table("ESearchResult", TableSchema.fromBean(ESearchResult.class));

	}

	public void save(ESearchResult eSearchResult) {
		eSearchResultTable.putItem(eSearchResult);
	}

	public void saveAll(List<ESearchResult> eSearchResults) {
		eSearchResults.forEach(eSearchResult -> eSearchResultTable.putItem(eSearchResult));
	}

	public Optional<ESearchResult> findById(String id) {
		return Optional.ofNullable(eSearchResultTable.getItem(r -> r.key(k -> k.partitionValue(id))));
	}

	public Iterable<ESearchResult> findAll() {
		return eSearchResultTable.scan().items();
	}

	public void deleteById(String id) {
		ESearchResult eSearchResult = new ESearchResult();
		eSearchResult.setUid(id);
		eSearchResultTable.deleteItem(eSearchResult);
	}

	public void deleteAll() {
		eSearchResultTable.scan().items().forEach(entity -> eSearchResultTable.deleteItem(entity));
	}

	@SuppressWarnings("unused")
	public long getItemCount() {
		long count = 0;

		for (ESearchResult item : eSearchResultTable.scan().items()) {
			count++;
		}
		return count;
	}

	public List<ESearchResult> findAllById(List<String> uids) {
		List<ESearchResult> eSearchResults = new ArrayList<>();
		for (String uid : uids) {
			eSearchResults.add(eSearchResultTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
		}
		return eSearchResults;
	}
}
