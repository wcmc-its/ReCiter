package reciter.service.dynamo;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.service.AnalysisService;
import reciter.service.ExternalArticleService;

@Service
public class ExternalArticleServiceImpl implements ExternalArticleService {

    private static final Logger log = LoggerFactory.getLogger(ExternalArticleServiceImpl.class);

    private final DynamoDBMapper dynamoDBMapper;
    private final AnalysisService analysisService;
    private final IDynamoDbGoldStandardService dynamoDbGoldStandardService;

    public ExternalArticleServiceImpl(AmazonDynamoDB amazonDynamoDB,
                                      AnalysisService analysisService,
                                      IDynamoDbGoldStandardService dynamoDbGoldStandardService) {
        this.dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB);
        this.analysisService = analysisService;
        this.dynamoDbGoldStandardService = dynamoDbGoldStandardService;
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
    public int reconcileWithGoldStandard(String uid) {
        List<ExternalArticle> rows;
        Set<Long> acceptedPmids;
        try {
            if (uid == null) {
                return 0;
            }
            rows = findByUid(uid);
            if (rows.isEmpty()) {
                return 0;
            }
            GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
            acceptedPmids = (goldStandard == null || goldStandard.getKnownPmids() == null)
                    ? Collections.emptySet()
                    : new HashSet<>(goldStandard.getKnownPmids());
        } catch (Exception e) {
            // A gold standard update must never fail on this sidecar.
            log.warn("Supersede reconciliation skipped for uid {}", uid, e);
            return 0;
        }

        int changed = 0;
        // Un-suppress rows whose superseding PMID is no longer accepted.
        for (ExternalArticle row : rows) {
            if (Boolean.TRUE.equals(row.getSuppressed()) && row.getSupersededByPmid() != null
                    && !acceptedPmids.contains(row.getSupersededByPmid())) {
                Long formerPmid = row.getSupersededByPmid();
                row.setSuppressed(Boolean.FALSE);
                row.setSupersededByPmid(null);
                if (saveQuietly(row)) {
                    changed++;
                    log.info("External article {} for uid {} un-suppressed; superseding PMID {} is no longer accepted",
                            row.getArticleId(), uid, formerPmid);
                }
            }
        }

        // Suppress active rows matching an accepted PMID or DOI.
        List<ExternalArticle> activeRows = rows.stream()
                .filter(row -> !Boolean.TRUE.equals(row.getSuppressed()))
                .collect(Collectors.toList());
        if (activeRows.isEmpty() || acceptedPmids.isEmpty()) {
            return changed;
        }
        Map<String, Long> acceptedDoiToPmid = activeRows.stream().anyMatch(row -> row.getDoi() != null)
                ? acceptedDoiToPmid(uid, acceptedPmids)
                : Collections.emptyMap();
        for (ExternalArticle row : activeRows) {
            Long supersedingPmid = ExternalArticleDupCheck.matchSupersedingPmid(row, acceptedPmids, acceptedDoiToPmid);
            if (supersedingPmid != null) {
                row.setSuppressed(Boolean.TRUE);
                row.setSupersededByPmid(supersedingPmid);
                if (saveQuietly(row)) {
                    changed++;
                    log.info("External article {} for uid {} superseded by accepted PMID {}",
                            row.getArticleId(), uid, supersedingPmid);
                }
            }
        }
        return changed;
    }

    private boolean saveQuietly(ExternalArticle row) {
        try {
            save(row);
            return true;
        } catch (Exception e) {
            log.warn("Could not persist supersede state for uid {} articleId {}", row.getUid(), row.getArticleId(), e);
            return false;
        }
    }

    /** Normalized-DOI → accepted PMID, resolved from the person's Analysis output. */
    private Map<String, Long> acceptedDoiToPmid(String uid, Set<Long> acceptedPmids) {
        Map<String, Long> doiToPmid = new HashMap<>();
        try {
            AnalysisOutput analysis = analysisService.findByUid(uid);
            if (analysis != null && analysis.getReCiterFeature() != null
                    && analysis.getReCiterFeature().getReCiterArticleFeatures() != null) {
                for (ReCiterArticleFeature feature : analysis.getReCiterFeature().getReCiterArticleFeatures()) {
                    String normalizedDoi = ExternalArticleDupCheck.normalizeDoi(feature.getDoi());
                    if (normalizedDoi != null && acceptedPmids.contains(feature.getPmid())) {
                        doiToPmid.put(normalizedDoi, feature.getPmid());
                    }
                }
            }
        } catch (Exception e) {
            // Direct PMID matching still applies; only DOI resolution degrades.
            log.warn("Could not load Analysis for uid {} during supersede reconciliation: {}", uid, e.getMessage());
        }
        return doiToPmid;
    }
}
