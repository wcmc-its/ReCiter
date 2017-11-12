package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "ESearchResult")
public class ESearchResult {

    private String uid;
    private ESearchPmid eSearchPmid;

    @DynamoDBHashKey(attributeName = "uid")
    public String getUid() {
        return uid;
    }

    @DynamoDBAttribute(attributeName = "esearchpmid")
    public ESearchPmid getESearchPmid() {
        return eSearchPmid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setESearchPmid(ESearchPmid eSearchPmid) {
        this.eSearchPmid = eSearchPmid;
    }
}
