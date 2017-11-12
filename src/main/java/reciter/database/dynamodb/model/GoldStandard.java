package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@DynamoDBTable(tableName = "GoldStandard")
@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class GoldStandard {
    private String uid;
    private List<Long> knownPmids;
    private List<Long> rejectedPmids;

    @DynamoDBHashKey(attributeName = "uid")
    public String getUid() {
        return uid;
    }

    @DynamoDBAttribute(attributeName = "knownpmids")
    public List<Long> getKnownPmids() {
        return knownPmids;
    }

    @DynamoDBAttribute(attributeName = "rejectedpmids")
    public List<Long> getRejectedPmids() {
        return rejectedPmids;
    }
}
