package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@DynamoDBTable(tableName = "GoldStandard")
@Data
@AllArgsConstructor
public class GoldStandard {
    private final List<Long> knownPmids;
    private final List<Long> rejectedPmids;
}
