package reciter.database.dynamodb.model;

import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "PmidProvenance")
public class PmidProvenance {

    @DynamoDBHashKey(attributeName = "uid")
    private String uid;

    @DynamoDBRangeKey(attributeName = "pmid")
    private long pmid;

    @DynamoDBAttribute(attributeName = "firstRetrievalDate")
    private Date firstRetrievalDate;

    @DynamoDBAttribute(attributeName = "retrievalStrategy")
    private String retrievalStrategy;
}
