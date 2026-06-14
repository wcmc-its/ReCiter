package reciter.database.dynamodb.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper.FailedBatch;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

/**
 * Generic CRUD base over the AWS SDK v1 {@link DynamoDBMapper}, replacing
 * {@code com.github.derjust:spring-data-dynamodb} (removed in #634 Stage 1).
 *
 * <p>Exposes exactly the subset of the former Spring Data {@code CrudRepository}
 * contract that ReCiter's repositories actually use, with the same method
 * signatures (returning {@link java.util.Optional} / {@link Iterable}) so callers
 * compile unchanged. Each concrete repository extends this base, passing its
 * domain type, e.g.:
 *
 * <pre>{@code
 * @Repository
 * public class IdentityRepository extends DynamoDbCrudRepository<Identity, String> {
 *     public IdentityRepository(DynamoDBMapper dynamoDBMapper) {
 *         super(dynamoDBMapper, Identity.class);
 *     }
 * }
 * }</pre>
 *
 * <p>{@link #findAll()}, {@link #count()} and {@link #deleteAll()} perform a table
 * scan, mirroring the {@code @EnableScan} behavior the repositories previously
 * relied on. All 15 repositories use a single-attribute hash key (their Spring Data
 * ID type was {@code String}/{@code Long}, never a composite key class), so
 * {@link DynamoDBMapper#load(Class, Object)} and {@link KeyPair#withHashKey(Object)}
 * are correct for id-based access.
 *
 * @param <T>  the domain (entity) type
 * @param <ID> the type of the entity's hash key
 */
public abstract class DynamoDbCrudRepository<T, ID> {

    protected final DynamoDBMapper dynamoDBMapper;
    protected final Class<T> domainType;

    protected DynamoDbCrudRepository(DynamoDBMapper dynamoDBMapper, Class<T> domainType) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.domainType = domainType;
    }

    public <S extends T> S save(S entity) {
        dynamoDBMapper.save(entity);
        return entity;
    }

    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        List<S> batch = new ArrayList<>();
        entities.forEach(batch::add);
        if (!batch.isEmpty()) {
            List<FailedBatch> failures = dynamoDBMapper.batchSave(batch);
            if (failures != null && !failures.isEmpty()) {
                throw failures.get(0).getException() instanceof RuntimeException
                        ? (RuntimeException) failures.get(0).getException()
                        : new RuntimeException("DynamoDB batchSave reported "
                                + failures.size() + " failed batch(es)", failures.get(0).getException());
            }
        }
        return batch;
    }

    public Optional<T> findById(ID id) {
        return Optional.ofNullable(dynamoDBMapper.load(domainType, id));
    }

    public boolean existsById(ID id) {
        return dynamoDBMapper.load(domainType, id) != null;
    }

    public Iterable<T> findAll() {
        return dynamoDBMapper.scan(domainType, new DynamoDBScanExpression());
    }

    public Iterable<T> findAllById(Iterable<ID> ids) {
        List<KeyPair> keyPairs = new ArrayList<>();
        for (ID id : ids) {
            keyPairs.add(new KeyPair().withHashKey(id));
        }
        if (keyPairs.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Class<?>, List<KeyPair>> itemsToGet = new HashMap<>();
        itemsToGet.put(domainType, keyPairs);
        Map<String, List<Object>> loaded = dynamoDBMapper.batchLoad(itemsToGet);
        List<T> result = new ArrayList<>();
        for (List<Object> tableItems : loaded.values()) {
            for (Object item : tableItems) {
                result.add(domainType.cast(item));
            }
        }
        return result;
    }

    public long count() {
        return dynamoDBMapper.count(domainType, new DynamoDBScanExpression());
    }

    public void deleteById(ID id) {
        T entity = dynamoDBMapper.load(domainType, id);
        if (entity != null) {
            dynamoDBMapper.delete(entity);
        }
    }

    public void deleteAll() {
        PaginatedScanList<T> scanned = dynamoDBMapper.scan(domainType, new DynamoDBScanExpression());
        List<T> items = new ArrayList<>(scanned);
        if (!items.isEmpty()) {
            dynamoDBMapper.batchDelete(items);
        }
    }
}
