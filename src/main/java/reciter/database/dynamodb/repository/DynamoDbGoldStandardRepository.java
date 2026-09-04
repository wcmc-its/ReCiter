package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.GoldStandard;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

@Repository
public class DynamoDbGoldStandardRepository {

	private final DynamoDbTable<GoldStandard> goldStandardTable;

	public DynamoDbGoldStandardRepository(DynamoDbEnhancedClient enhancedClient) {
		this.goldStandardTable = enhancedClient.table("GoldStandard", TableSchema.fromBean(GoldStandard.class));

	}

	public void save(GoldStandard goldStandard) {
		goldStandardTable.putItem(goldStandard);
	}

	/**
	 * Optimistic-concurrency persist: conditionally write the whole GoldStandard item only
	 * if the stored knownpmids / rejectedpmids still equal the pre-image the caller read and
	 * merged against. Guards the interactive single-accept path against lost updates when
	 * multiple replicas do a read-modify-write on the same item concurrently.
	 *
	 * @param expectedKnown    the knownpmids list as it was read (the merge pre-image); null
	 *                         means the attribute was absent
	 * @param expectedRejected the rejectedpmids list as it was read; null means absent
	 * @return {@code true} if the conditional write committed, {@code false} if another writer
	 *         changed the item first (caller should re-read, re-merge and retry)
	 */
	public boolean saveIfUnchanged(GoldStandard goldStandard, List<Long> expectedKnown, List<Long> expectedRejected) {
		try {
			goldStandardTable.putItem(PutItemEnhancedRequest.builder(GoldStandard.class)
					.item(goldStandard)
					.conditionExpression(Expression.builder()
							.expression("(attribute_not_exists(knownpmids) OR knownpmids = :ek) AND (attribute_not_exists(rejectedpmids) OR rejectedpmids = :er)")
							.putExpressionValue(":ek", listAv(expectedKnown))
							.putExpressionValue(":er", listAv(expectedRejected))
							.build())
					.build());
			return true;
		} catch (ConditionalCheckFailedException e) {
			return false;
		}
	}

	/**
	 * Conditional create: write the item only if no GoldStandard already exists for this uid.
	 * Guards the create path so a concurrent create from another replica is not clobbered.
	 *
	 * @return {@code true} if the create committed, {@code false} if an item already existed
	 *         (caller should re-read and merge instead)
	 */
	public boolean saveIfAbsent(GoldStandard goldStandard) {
		try {
			goldStandardTable.putItem(PutItemEnhancedRequest.builder(GoldStandard.class)
					.item(goldStandard)
					.conditionExpression(Expression.builder()
							.expression("attribute_not_exists(uid)")
							.build())
					.build());
			return true;
		} catch (ConditionalCheckFailedException e) {
			return false;
		}
	}

	/**
	 * Build a DynamoDB list-of-number AttributeValue for a pmid list, to compare against the
	 * stored knownpmids / rejectedpmids attributes. A null list is treated as an empty list;
	 * the absent-attribute case is handled separately by the {@code attribute_not_exists(...)}
	 * branch of the condition expression.
	 */
	private static AttributeValue listAv(List<Long> pmids) {
		return AttributeValue.builder()
				.l(pmids == null
						? List.of()
						: pmids.stream()
								.map(p -> AttributeValue.builder().n(String.valueOf(p)).build())
								.collect(Collectors.toList()))
				.build();
	}

	public void saveAll(List<GoldStandard> goldStandards) {
		goldStandards.forEach(goldStandard -> goldStandardTable.putItem(goldStandard));
	}

	public Optional<GoldStandard> findById(String id) {
		return Optional.ofNullable(goldStandardTable.getItem(r -> r.key(k -> k.partitionValue(id))));
	}

	public Iterable<GoldStandard> findAll() {
		return goldStandardTable.scan().items();
	}

	@SuppressWarnings("unused")
	public long getItemCount() {
		long count = 0;

		for (GoldStandard goldStandard : goldStandardTable.scan().items()) {
			count++;
		}
		return count;
	}

	public List<GoldStandard> findAllById(List<String> uids) {
		List<GoldStandard> goldStandards = new ArrayList<>();
		for (String uid : uids) {
			GoldStandard goldStandard = goldStandardTable.getItem(r -> r.key(k -> k.partitionValue(uid)));
			if (goldStandard != null) {
				goldStandards.add(goldStandard);
			}
		}
		return goldStandards;
	}

	 /**
     * Delete GoldStandard by UID.
     *
     * @param uid user identifier
     */
    public void deleteById(String uid) {

        goldStandardTable.deleteItem(
                Key.builder()
                        .partitionValue(uid)
                        .build());
    }
}
