package reciter.database.dynamodb.repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import reciter.database.dynamodb.model.PubMedArticle;
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
public class PubMedArticleRepository {

	private static final Logger log = LoggerFactory.getLogger(PubMedArticleRepository.class);

	private static final int AWS_MAX_WRITE_BATCH = 25;
	private static final int AWS_MAX_READ_BATCH = 100;
	private static final int MAX_UNPROCESSED_RETRIES = 5;
	private static final long BACKOFF_BASE_MILLIS = 50L;
	private static final long BACKOFF_MAX_MILLIS = 2_000L;

	private final DynamoDbEnhancedClient enhancedClient;
	private final DynamoDbTable<PubMedArticle> pubMedArticleTable;

	@Value("${dynamodb.batch.write.size:25}")
	private int configuredWriteBatchSize;

	@Value("${dynamodb.batch.read.size:100}")
	private int configuredReadBatchSize;

	@Value("${dynamodb.batch.max-concurrency:10}")
	private int maxConcurrentBatches;

	public PubMedArticleRepository(DynamoDbEnhancedClient enhancedClient) {
		this.enhancedClient = enhancedClient;
		this.pubMedArticleTable = enhancedClient.table("PubMedArticle", TableSchema.fromBean(PubMedArticle.class));
	}

	public void save(PubMedArticle pubMedArticle) {
		pubMedArticleTable.putItem(pubMedArticle);
	}

	public Optional<PubMedArticle> findById(Long pmid) {
		return Optional.ofNullable(pubMedArticleTable.getItem(r -> r.key(k -> k.partitionValue(pmid))));
	}

	@SuppressWarnings("unused")
	public long getItemCount() {
		return pubMedArticleTable.scan().items().stream().count();
	}

	/**
	 * Writes the given articles in batches.
	 *
	 * @throws IllegalArgumentException if the list contains null elements
	 */
	public void saveAll(List<PubMedArticle> pubMedArticles) {
		if (pubMedArticles == null || pubMedArticles.isEmpty()) {
			return;
		}
		if (pubMedArticles.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("pubMedArticles must not contain null elements");
		}

		final int effectiveBatchSize = Math.clamp(configuredWriteBatchSize, 1, AWS_MAX_WRITE_BATCH);
		final int concurrencyLimit = Math.max(1, maxConcurrentBatches);

		// Direct partition - no overhead
		List<List<PubMedArticle>> chunks = partition(pubMedArticles, effectiveBatchSize);
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

			// invokeAll guarantees complete execution before returning
			for (Future<Void> future : executor.invokeAll(tasks)) {
				future.get();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted while executing batch write", e);
		} catch (ExecutionException e) {
			// Include the real reason in the message: callers (PubMedServiceImpl.save) log only getMessage().
			throw new RuntimeException("Batch write execution failed: " + e.getCause(), e.getCause());
		}
	}

	/**
	 * Returns the stored articles for the given pmids, in input order, skipping null pmids and pmids that are
	 * not stored (same contract as the former one-getItem-per-id loop). If a pmid appears more than once
	 * in the input, the matching article is returned once per occurrence; only the DynamoDB request keys
	 * are de-duplicated, to avoid redundant reads and the duplicate-key rejection of BatchGetItem.
	 */
	public List<PubMedArticle> findAllById(List<Long> pmids) {
		if (pmids == null || pmids.isEmpty()) {
			return List.of();
		}

		final int effectiveBatchSize = Math.clamp(configuredReadBatchSize, 1, AWS_MAX_READ_BATCH);
		final int concurrencyLimit = Math.max(1, maxConcurrentBatches);

		// DynamoDB rejects duplicate keys in one BatchGetItem and null keys are invalid: query distinct, non-null ids only.
		List<Long> distinctPmids = pmids.stream().filter(Objects::nonNull).distinct().toList();
		List<List<Long>> chunks = partition(distinctPmids, effectiveBatchSize);
		Semaphore semaphore = new Semaphore(concurrencyLimit);

		List<PubMedArticle> fetchedArticles = new ArrayList<>();
		
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Callable<List<PubMedArticle>>> tasks = chunks.stream().<Callable<List<PubMedArticle>>>map(chunk -> () -> {
				semaphore.acquire();
				try {
					return readBatch(chunk);
				} finally {
					semaphore.release();
				}
			}).toList();

			// invokeAll guarantees complete execution before extracting results
			for (Future<List<PubMedArticle>> future : executor.invokeAll(tasks)) {
				fetchedArticles.addAll(future.get());
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted while executing batch read", e);
		} catch (ExecutionException e) {
			throw new RuntimeException("Batch read execution failed: " + e.getCause(), e.getCause());
		}

		// Reorder articles according to the original input sequence
		// Replace getPmid with your actual getter method if it is named differently
		Map<Long, PubMedArticle> articleMap = fetchedArticles.stream()
		        .collect(Collectors.toMap(PubMedArticle::getPmid, Function.identity(), (existing, replacement) -> existing));

		List<PubMedArticle> orderedResult = new ArrayList<>(pmids.size());
		for (Long pmid : pmids) {
			PubMedArticle match = articleMap.get(pmid);
			if (match != null) {
				orderedResult.add(match);
			}
		}
		return orderedResult;
	}

	private void writeBatchWithRetry(List<PubMedArticle> items) {
		List<PubMedArticle> pendingItems = new ArrayList<>(items);
		int attempts = 0;

		while (!pendingItems.isEmpty()) {
			WriteBatch.Builder<PubMedArticle> batchBuilder = WriteBatch.builder(PubMedArticle.class)
					.mappedTableResource(pubMedArticleTable);
			
			pendingItems.forEach(batchBuilder::addPutItem);

			BatchWriteResult result = enhancedClient.batchWriteItem(
					BatchWriteItemEnhancedRequest.builder().addWriteBatch(batchBuilder.build()).build()
			);

			List<PubMedArticle> unprocessed = result.unprocessedPutItemsForTable(pubMedArticleTable);
			if (unprocessed == null || unprocessed.isEmpty()) {
				break;
			}

			attempts++;
			if (attempts > MAX_UNPROCESSED_RETRIES) {
				throw new RuntimeException("Exceeded maximum retries for " + unprocessed.size() + " unprocessed write items");
			}

			backoffSleep(attempts);
			pendingItems = unprocessed;
		}
	}

	private List<PubMedArticle> readBatch(List<Long> keys) {
		ReadBatch.Builder<PubMedArticle> batchBuilder = ReadBatch.builder(PubMedArticle.class)
				.mappedTableResource(pubMedArticleTable);
		keys.forEach(k -> batchBuilder.addGetItem(r -> r.key(Key.builder().partitionValue(k).build())));

		// The Enhanced Client follows UnprocessedKeys across pages itself, so no manual retry is needed.
		List<PubMedArticle> collected = new ArrayList<>();
		for (BatchGetResultPage page : enhancedClient.batchGetItem(
				BatchGetItemEnhancedRequest.builder().addReadBatch(batchBuilder.build()).build())) {
			collected.addAll(page.resultsForTable(pubMedArticleTable));
		}
		return collected;
	}

	private void backoffSleep(int attempt) {
		long exponential = Math.min(BACKOFF_MAX_MILLIS, BACKOFF_BASE_MILLIS << Math.min(attempt, 10));
		long delay = exponential + ThreadLocalRandom.current().nextLong(25);
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
