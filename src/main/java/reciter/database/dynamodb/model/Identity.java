package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;

@DynamoDBTable(tableName = "Identity")
@Data
@AllArgsConstructor
public class Identity {
    private final String uid;
    private final reciter.model.identity.Identity identity;

    @DynamoDBHashKey(attributeName = "uid")
    public String getUid() {
        return uid;
    }

    @DynamoDBAttribute(attributeName = "identity")
    public reciter.model.identity.Identity getIdentity() {
        return identity;
    }
}
