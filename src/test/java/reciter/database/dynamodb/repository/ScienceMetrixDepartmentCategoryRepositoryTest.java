package reciter.database.dynamodb.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;

/**
 * Verifies the explicit scan replacing the former Spring Data derived query
 * {@code findByScienceMetrixJournalSubfieldId} (#634 Stage 1): the numeric (N)
 * attribute is filtered for equality and the scan result returned verbatim.
 */
@RunWith(MockitoJUnitRunner.class)
public class ScienceMetrixDepartmentCategoryRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @Mock
    private PaginatedScanList<ScienceMetrixDepartmentCategory> scanResult;

    private ScienceMetrixDepartmentCategoryRepository repository;

    @Before
    public void setUp() {
        repository = new ScienceMetrixDepartmentCategoryRepository(dynamoDBMapper);
    }

    @Test
    public void findBySubfieldId_filtersNumericEqualityAndReturnsResults() {
        when(dynamoDBMapper.scan(eq(ScienceMetrixDepartmentCategory.class), any(DynamoDBScanExpression.class)))
                .thenReturn(scanResult);

        List<ScienceMetrixDepartmentCategory> result =
                repository.findByScienceMetrixJournalSubfieldId(101L);

        assertSame(scanResult, result);
        ArgumentCaptor<DynamoDBScanExpression> captor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
        verify(dynamoDBMapper).scan(eq(ScienceMetrixDepartmentCategory.class), captor.capture());
        DynamoDBScanExpression scan = captor.getValue();
        assertEquals("#attr = :val", scan.getFilterExpression());
        assertEquals("scienceMetrixJournalSubfieldId", scan.getExpressionAttributeNames().get("#attr"));
        assertEquals("101", scan.getExpressionAttributeValues().get(":val").getN());
    }
}
