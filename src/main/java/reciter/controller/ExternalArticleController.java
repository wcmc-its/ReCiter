package reciter.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.service.AnalysisService;
import reciter.service.ExternalArticleService;
import reciter.service.IdentityService;
import reciter.service.dynamo.ExternalArticleDupCheck;
import reciter.service.dynamo.IDynamoDbGoldStandardService;

/**
 * CRUD for publications manually added from non-PubMed sources (Scopus, Web of
 * Science, OpenAlex). These records never enter feature generation or scoring;
 * they surface in feature-generator output only via includeExternal=true.
 */
@Tag(name = "ExternalArticleController", description = "Operations on manually added external-source articles.")
@Controller
public class ExternalArticleController {

    private static final Logger log = LoggerFactory.getLogger(ExternalArticleController.class);

    private static final Pattern ARTICLE_ID_PATTERN = Pattern.compile("^(SCOPUS|WOS|OPENALEX):\\S+$");

    @Autowired
    private ExternalArticleService externalArticleService;

    @Autowired
    private IDynamoDbGoldStandardService dynamoDbGoldStandardService;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private IdentityService identityService;

    @Operation(summary = "Add an external-source article for a person.",
            description = "Adds a publication from Scopus, Web of Science, or OpenAlex. Blocks on PMID/DOI duplicates "
                    + "against the person's PubMed record and existing external articles; warns on fuzzy title+year "
                    + "collisions unless force=true.")
    @RequestMapping(value = "/reciter/external-article/by/uid", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> addExternalArticle(@RequestParam(value = "uid") String uid,
                                                     @RequestParam(value = "force", required = false, defaultValue = "false") boolean force,
                                                     @RequestBody ExternalArticle externalArticle) {
        externalArticle.setUid(uid.trim());
        String validationError = validate(externalArticle);
        if (validationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(validationError));
        }
        if (identityService.findByUid(externalArticle.getUid()) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("The uid provided '" + externalArticle.getUid() + "' was not found in the Identity table"));
        }

        GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(externalArticle.getUid());
        ExternalArticleDupCheck.Result result = ExternalArticleDupCheck.check(
                externalArticle,
                goldStandard == null ? null : goldStandard.getKnownPmids(),
                goldStandard == null ? null : goldStandard.getRejectedPmids(),
                candidateArticles(externalArticle.getUid()),
                externalArticleService.findByUid(externalArticle.getUid()));

        if (result.getLevel() == ExternalArticleDupCheck.Level.BLOCKED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictBody("BLOCKED", result,
                    "This article already exists in the person's record."));
        }
        if (result.getLevel() == ExternalArticleDupCheck.Level.WARNING && !force) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictBody("WARNING", result,
                    "Possible duplicate. Retry with force=true to add anyway."));
        }

        if (externalArticle.getDateAdded() == null || externalArticle.getDateAdded().trim().isEmpty()) {
            externalArticle.setDateAdded(Instant.now().toString());
        }
        // Owned by the supersede rule (#660), never by the client.
        externalArticle.setSuppressed(Boolean.FALSE);
        externalArticle.setSupersededByPmid(null);
        externalArticleService.save(externalArticle);
        log.info("External article {} added for uid {} from {} (method={}, force={})",
                externalArticle.getArticleId(), externalArticle.getUid(), externalArticle.getSourceType(),
                externalArticle.getMethod(), force);
        return ResponseEntity.status(HttpStatus.CREATED).body(externalArticle);
    }

    @Operation(summary = "List external-source articles for a person.",
            description = "Returns all manually added external articles for the uid, including suppressed rows.")
    @RequestMapping(value = "/reciter/external-article/by/uid", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<ExternalArticle>> findExternalArticles(@RequestParam(value = "uid") String uid) {
        return new ResponseEntity<>(externalArticleService.findByUid(uid.trim()), HttpStatus.OK);
    }

    @Operation(summary = "Delete an external-source article for a person.",
            description = "Deleting is the revoke path — there is deliberately no sameAs assert/revoke model.")
    @RequestMapping(value = "/reciter/external-article/by/uid", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public ResponseEntity<Object> deleteExternalArticle(@RequestParam(value = "uid") String uid,
                                                        @RequestParam(value = "articleId") String articleId) {
        ExternalArticle existing = externalArticleService.find(uid.trim(), articleId.trim());
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("No external article '" + articleId + "' found for uid '" + uid + "'."));
        }
        externalArticleService.delete(uid.trim(), articleId.trim());
        log.info("External article {} deleted for uid {}", articleId, uid);
        return new ResponseEntity<>(existing, HttpStatus.OK);
    }

    private String validate(ExternalArticle externalArticle) {
        if (externalArticle.getArticleId() == null
                || !ARTICLE_ID_PATTERN.matcher(externalArticle.getArticleId().trim()).matches()) {
            return "articleId is required and must be a prefixed identifier: SCOPUS:<id>, WOS:<id>, or OPENALEX:<id>.";
        }
        externalArticle.setArticleId(externalArticle.getArticleId().trim());
        if (externalArticle.getTitle() == null || externalArticle.getTitle().trim().isEmpty()) {
            return "title is required.";
        }
        String prefix = externalArticle.getArticleId().substring(0, externalArticle.getArticleId().indexOf(':'));
        if (externalArticle.getSourceType() == null || externalArticle.getSourceType().trim().isEmpty()) {
            externalArticle.setSourceType(prefix);
        } else if (!prefix.equals(externalArticle.getSourceType().trim())) {
            return "sourceType '" + externalArticle.getSourceType() + "' does not match articleId prefix '" + prefix + "'.";
        }
        return null;
    }

    private List<ReCiterArticleFeature> candidateArticles(String uid) {
        try {
            AnalysisOutput analysis = analysisService.findByUid(uid);
            if (analysis != null && analysis.getReCiterFeature() != null
                    && analysis.getReCiterFeature().getReCiterArticleFeatures() != null) {
                return analysis.getReCiterFeature().getReCiterArticleFeatures();
            }
        } catch (Exception e) {
            // Weakened dup check (gold standard PMIDs still block) is better than failing the add.
            log.warn("Could not load Analysis for uid {} during external-article dup check: {}", uid, e.getMessage());
        }
        return new ArrayList<>();
    }

    private Map<String, Object> conflictBody(String status, ExternalArticleDupCheck.Result result, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        body.put("matches", result.getMatches());
        return body;
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "INVALID");
        body.put("message", message);
        return body;
    }
}
