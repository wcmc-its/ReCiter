package reciter.database.dynamodb.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.CwidSkipAudit;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@Repository
public class CwidSkipAuditRepository {

	private final DynamoDbTable<CwidSkipAudit> cwidSkipAuditTable;

	public CwidSkipAuditRepository(DynamoDbEnhancedClient enhancedClient) {
		this.cwidSkipAuditTable = enhancedClient.table("CwidSkipAudit", TableSchema.fromBean(CwidSkipAudit.class));
	}

	public void save(CwidSkipAudit cwidSkipAudit) {
		cwidSkipAuditTable.putItem(cwidSkipAudit);
	}

	public List<CwidSkipAudit> findByCwid(String cwid) {
		// Uses QueryConditional to query by Partition Key (Hash Key) safely; results
		// come back naturally sorted by the timestamp sort key.
		QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue(cwid));

		return cwidSkipAuditTable.query(r -> r.queryConditional(queryConditional)).items().stream()
				.collect(Collectors.toList());
	}
}
