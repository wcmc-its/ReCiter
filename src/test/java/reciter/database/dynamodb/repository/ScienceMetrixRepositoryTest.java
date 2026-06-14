package reciter.database.dynamodb.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

import reciter.database.dynamodb.model.ScienceMetrix;

/**
 * Verifies the explicit scan replacing the former Spring Data derived queries
 * {@code findByEissn}/{@code findByIssn} (#634 Stage 1): the right (string)
 * attribute is filtered for equality and the first match (or {@code null}) returned.
 */
@RunWith(MockitoJUnitRunner.class)
public class ScienceMetrixRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @Mock
    private PaginatedScanList<ScienceMetrix> scanResult;

    private ScienceMetrixRepository repository;

    @Before
    public void setUp() {
        repository = new ScienceMetrixRepository(dynamoDBMapper);
    }

    @Test
    public void findByEissn_filtersEissnEqualityAndReturnsFirst() {
        ScienceMetrix expected = new ScienceMetrix();
        when(scanResult.isEmpty()).thenReturn(false);
        when(scanResult.get(0)).thenReturn(expected);
        when(dynamoDBMapper.scan(eq(ScienceMetrix.class), any(DynamoDBScanExpression.class)))
                .thenReturn(scanResult);

        ScienceMetrix result = repository.findByEissn("1234-5678");

        assertSame(expected, result);
        DynamoDBScanExpression scan = captureScan();
        assertEquals("#attr = :val", scan.getFilterExpression());
        assertEquals("eissn", scan.getExpressionAttributeNames().get("#attr"));
        assertEquals("1234-5678", scan.getExpressionAttributeValues().get(":val").getS());
    }

    @Test
    public void findByIssn_filtersIssnEqualityAndReturnsFirst() {
        ScienceMetrix expected = new ScienceMetrix();
        when(scanResult.isEmpty()).thenReturn(false);
        when(scanResult.get(0)).thenReturn(expected);
        when(dynamoDBMapper.scan(eq(ScienceMetrix.class), any(DynamoDBScanExpression.class)))
                .thenReturn(scanResult);

        ScienceMetrix result = repository.findByIssn("9999-0000");

        assertSame(expected, result);
        DynamoDBScanExpression scan = captureScan();
        assertEquals("issn", scan.getExpressionAttributeNames().get("#attr"));
        assertEquals("9999-0000", scan.getExpressionAttributeValues().get(":val").getS());
    }

    @Test
    public void findByEissn_returnsNullWhenNoMatch() {
        when(scanResult.isEmpty()).thenReturn(true);
        when(dynamoDBMapper.scan(eq(ScienceMetrix.class), any(DynamoDBScanExpression.class)))
                .thenReturn(scanResult);

        assertNull(repository.findByEissn("absent"));
    }

    private DynamoDBScanExpression captureScan() {
        ArgumentCaptor<DynamoDBScanExpression> captor = ArgumentCaptor.forClass(DynamoDBScanExpression.class);
        verify(dynamoDBMapper).scan(eq(ScienceMetrix.class), captor.capture());
        return captor.getValue();
    }
}
