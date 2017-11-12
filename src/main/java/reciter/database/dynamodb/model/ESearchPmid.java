package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class ESearchPmid {
    private List<Long> pmids;
    private String retrievalStrategyName;
    private Date retrievalDate;

    public List<Long> getPmids() {
        return pmids;
    }
    public void setPmids(List<Long> pmids) {
        this.pmids = pmids;
    }
    public String getRetrievalStrategyName() {
        return retrievalStrategyName;
    }
    public void setRetrievalStrategyName(String retrievalStrategyName) {
        this.retrievalStrategyName = retrievalStrategyName;
    }
    public Date getRetrievalDate() {
        return retrievalDate;
    }
    public void setRetrievalDate(Date retrievalDate) {
        this.retrievalDate = retrievalDate;
    }
}
