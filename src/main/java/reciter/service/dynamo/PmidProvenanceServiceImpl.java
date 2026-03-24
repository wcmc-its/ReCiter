package reciter.service.dynamo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;

import reciter.database.dynamodb.model.PmidProvenance;
import reciter.service.PmidProvenanceService;

@Service
public class PmidProvenanceServiceImpl implements PmidProvenanceService {

    private static final Logger log = LoggerFactory.getLogger(PmidProvenanceServiceImpl.class);
    private static final String BACKFILL_STRATEGY = "BACKFILL_FROM_ESEARCHRESULT";

    private final DynamoDBMapper dynamoDBMapper;

    public PmidProvenanceServiceImpl(AmazonDynamoDB amazonDynamoDB) {
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
    }

    @Override
    public void save(PmidProvenance pmidProvenance) {
        dynamoDBMapper.save(pmidProvenance);
    }

    @Override
    public void saveIfNotExists(PmidProvenance pmidProvenance) {
        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();
        expected.put("uid", new ExpectedAttributeValue(false));
        saveExpression.setExpected(expected);
        try {
            dynamoDBMapper.save(pmidProvenance, saveExpression);
        } catch (ConditionalCheckFailedException e) {
            // Item already exists — this is expected, not an error
            log.debug("PmidProvenance already exists for uid={} pmid={}, skipping",
                    pmidProvenance.getUid(), pmidProvenance.getPmid());
        }
    }

    @Override
    public void saveAllIfNotExists(List<PmidProvenance> pmidProvenances) {
        int saved = 0;
        int skipped = 0;
        for (PmidProvenance pmidProvenance : pmidProvenances) {
            try {
                saveIfNotExists(pmidProvenance);
                saved++;
            } catch (Exception e) {
                skipped++;
                log.warn("Failed to save provenance for uid={} pmid={}: {}",
                        pmidProvenance.getUid(), pmidProvenance.getPmid(), e.getMessage());
            }
        }
        log.info("PmidProvenance batch: saved={}, skipped={}, total={}",
                saved, skipped, pmidProvenances.size());
    }

    @Override
    public List<PmidProvenance> findByUid(String uid) {
        PmidProvenance hashKey = new PmidProvenance();
        hashKey.setUid(uid);
        DynamoDBQueryExpression<PmidProvenance> queryExpression =
                new DynamoDBQueryExpression<PmidProvenance>()
                        .withHashKeyValues(hashKey)
                        .withConsistentRead(false);
        return dynamoDBMapper.query(PmidProvenance.class, queryExpression);
    }

    @Override
    public Set<Long> findPmidsByUid(String uid) {
        List<PmidProvenance> results = findByUid(uid);
        Set<Long> pmids = new HashSet<>();
        for (PmidProvenance p : results) {
            pmids.add(p.getPmid());
        }
        return pmids;
    }

    @Override
    public Set<Long> findPmidsByUidAndStrategy(String uid, String strategy) {
        PmidProvenance hashKey = new PmidProvenance();
        hashKey.setUid(uid);

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":strategy", new AttributeValue().withS(strategy));

        DynamoDBQueryExpression<PmidProvenance> queryExpression =
                new DynamoDBQueryExpression<PmidProvenance>()
                        .withHashKeyValues(hashKey)
                        .withFilterExpression("retrievalStrategy = :strategy")
                        .withExpressionAttributeValues(expressionValues)
                        .withConsistentRead(false);

        List<PmidProvenance> results = dynamoDBMapper.query(PmidProvenance.class, queryExpression);
        Set<Long> pmids = new HashSet<>();
        for (PmidProvenance p : results) {
            pmids.add(p.getPmid());
        }
        return pmids;
    }

    @Override
    public void updateStrategyIfBackfill(String uid, long pmid, String realStrategy) {
        PmidProvenance existing = dynamoDBMapper.load(PmidProvenance.class, uid, pmid);
        if (existing == null || !BACKFILL_STRATEGY.equals(existing.getRetrievalStrategy())) {
            return;
        }

        existing.setRetrievalStrategy(realStrategy);
        // Preserve original firstRetrievalDate — only the strategy tag is healed.

        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();
        expected.put("retrievalStrategy",
                new ExpectedAttributeValue(new AttributeValue().withS(BACKFILL_STRATEGY))
                        .withComparisonOperator(ComparisonOperator.EQ));
        saveExpression.setExpected(expected);

        try {
            dynamoDBMapper.save(existing, saveExpression);
            log.info("Healed backfill provenance for uid={} pmid={} to strategy={}",
                    uid, pmid, realStrategy);
        } catch (ConditionalCheckFailedException e) {
            log.debug("Provenance for uid={} pmid={} already healed, skipping", uid, pmid);
        }
    }
}
