package reciter.database.dynamodb.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import reciter.database.dynamodb.model.ScienceMetrix;

@Repository
public class ScienceMetrixRepository extends DynamoDbCrudRepository<ScienceMetrix, Long> {

    public ScienceMetrixRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, ScienceMetrix.class);
    }

    public ScienceMetrix findByEissn(String eissn) {
        return scanFirstByStringAttribute("eissn", eissn);
    }

    public ScienceMetrix findByIssn(String issn) {
        return scanFirstByStringAttribute("issn", issn);
    }

    /**
     * Replaces the former Spring Data derived query. The {@code @EnableScan}
     * repositories scanned with an equality filter on the (non-key) attribute, so
     * this performs the same scan and returns the first match (or {@code null}).
     */
    private ScienceMetrix scanFirstByStringAttribute(String attributeName, String value) {
        DynamoDBScanExpression scan = new DynamoDBScanExpression()
                .withFilterExpression("#attr = :val")
                .withExpressionAttributeNames(Collections.singletonMap("#attr", attributeName))
                .withExpressionAttributeValues(
                        Collections.singletonMap(":val", new AttributeValue().withS(value)));
        List<ScienceMetrix> results = dynamoDBMapper.scan(ScienceMetrix.class, scan);
        return results.isEmpty() ? null : results.get(0);
    }
}
