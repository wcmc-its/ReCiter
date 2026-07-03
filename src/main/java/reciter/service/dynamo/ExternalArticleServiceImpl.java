package reciter.service.dynamo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.service.AnalysisService;
import reciter.service.ExternalArticleService;

@Service
public class ExternalArticleServiceImpl implements ExternalArticleService {

    private static final Logger log = LoggerFactory.getLogger(ExternalArticleServiceImpl.class);

    private final DynamoDBMapper dynamoDBMapper;
    private final AnalysisService analysisService;

    public ExternalArticleServiceImpl(AmazonDynamoDB amazonDynamoDB, AnalysisService analysisService) {
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
        this.analysisService = analysisService;
    }

    @Override
    public void save(ExternalArticle externalArticle) {
        dynamoDBMapper.save(externalArticle);
    }

    @Override
    public List<ExternalArticle> findByUid(String uid) {
        ExternalArticle hashKey = new ExternalArticle();
        hashKey.setUid(uid);
        DynamoDBQueryExpression<ExternalArticle> queryExpression =
                new DynamoDBQueryExpression<ExternalArticle>()
                        .withHashKeyValues(hashKey)
                        .withConsistentRead(true);
        return dynamoDBMapper.query(ExternalArticle.class, queryExpression);
    }

    @Override
    public ExternalArticle find(String uid, String articleId) {
        return dynamoDBMapper.load(ExternalArticle.class, uid, articleId);
    }

    @Override
    public void delete(String uid, String articleId) {
        ExternalArticle key = new ExternalArticle();
        key.setUid(uid);
        key.setArticleId(articleId);
        dynamoDBMapper.delete(key);
    }

    @Override
    public int supersedeByAcceptedPmids(String uid, List<Long> acceptedPmids) {
        try {
            if (uid == null || acceptedPmids == null || acceptedPmids.isEmpty()) {
                return 0;
            }
            List<ExternalArticle> activeRows = findByUid(uid).stream()
                    .filter(row -> !Boolean.TRUE.equals(row.getSuppressed()))
                    .collect(Collectors.toList());
            if (activeRows.isEmpty()) {
                return 0;
            }
            Map<Long, String> acceptedPmidToDoi = acceptedPmidToDoi(uid, acceptedPmids);
            int suppressedCount = 0;
            for (ExternalArticle row : activeRows) {
                Long supersedingPmid = ExternalArticleDupCheck.matchSupersedingPmid(row, acceptedPmids, acceptedPmidToDoi);
                if (supersedingPmid != null) {
                    row.setSuppressed(Boolean.TRUE);
                    row.setSupersededByPmid(supersedingPmid);
                    save(row);
                    suppressedCount++;
                    log.info("External article {} for uid {} superseded by accepted PMID {}",
                            row.getArticleId(), uid, supersedingPmid);
                }
            }
            return suppressedCount;
        } catch (Exception e) {
            // A gold standard update must never fail on this sidecar.
            log.warn("Supersede check failed for uid {}: {}", uid, e.getMessage());
            return 0;
        }
    }

    private Map<Long, String> acceptedPmidToDoi(String uid, List<Long> acceptedPmids) {
        Map<Long, String> pmidToDoi = new HashMap<>();
        try {
            AnalysisOutput analysis = analysisService.findByUid(uid);
            if (analysis != null && analysis.getReCiterFeature() != null
                    && analysis.getReCiterFeature().getReCiterArticleFeatures() != null) {
                for (ReCiterArticleFeature feature : analysis.getReCiterFeature().getReCiterArticleFeatures()) {
                    if (feature.getDoi() != null && acceptedPmids.contains(feature.getPmid())) {
                        pmidToDoi.put(feature.getPmid(), feature.getDoi());
                    }
                }
            }
        } catch (Exception e) {
            // Direct PMID matching still applies; only DOI resolution degrades.
            log.warn("Could not load Analysis for uid {} during supersede check: {}", uid, e.getMessage());
        }
        return pmidToDoi;
    }
}
