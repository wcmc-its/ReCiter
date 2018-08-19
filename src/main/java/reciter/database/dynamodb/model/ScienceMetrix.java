package reciter.database.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@DynamoDBTable(tableName = "ScienceMetrix")
@NoArgsConstructor
@AllArgsConstructor
public class ScienceMetrix {

    private Long smsid;
    private String eissn;
    private String issn;
    private String publicationName;
    private String scienceMatrixSubfieldId;
    private String scienceMetrixDomain;
    private String scienceMetrixField;
    private String scienceMetrixSubfield;

    @DynamoDBHashKey(attributeName = "smsid")
    public Long getSmsid() {
        return smsid;
    }

    public void setSmsid(Long smsid) {
        this.smsid = smsid;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "eissn-index")
    @DynamoDBAttribute(attributeName = "eissn")
    public String getEissn() {
        return eissn;
    }

    public void setEissn(String eissn) {
        this.eissn = eissn;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "issn-index")
    @DynamoDBAttribute(attributeName = "issn")
    public String getIssn() {
        return issn;
    }

    public void setIssn(String issn) {
        this.issn = issn;
    }

    @DynamoDBAttribute(attributeName = "publicationName")
    public String getPublicationName() {
        return publicationName;
    }

    public void setPublicationName(String publicationName) {
        this.publicationName = publicationName;
    }

    @DynamoDBAttribute(attributeName = "scienceMatrixSubfieldId")
    public String getScienceMatrixSubfieldId() {
        return scienceMatrixSubfieldId;
    }

    public void setScienceMatrixSubfieldId(String scienceMatrixSubfieldId) {
        this.scienceMatrixSubfieldId = scienceMatrixSubfieldId;
    }

    @DynamoDBAttribute(attributeName = "scienceMetrixDomain")
    public String getScienceMetrixDomain() {
        return scienceMetrixDomain;
    }

    public void setScienceMetrixDomain(String scienceMetrixDomain) {
        this.scienceMetrixDomain = scienceMetrixDomain;
    }

    @DynamoDBAttribute(attributeName = "scienceMetrixField")
    public String getScienceMetrixField() {
        return scienceMetrixField;
    }

    public void setScienceMetrixField(String scienceMetrixField) {
        this.scienceMetrixField = scienceMetrixField;
    }

    @DynamoDBAttribute(attributeName = "scienceMetrixSubfield")
    public String getScienceMetrixSubfield() {
        return scienceMetrixSubfield;
    }

    public void setScienceMetrixSubfield(String scienceMetrixSubfield) {
        this.scienceMetrixSubfield = scienceMetrixSubfield;
    }
}
