package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "ESearchResult")
public class ESearchResult {

    private String uid;

    private ESearchPmid eSearchPmid;

    public ESearchResult() {

    }

    public ESearchResult(String uid, ESearchPmid eSearchPmid) {
        this.uid = uid;
        this.eSearchPmid = eSearchPmid;
    }

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
