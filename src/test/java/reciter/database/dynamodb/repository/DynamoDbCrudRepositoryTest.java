package reciter.database.dynamodb.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

import reciter.database.dynamodb.model.Identity;

/**
 * Unit tests for the {@link DynamoDbCrudRepository} facade (#634 Stage 1), exercised
 * through the concrete {@link IdentityRepository} with a mocked {@link DynamoDBMapper}.
 * Mirrors the existing mocked-AWS-SDK test style (e.g. ArticleProvenanceServiceImplTest);
 * per the JDK-17 guidance, only plain {@code @Mock}s are used (no {@code @Spy} on JDK
 * collections).
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamoDbCrudRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    private IdentityRepository repository;

    @Before
    public void setUp() {
        repository = new IdentityRepository(dynamoDBMapper);
    }

    @Test
    public void save_delegatesToMapperAndReturnsEntity() {
        Identity entity = new Identity();
        Identity result = repository.save(entity);
        verify(dynamoDBMapper).save(entity);
        assertSame(entity, result);
    }

    @Test
    public void saveAll_batchSavesAndReturnsEntities() {
        Identity a = new Identity();
        Identity b = new Identity();
        when(dynamoDBMapper.batchSave(anyList())).thenReturn(Collections.emptyList());

        Iterable<Identity> result = repository.saveAll(Arrays.asList(a, b));

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(dynamoDBMapper).batchSave(captor.capture());
        assertEquals(Arrays.asList(a, b), captor.getValue());
        assertEquals(Arrays.asList(a, b), toList(result));
    }

    @Test
    public void saveAll_emptyDoesNotCallMapper() {
        Iterable<Identity> result = repository.saveAll(Collections.<Identity>emptyList());
        verify(dynamoDBMapper, never()).batchSave(anyList());
        assertFalse(result.iterator().hasNext());
    }

    @Test(expected = RuntimeException.class)
    public void saveAll_throwsWhenBatchReportsFailure() {
        FailedBatch failed = org.mockito.Mockito.mock(FailedBatch.class);
        when(failed.getException()).thenReturn(new RuntimeException("boom"));
        when(dynamoDBMapper.batchSave(anyList())).thenReturn(Collections.singletonList(failed));
        repository.saveAll(Collections.singletonList(new Identity()));
    }

    @Test
    public void findById_present() {
        Identity entity = new Identity();
        when(dynamoDBMapper.load(Identity.class, "uid1")).thenReturn(entity);
        Optional<Identity> result = repository.findById("uid1");
        assertTrue(result.isPresent());
        assertSame(entity, result.get());
    }

    @Test
    public void findById_absentReturnsEmptyOptional() {
        when(dynamoDBMapper.load(Identity.class, "missing")).thenReturn(null);
        assertFalse(repository.findById("missing").isPresent());
    }

    @Test
    public void existsById_reflectsLoad() {
        when(dynamoDBMapper.load(Identity.class, "yes")).thenReturn(new Identity());
        when(dynamoDBMapper.load(Identity.class, "no")).thenReturn(null);
        assertTrue(repository.existsById("yes"));
        assertFalse(repository.existsById("no"));
    }

    @Test
    public void findAll_scansTable() {
        @SuppressWarnings("unchecked")
        PaginatedScanList<Identity> scanList = org.mockito.Mockito.mock(PaginatedScanList.class);
        when(dynamoDBMapper.scan(eq(Identity.class), any(DynamoDBScanExpression.class))).thenReturn(scanList);
        Iterable<Identity> result = repository.findAll();
        assertSame(scanList, result);
        verify(dynamoDBMapper).scan(eq(Identity.class), any(DynamoDBScanExpression.class));
    }

    @Test
    public void findAllById_batchLoadsAndFlattens() {
        Identity a = new Identity();
        Identity b = new Identity();
        Map<String, List<Object>> loaded =
                Collections.singletonMap("Identity", Arrays.<Object>asList(a, b));
        when(dynamoDBMapper.batchLoad(anyMap())).thenReturn(loaded);

        List<Identity> result = toList(repository.findAllById(Arrays.asList("a", "b")));

        assertEquals(Arrays.asList(a, b), result);
        verify(dynamoDBMapper).batchLoad(anyMap());
    }

    @Test
    public void findAllById_emptyDoesNotCallMapper() {
        List<Identity> result = toList(repository.findAllById(Collections.<String>emptyList()));
        assertTrue(result.isEmpty());
        verify(dynamoDBMapper, never()).batchLoad(anyMap());
    }

    @Test
    public void count_delegatesToMapperCount() {
        when(dynamoDBMapper.count(eq(Identity.class), any(DynamoDBScanExpression.class))).thenReturn(42);
        assertEquals(42L, repository.count());
    }

    @Test
    public void deleteById_deletesWhenPresent() {
        Identity entity = new Identity();
        when(dynamoDBMapper.load(Identity.class, "uid1")).thenReturn(entity);
        repository.deleteById("uid1");
        verify(dynamoDBMapper).delete(entity);
    }

    @Test
    public void deleteById_noDeleteWhenAbsent() {
        when(dynamoDBMapper.load(Identity.class, "missing")).thenReturn(null);
        repository.deleteById("missing");
        verify(dynamoDBMapper, never()).delete(any());
    }

    @Test
    public void deleteAll_scansThenBatchDeletes() {
        Identity a = new Identity();
        @SuppressWarnings("unchecked")
        PaginatedScanList<Identity> scanList = org.mockito.Mockito.mock(PaginatedScanList.class);
        when(scanList.toArray()).thenReturn(new Object[] { a });
        when(dynamoDBMapper.scan(eq(Identity.class), any(DynamoDBScanExpression.class))).thenReturn(scanList);

        repository.deleteAll();

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(dynamoDBMapper).batchDelete(captor.capture());
        assertEquals(Collections.singletonList(a), captor.getValue());
    }

    @Test
    public void deleteAll_emptyDoesNotBatchDelete() {
        @SuppressWarnings("unchecked")
        PaginatedScanList<Identity> scanList = org.mockito.Mockito.mock(PaginatedScanList.class);
        when(scanList.toArray()).thenReturn(new Object[0]);
        when(dynamoDBMapper.scan(eq(Identity.class), any(DynamoDBScanExpression.class))).thenReturn(scanList);

        repository.deleteAll();

        verify(dynamoDBMapper, never()).batchDelete(anyList());
    }

    private static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
