package reciter.database.dynamodb.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import reciter.database.dynamodb.model.PubMedArticle;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PubMedArticleRepositoryTest {

    @Mock
    private PubMedArticleRepository repository;
    
    private PubMedArticle pubMedArticle1;
    private PubMedArticle pubMedArticle2;
    
    @BeforeEach
    public void setUp() {
        // Create common test data
        pubMedArticle1 = new PubMedArticle();
        pubMedArticle1.setPmid(12345L);
        pubMedArticle1.setPubMedArticle(new reciter.model.pubmed.PubMedArticle());
        
        pubMedArticle2 = new PubMedArticle();
        pubMedArticle2.setPmid(67890L);
        pubMedArticle2.setPubMedArticle(new reciter.model.pubmed.PubMedArticle());
    }

    private DynamoDbEnhancedClient client;
    private DynamoDbTable<PubMedArticle> table;
    private PubMedArticleRepository batchRepository;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUpBatchRepository() {
        client = mock(DynamoDbEnhancedClient.class);
        table = mock(DynamoDbTable.class);
        when(table.tableSchema()).thenReturn(TableSchema.fromBean(PubMedArticle.class));
        when(table.tableName()).thenReturn("PubMedArticle");
        when(client.table(anyString(), ArgumentMatchers.<TableSchema<PubMedArticle>>any())).thenReturn(table);
        batchRepository = new PubMedArticleRepository(client);
        ReflectionTestUtils.setField(batchRepository, "configuredWriteBatchSize", 25);
        ReflectionTestUtils.setField(batchRepository, "configuredReadBatchSize", 100);
        ReflectionTestUtils.setField(batchRepository, "maxConcurrentBatches", 10);
    }

    @Test
    public void testSave() {
        // Setup mock behavior
        when(repository.findById(12345L)).thenReturn(Optional.of(pubMedArticle1));
        
        // Call the repository method
        repository.save(pubMedArticle1);
        
        // Verify the interaction
        verify(repository).save(pubMedArticle1);
        
        // Retrieve the saved entity by pmid
        Optional<PubMedArticle> retrievedArticle = repository.findById(12345L);
        
        // Assert that the entity is not null and matches the saved values
        assertTrue(retrievedArticle.isPresent());
        assertEquals(pubMedArticle1.getPmid(), retrievedArticle.get().getPmid());
    }
    
    @Test
    public void testFindById() {
        // Setup mock behavior
        when(repository.findById(67890L)).thenReturn(Optional.of(pubMedArticle2));
        
        // Call the repository method
        Optional<PubMedArticle> retrievedArticle = repository.findById(67890L);
        
        // Verify the interaction
        verify(repository).findById(67890L);
        
        // Assert that the entity is present and its fields match
        assertTrue(retrievedArticle.isPresent());
        assertEquals(pubMedArticle2.getPmid(), retrievedArticle.get().getPmid());
    }
    
    @Test
    public void testGetItemCount() {
        // Setup mock behavior
        when(repository.getItemCount()).thenReturn(4L);
        
        // Call the repository method
        long count = repository.getItemCount();
        
        // Verify the interaction
        verify(repository).getItemCount();
        
        // Assert that the count matches the expected value
        assertEquals(4, count);
    }
    
    @Test
    public void testFindAllById() {
        // Setup mock behavior
        List<Long> pmids = Arrays.asList(12345L, 67890L);
        List<PubMedArticle> expectedArticles = Arrays.asList(pubMedArticle1, pubMedArticle2);
        when(repository.findAllById(pmids)).thenReturn(expectedArticles);
        
        // Call the repository method
        List<PubMedArticle> articles = repository.findAllById(pmids);
        
        // Verify the interaction
        verify(repository).findAllById(pmids);
        
        // Assert that the retrieved articles match the expected ones
        assertEquals(2, articles.size());
        assertTrue(articles.stream().anyMatch(a -> a.getPmid().equals(12345L)));
        assertTrue(articles.stream().anyMatch(a -> a.getPmid().equals(67890L)));
    }

    // ---- real batch code paths (the tests above mock the repository itself) ----

    private static List<PubMedArticle> articles(int n) {
        List<PubMedArticle> list = new ArrayList<>();
        for (long i = 1; i <= n; i++) {
            PubMedArticle article = new PubMedArticle();
            article.setPmid(i);
            list.add(article);
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
    public void saveAllRethrowsFailuresInsteadOfSwallowingThem() {
        when(client.batchWriteItem(anyWrite())).thenThrow(new IllegalArgumentException("boom"));
        RuntimeException e = assertThrows(RuntimeException.class, () -> batchRepository.saveAll(articles(60)));
        // the original failure is kept as the cause
        assertEquals("boom", e.getCause().getMessage());
    }

    @Test
    public void findAllByIdRethrowsFailuresInsteadOfSwallowingThem() {
        when(client.batchGetItem(anyRead())).thenThrow(new IllegalStateException("read failed"));
        List<Long> pmids = new ArrayList<>();
        for (long i = 1; i <= 250; i++) {
            pmids.add(i);
        }
        RuntimeException e = assertThrows(RuntimeException.class, () -> batchRepository.findAllById(pmids));
        // the original failure is kept as the cause
        assertEquals("read failed", e.getCause().getMessage());
        // 250 distinct pmids at 100 per request = 3 requests
        verify(client, org.mockito.Mockito.atLeastOnce()).batchGetItem(anyRead());
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
    public void findAllByIdReturnsArticlesInTheOrderOfTheInput() {
        List<PubMedArticle> reversed = articles(3);
        java.util.Collections.reverse(reversed);   // DynamoDB returns them in no particular order
        BatchGetResultPage page = mock(BatchGetResultPage.class);
        when(page.resultsForTable(table)).thenReturn(reversed);
        when(page.unprocessedKeysForTable(table)).thenReturn(List.of());
        BatchGetResultPageIterable pages = mock(BatchGetResultPageIterable.class);
        when(pages.iterator()).thenAnswer(inv -> List.of(page).iterator());
        when(client.batchGetItem(anyRead())).thenReturn(pages);

        List<PubMedArticle> result = batchRepository.findAllById(List.of(1L, 2L, 3L, 99L));
        assertEquals(List.of(1L, 2L, 3L), result.stream().map(PubMedArticle::getPmid).toList());
    }

    @Test
    public void emptyInputsMakeNoDynamoDbCalls() {
        batchRepository.saveAll(new ArrayList<>());
        assertTrue(batchRepository.findAllById(new ArrayList<>()).isEmpty());
        verify(client, never()).batchWriteItem(anyWrite());
        verify(client, never()).batchGetItem(anyRead());
    }
}
