package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.UserFeedback;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class UserFeedbackRepository {

	private final DynamoDbTable<UserFeedback> userFeedbackTable;

	public UserFeedbackRepository(DynamoDbEnhancedClient enhancedClient) {
		this.userFeedbackTable = enhancedClient.table("UserFeedback", TableSchema.fromBean(UserFeedback.class));
	}

	public void save(UserFeedback userFeedback) {
		userFeedbackTable.putItem(userFeedback);
	}

	public void saveAll(List<UserFeedback> userFeedbackList) {
		userFeedbackList.forEach(userFeedback -> userFeedbackTable.putItem(userFeedback));
	}

	public Optional<UserFeedback> findById(String id) {
		return Optional.ofNullable(userFeedbackTable.getItem(r -> r.key(k -> k.partitionValue(id))));
	}

	public Iterable<UserFeedback> findAll() {
		return userFeedbackTable.scan().items();
	}

	public void deleteById(String id) {
		UserFeedback userFeedback = new UserFeedback();
		userFeedback.setUid(id);
		userFeedbackTable.deleteItem(userFeedback);
	}

	public void deleteAll() {
		userFeedbackTable.scan().items().forEach(userFeedback -> userFeedbackTable.deleteItem(userFeedback));
	}

	@SuppressWarnings("unused")
	public long getItemCount() {
		long count = 0;

		for (UserFeedback userFeedback : userFeedbackTable.scan().items()) {
			count++;
		}
		return count;
	}

	public List<UserFeedback> findAllById(List<String> uids) {
		List<UserFeedback> userFeedbackList = new ArrayList<>();
		for (String uid : uids) {
			userFeedbackList.add(userFeedbackTable.getItem(r -> r.key(k -> k.partitionValue(uid))));
		}
		return userFeedbackList;
	}
}
