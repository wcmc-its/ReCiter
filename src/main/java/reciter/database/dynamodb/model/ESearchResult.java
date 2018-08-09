package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "ESearchResult")
public class ESearchResult {

    private String uid;
    private Date retrievalDate;
    private List<ESearchPmid> eSearchPmids;

    @DynamoDBHashKey(attributeName = "uid")
    public String getUid() {
        return uid;
    }

    @DynamoDBAttribute(attributeName = "esearchpmids")
    public List<ESearchPmid> getESearchPmids() {
        return eSearchPmids;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setESearchPmids(List<ESearchPmid> eSearchPmids) {
        this.eSearchPmids = eSearchPmids;
    }
    
    public Date getRetrievalDate() {
        return retrievalDate;
    }
    public void setRetrievalDate(Date retrievalDate) {
        this.retrievalDate = retrievalDate;
    }
}
