package reciter.database.dynamodb.repository;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import java.util.ArrayList;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import reciter.database.dynamodb.model.ScopusArticle;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScopusArticleRepositoryTest {

    @Mock
    private ScopusArticleRepository repository;
    
    private ScopusArticle scopusArticle1;
    private ScopusArticle scopusArticle2;
    
    @BeforeEach
    public void setUp() {
        // Create test ScopusArticle objects for reuse in tests
        scopusArticle1 = new ScopusArticle();
        scopusArticle1.setId("scopus123");
        reciter.model.scopus.ScopusArticle articleData1 = new reciter.model.scopus.ScopusArticle();
        scopusArticle1.setScopusArticle(articleData1);
        
        scopusArticle2 = new ScopusArticle();
        scopusArticle2.setId("scopus1231");
        reciter.model.scopus.ScopusArticle articleData2 = new reciter.model.scopus.ScopusArticle();
        scopusArticle2.setScopusArticle(articleData2);
    }
    
    private DynamoDbEnhancedClient client;
    private DynamoDbTable<ScopusArticle> table;
    private ScopusArticleRepository batchRepository;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUpBatchRepository() {
        client = mock(DynamoDbEnhancedClient.class);
        table = mock(DynamoDbTable.class);
        when(table.tableSchema()).thenReturn(TableSchema.fromBean(ScopusArticle.class));
        when(table.tableName()).thenReturn("ScopusArticle");
        when(client.table(anyString(), ArgumentMatchers.<TableSchema<ScopusArticle>>any())).thenReturn(table);
        batchRepository = new ScopusArticleRepository(client);
        ReflectionTestUtils.setField(batchRepository, "configuredWriteBatchSize", 25);
        ReflectionTestUtils.setField(batchRepository, "configuredReadBatchSize", 100);
        ReflectionTestUtils.setField(batchRepository, "maxConcurrentBatches", 10);
    }

    @Test
    public void testSave() {
        // Setup mock behavior
        when(repository.findById("scopus1231")).thenReturn(Optional.of(scopusArticle2));
        
        // Call the method
        repository.saveAll(Arrays.asList(scopusArticle1, scopusArticle2));
        
        // Retrieve via mock
        Optional<ScopusArticle> retrievedArticle = repository.findById("scopus1231");
        
        // Verify interactions
        verify(repository).saveAll(Arrays.asList(scopusArticle1, scopusArticle2));
        verify(repository).findById("scopus1231");
        
        // Assert results
        assertTrue(retrievedArticle.isPresent());
        assertEquals(scopusArticle2.getId(), retrievedArticle.get().getId());
    }
    
    @Test
    public void testFindById() {
        // Create a new ScopusArticle entity
        ScopusArticle scopusArticle = new ScopusArticle();
        scopusArticle.setId("scopus456");
        reciter.model.scopus.ScopusArticle articleData = new reciter.model.scopus.ScopusArticle();
        scopusArticle.setScopusArticle(articleData);
        
        // Setup mock behavior
        when(repository.findById("scopus456")).thenReturn(Optional.of(scopusArticle));
        
        // Call the repository method
        Optional<ScopusArticle> retrievedArticle = repository.findById("scopus456");
        
        // Verify interaction
        verify(repository).findById("scopus456");
        
        // Assert results
        assertTrue(retrievedArticle.isPresent());
        assertEquals(scopusArticle.getId(), retrievedArticle.get().getId());
    }
    
    @Test
    public void testDeleteAll() {
        // Setup mock behavior
        when(repository.findById("scopus555")).thenReturn(Optional.empty());
        
        // Call delete all
        repository.deleteAll();
        
        // Verify delete was called
        verify(repository).deleteAll();
        
        // Test that find returns empty after delete
        Optional<ScopusArticle> byId = repository.findById("scopus555");
        
        assertTrue(byId.isEmpty(), "Article should be deleted after deleteAll");
    }
    
    @Test
    public void testFindAllById() {
        // Create test articles
        ScopusArticle scopusArticle1 = new ScopusArticle();
        scopusArticle1.setId("scopus12345");
        scopusArticle1.setScopusArticle(new reciter.model.scopus.ScopusArticle());
        
        ScopusArticle scopusArticle2 = new ScopusArticle();
        scopusArticle2.setId("scopus67890");
        scopusArticle2.setScopusArticle(new reciter.model.scopus.ScopusArticle());
        
        List<String> ids = Arrays.asList("scopus12345", "scopus67890");
        List<ScopusArticle> expectedArticles = Arrays.asList(scopusArticle1, scopusArticle2);
        
        // Setup mock behavior
        when(repository.findAllById(ids)).thenReturn(expectedArticles);
        
        // Call the repository method
        List<ScopusArticle> articles = repository.findAllById(ids);
        
        // Verify interaction
        verify(repository).findAllById(ids);
        
        // Assert results
        assertEquals(2, articles.size());
        assertTrue(articles.stream().anyMatch(a -> a.getId().equals("scopus12345")));
        assertTrue(articles.stream().anyMatch(a -> a.getId().equals("scopus67890")));
    }

    // ---- real batch code paths (the tests above mock the repository itself) ----

    private static ScopusArticle article(long id) {
        ScopusArticle a = new ScopusArticle();
        a.setId(String.valueOf(id));
        return a;
    }

    private static List<ScopusArticle> articles(int n) {
        List<ScopusArticle> list = new ArrayList<>();
        for (long i = 1; i <= n; i++) {
            list.add(article(i));
        }
        return list;
    }

    private static BatchWriteItemEnhancedRequest anyWrite() {
        return ArgumentMatchers.any(BatchWriteItemEnhancedRequest.class);
    }

    private static BatchGetItemEnhancedRequest anyRead() {
        return ArgumentMatchers.any(BatchGetItemEnhancedRequest.class);
    }

    private BatchWriteResult nothingUnprocessed() {
        BatchWriteResult result = mock(BatchWriteResult.class);
        when(result.unprocessedPutItemsForTable(table)).thenReturn(List.of());
        return result;
    }

    @Test
    public void saveAllSplitsIntoBatchesOfAtMost25() {
        BatchWriteResult ok = nothingUnprocessed();
        when(client.batchWriteItem(anyWrite())).thenReturn(ok);
        batchRepository.saveAll(articles(60));
        verify(client, times(3)).batchWriteItem(anyWrite());
    }

    @Test
    public void saveAllResendsItemsDynamoDbLeftUnprocessed() {
        BatchWriteResult first = mock(BatchWriteResult.class);
        when(first.unprocessedPutItemsForTable(table)).thenReturn(articles(2));
        BatchWriteResult second = nothingUnprocessed();
        when(client.batchWriteItem(anyWrite())).thenReturn(first, second);
        batchRepository.saveAll(articles(5));
        verify(client, times(2)).batchWriteItem(anyWrite());
    }

    @Test
    public void saveAllRethrowsFailuresWithTheOriginalCause() {
        when(client.batchWriteItem(anyWrite())).thenThrow(new IllegalArgumentException("boom"));
        RuntimeException e = assertThrows(RuntimeException.class, () -> batchRepository.saveAll(articles(30)));
        assertEquals("boom", e.getCause().getMessage());
    }

    @Test
    public void findAllByIdKeepsInputOrderAndSkipsMissing() {
        List<ScopusArticle> reversed = articles(3);
        java.util.Collections.reverse(reversed);   // DynamoDB returns items in no particular order
        BatchGetResultPage page = mock(BatchGetResultPage.class);
        when(page.resultsForTable(table)).thenReturn(reversed);
        when(page.unprocessedKeysForTable(table)).thenReturn(List.of());
        BatchGetResultPageIterable pages = mock(BatchGetResultPageIterable.class);
        when(pages.iterator()).thenAnswer(inv -> List.of(page).iterator());
        when(client.batchGetItem(anyRead())).thenReturn(pages);

        List<ScopusArticle> result = batchRepository.findAllById(List.of("1", "2", "99", "3"));
        assertEquals(List.of("1", "2", "3"), result.stream().map(ScopusArticle::getId).toList());
    }

    @Test
    public void findAllByIdRethrowsFailuresWithTheOriginalCause() {
        when(client.batchGetItem(anyRead())).thenThrow(new IllegalStateException("read failed"));
        RuntimeException e = assertThrows(RuntimeException.class, () -> batchRepository.findAllById(List.of("1", "2")));
        assertEquals("read failed", e.getCause().getMessage());
    }

    @Test
    public void emptyInputsMakeNoDynamoDbCalls() {
        batchRepository.saveAll(new ArrayList<>());
        assertTrue(batchRepository.findAllById(new ArrayList<>()).isEmpty());
        verify(client, never()).batchWriteItem(anyWrite());
        verify(client, never()).batchGetItem(anyRead());
    }
}
