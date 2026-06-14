package reciter.database.dynamodb.repository;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;

@Repository
public class ScienceMetrixDepartmentCategoryRepository
        extends DynamoDbCrudRepository<ScienceMetrixDepartmentCategory, Long> {

    public ScienceMetrixDepartmentCategoryRepository(DynamoDBMapper dynamoDBMapper) {
        super(dynamoDBMapper, ScienceMetrixDepartmentCategory.class);
    }

    /**
     * Replaces the former Spring Data derived query. {@code scienceMetrixJournalSubfieldId}
     * is a numeric (N) attribute, so the filter binds it as a number.
     */
    public List<ScienceMetrixDepartmentCategory> findByScienceMetrixJournalSubfieldId(Long subfieldId) {
        DynamoDBScanExpression scan = new DynamoDBScanExpression()
                .withFilterExpression("#attr = :val")
                .withExpressionAttributeNames(
                        Collections.singletonMap("#attr", "scienceMetrixJournalSubfieldId"))
                .withExpressionAttributeValues(
                        Collections.singletonMap(":val", new AttributeValue().withN(String.valueOf(subfieldId))));
        return dynamoDBMapper.scan(ScienceMetrixDepartmentCategory.class, scan);
    }
}
