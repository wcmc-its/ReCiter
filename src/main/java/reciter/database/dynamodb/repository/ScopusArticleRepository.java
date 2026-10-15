package reciter.database.dynamodb.repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.ScopusArticle;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

@Repository
public class ScopusArticleRepository {

	private static final Logger log = LoggerFactory.getLogger(ScopusArticleRepository.class);

	private static final int AWS_MAX_WRITE_BATCH = 25;
	private static final int AWS_MAX_READ_BATCH = 100;
	private static final int MAX_UNPROCESSED_RETRIES = 5;

	private final DynamoDbEnhancedClient enhancedClient;
	private final DynamoDbTable<ScopusArticle> scopusArticleTable;

	@Value("${dynamodb.batch.write.size:25}")
	private int configuredWriteBatchSize;

	@Value("${dynamodb.batch.read.size:100}")
	private int configuredReadBatchSize;

	@Value("${dynamodb.batch.max-concurrency:10}")
	private int maxConcurrentBatches;

	public ScopusArticleRepository(DynamoDbEnhancedClient enhancedClient) {
		this.enhancedClient = enhancedClient;
		this.scopusArticleTable = enhancedClient.table("ScopusArticle", TableSchema.fromBean(ScopusArticle.class));
	}

	public void save(ScopusArticle scopusArticle) {
		scopusArticleTable.putItem(scopusArticle);
	}

	public void saveAll(List<ScopusArticle> scopusArticles) {
		if (scopusArticles == null || scopusArticles.isEmpty()) {
			return;
		}

		final int effectiveBatchSize = Math.clamp(configuredWriteBatchSize, 1, AWS_MAX_WRITE_BATCH);
		final int concurrencyLimit = Math.max(1, maxConcurrentBatches);

		List<List<ScopusArticle>> chunks = partition(scopusArticles, effectiveBatchSize);
		Semaphore semaphore = new Semaphore(concurrencyLimit);

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Callable<Void>> tasks = chunks.stream().<Callable<Void>>map(chunk -> () -> {
				semaphore.acquire();
				try {
					writeBatchWithRetry(chunk);
					return null;
				} finally {
					semaphore.release();
				}
			}).toList();

			for (Future<Void> future : executor.invokeAll(tasks)) {
				future.get();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted while executing batch write", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("Batch write execution failed: " + e.getCause(), e.getCause());
		}
	}

	public Optional<ScopusArticle> findById(String id) {
		return Optional.ofNullable(scopusArticleTable.getItem(r -> r.key(k -> k.partitionValue(id))));
	}

	public void deleteAll() {
		scopusArticleTable.scan().items().forEach(scopusArticle -> scopusArticleTable.deleteItem(scopusArticle));
	}

	/**
	 * Returns the stored articles for the given ids, in input order, skipping ids that are not stored
	 * (same contract as the former one-getItem-per-id loop).
	 */
	public List<ScopusArticle> findAllById(List<String> uids) {
		if (uids == null || uids.isEmpty()) {
			return new ArrayList<>();
		}

		final int effectiveBatchSize = Math.clamp(configuredReadBatchSize, 1, AWS_MAX_READ_BATCH);
		final int concurrencyLimit = Math.max(1, maxConcurrentBatches);

		List<List<String>> chunks = partition(uids, effectiveBatchSize);
		Semaphore semaphore = new Semaphore(concurrencyLimit);

		Map<String, ScopusArticle> fetched = new java.util.HashMap<>();
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Callable<List<ScopusArticle>>> tasks = chunks.stream().<Callable<List<ScopusArticle>>>map(chunk -> () -> {
				semaphore.acquire();
				try {
					return readBatchWithRetry(chunk);
				} finally {
					semaphore.release();
				}
			}).toList();

			for (Future<List<ScopusArticle>> future : executor.invokeAll(tasks)) {
				for (ScopusArticle article : future.get()) {
					fetched.putIfAbsent(article.getId(), article);
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted while executing batch read", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("Batch read execution failed: " + e.getCause(), e.getCause());
		}

		List<ScopusArticle> ordered = new ArrayList<>(uids.size());
		for (String uid : uids) {
			ScopusArticle match = uid == null ? null : fetched.get(uid);
			if (match != null) {
				ordered.add(match);
			}
		}
		return ordered;
	}

	private void writeBatchWithRetry(List<ScopusArticle> items) {
		List<ScopusArticle> pendingItems = new ArrayList<>(items);
		int attempts = 0;

		while (!pendingItems.isEmpty()) {
			WriteBatch.Builder<ScopusArticle> batchBuilder = WriteBatch.builder(ScopusArticle.class)
					.mappedTableResource(scopusArticleTable);
			pendingItems.forEach(batchBuilder::addPutItem);

			BatchWriteResult result = enhancedClient.batchWriteItem(
					BatchWriteItemEnhancedRequest.builder().addWriteBatch(batchBuilder.build()).build());

			List<ScopusArticle> unprocessed = result.unprocessedPutItemsForTable(scopusArticleTable);
			if (unprocessed == null || unprocessed.isEmpty()) {
				break;
			}

			attempts++;
			if (attempts > MAX_UNPROCESSED_RETRIES) {
				throw new RuntimeException(
						"Exceeded maximum retries for " + unprocessed.size() + " unprocessed write items");
			}
			backoffSleep(attempts);
			pendingItems = unprocessed;
		}
	}

	private List<ScopusArticle> readBatchWithRetry(List<String> keys) {
		List<String> pendingKeys = new ArrayList<>(keys);
		List<ScopusArticle> collected = new ArrayList<>();
		int attempts = 0;

		while (!pendingKeys.isEmpty()) {
			ReadBatch.Builder<ScopusArticle> batchBuilder = ReadBatch.builder(ScopusArticle.class)
					.mappedTableResource(scopusArticleTable);
			pendingKeys.forEach(k -> batchBuilder.addGetItem(r -> r.key(Key.builder().partitionValue(k).build())));

			Iterable<BatchGetResultPage> pages = enhancedClient.batchGetItem(
					BatchGetItemEnhancedRequest.builder().addReadBatch(batchBuilder.build()).build());

			List<Key> unprocessedKeys = new ArrayList<>();
			for (BatchGetResultPage page : pages) {
				collected.addAll(page.resultsForTable(scopusArticleTable));
				List<Key> pageUnprocessed = page.unprocessedKeysForTable(scopusArticleTable);
				if (pageUnprocessed != null) {
					unprocessedKeys.addAll(pageUnprocessed);
				}
			}

			if (unprocessedKeys.isEmpty()) {
				break;
			}

			attempts++;
			if (attempts > MAX_UNPROCESSED_RETRIES) {
				log.warn("Exceeded max retries for {} read keys. Returning partial batch results.",
						unprocessedKeys.size());
				break;
			}
			backoffSleep(attempts);
			pendingKeys = unprocessedKeys.stream().map(k -> k.partitionKeyValue().s()).toList();
		}
		return collected;
	}

	private void backoffSleep(int attempt) {
		long delay = (long) Math.pow(2, attempt) * 50L + ThreadLocalRandom.current().nextLong(25);
		try {
			Thread.sleep(Duration.ofMillis(delay));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Retry interrupted during backoff", e);
		}
	}

	private static <T> List<List<T>> partition(List<T> list, int size) {
		List<List<T>> partitions = new ArrayList<>();
		for (int i = 0; i < list.size(); i += size) {
			partitions.add(list.subList(i, Math.min(list.size(), i + size)));
		}
		return partitions;
	}
}
