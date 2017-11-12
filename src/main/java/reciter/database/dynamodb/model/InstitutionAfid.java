package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@DynamoDBTable(tableName = "InstitutionAfid")
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBDocument
public class InstitutionAfid {
    private String institution;
    private List<String> afids;

    @DynamoDBHashKey(attributeName = "institution")
    public String getInstitution() {
        return institution;
    }

    @DynamoDBAttribute(attributeName = "afids")
    public List<String> getAfids() {
        return afids;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setAfids(List<String> afids) {
        this.afids = afids;
    }
}
