package reciter.database.dynamodb.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import reciter.database.dynamodb.model.PubMedArticle;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;

/**
 * Exercises the real batch code paths of {@link PubMedArticleRepository} (the other repository test
 * mocks the repository itself, so it cannot catch problems here).
 */
public class PubMedArticleRepositoryBatchTest {

    private DynamoDbEnhancedClient client;
    private DynamoDbTable<PubMedArticle> table;
    private PubMedArticleRepository repository;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        client = mock(DynamoDbEnhancedClient.class);
        table = mock(DynamoDbTable.class);
        TableSchema<PubMedArticle> schema = TableSchema.fromBean(PubMedArticle.class);
        when(table.tableSchema()).thenReturn(schema);
        when(table.tableName()).thenReturn("PubMedArticle");
        when(client.table(anyString(), ArgumentMatchers.<TableSchema<PubMedArticle>>any())).thenReturn(table);
        repository = new PubMedArticleRepository(client);
        ReflectionTestUtils.setField(repository, "configuredWriteBatchSize", 25);
        ReflectionTestUtils.setField(repository, "configuredReadBatchSize", 100);
        ReflectionTestUtils.setField(repository, "maxConcurrentBatches", 10);
    }

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
        repository.saveAll(articles(60));
        verify(client, times(3)).batchWriteItem(anyWrite());
    }

    @Test
    public void saveAllRethrowsFailuresInsteadOfSwallowingThem() {
        when(client.batchWriteItem(anyWrite())).thenThrow(new IllegalArgumentException("boom"));
        RuntimeException e = assertThrows(RuntimeException.class, () -> repository.saveAll(articles(60)));
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
        RuntimeException e = assertThrows(RuntimeException.class, () -> repository.findAllById(pmids));
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
        repository.saveAll(articles(5));
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

        List<PubMedArticle> result = repository.findAllById(List.of(1L, 2L, 3L, 99L));
        assertEquals(List.of(1L, 2L, 3L), result.stream().map(PubMedArticle::getPmid).toList());
    }

    @Test
    public void emptyInputsMakeNoDynamoDbCalls() {
        repository.saveAll(new ArrayList<>());
        assertTrue(repository.findAllById(new ArrayList<>()).isEmpty());
        verify(client, never()).batchWriteItem(anyWrite());
        verify(client, never()).batchGetItem(anyRead());
    }
}
