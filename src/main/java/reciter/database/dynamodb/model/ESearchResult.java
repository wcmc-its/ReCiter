package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

@DynamoDBTable(tableName = "GoldStandard")
@Data
@AllArgsConstructor
public class ESearchResult {

    private String id;
    private String uid;
    private reciter.database.mongo.model.ESearchPmid eSearchPmid;
}
