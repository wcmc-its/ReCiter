/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reciter.algorithm.evidence.targetauthor.TargetAuthorSelection;
import reciter.algorithm.util.ArticleTranslator;
import reciter.api.parameters.FilterFeedbackType;
import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.api.parameters.UseGoldStandard;
import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
import reciter.database.dynamodb.model.ExternalArticle;
import reciter.database.dynamodb.model.GoldStandard;
import reciter.engine.Engine;
import reciter.engine.EngineOutput;
import reciter.engine.EngineParameters;
import reciter.engine.ReCiterEngine;
import reciter.engine.StrategyParameters;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.engine.analysis.ReCiterArticleFeature.PublicationFeedback;
import reciter.engine.analysis.ReCiterFeature;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;
import reciter.model.identity.OrganizationalUnit;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.AnalysisService;
import reciter.service.ESearchResultService;
import reciter.service.ExternalArticleService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.service.dynamo.AuditHistoryEntry;
import reciter.service.dynamo.FeedbackLogQueryService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.utils.AuthorNameSanitizationUtils;
import reciter.utils.GenderProbability;
import reciter.utils.InstitutionSanitizationUtil;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Tag(name = "ReCiterController", description = "Operations on ReCiter API.")
@RestController
public class ReCiterController {

	private static final Logger log = LoggerFactory.getLogger(ReCiterController.class);

    @Autowired
    private ESearchResultService eSearchResultService;

    @Autowired
    private PubMedService pubMedService;

    @Autowired
    private ReCiterRetrievalEngine aliasReCiterRetrievalEngine;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private ScopusService scopusService;

    @Autowired
    private StrategyParameters strategyParameters;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
    
    @Autowired
    private FeedbackLogQueryService feedbackLogQueryService;


    @Autowired
    private ExternalArticleService externalArticleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${use.scopus.articles}")
    private boolean useScopusArticles;
    
    @Value("${authorshipLikelihoodScore}")
    private double totalArticleScoreStandardizedDefault;
    
    @Value("${namesIgnoredCoauthors}")
    private String nameIgnoredCoAuthors;
    
    @Value("${reciter.feature.generator.keywordCountMax}")
    private double keywordsMax;

    // Rolling lookback for ONLY_NEWLY_ADDED_PUBLICATIONS retrieval. A transient PubMed
    // failure (429 / HTML error) that returns 0 articles still advances the retrievalDate
    // watermark, so a fixed 1-day window would slide past the missed day forever. Re-scanning
    // a wider [EDAT] buffer each run lets the next run pick up anything a failed run dropped.
    // Queries are author-name-scoped, so a wider date filter adds only a few PMIDs — no extra
    // requests, no approach to the count thresholds. Gaps longer than this window are covered
    // by the periodic ALL_PUBLICATIONS sweep + error-aware watermark tracked in #689.
    @Value("${reciter.retrieval.onlyNewlyAdded.lookbackDays:30}")
    private int onlyNewlyAddedLookbackDays;

    @Value("${reciter.feature.generator.group.uids.maxCount}")
    private int uidsMaxCount;
    

    @Operation(summary = "Update the goldstandard by passing GoldStandard model(uid, knownPmids, rejectedPmids)", description ="This api updates the goldstandard by passing GoldStandard model(uid, knownPmids, rejectedPmids).")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description  = "GoldStandard creation successful"),
            @ApiResponse(responseCode = "401", description  = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description  = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description  = "The resource you were trying to reach is not found")
    })
    @PostMapping(value = "/reciter/goldstandard", produces = "application/json")
    public ResponseEntity updateGoldStandard(@RequestBody GoldStandard goldStandard,@RequestParam(required = false) GoldStandardUpdateFlag goldStandardUpdateFlag,
    		@RequestParam(value = "source", required = false) String provenanceSource,
    		@RequestParam(value = "entryPath", required = false) String entryPathParam,@RequestParam(value = "curatedBy", required = false, defaultValue = "0") int curatedBy) {
    
        StopWatch stopWatch = new StopWatch("Update GoldStandard");
        stopWatch.start("Update GoldStandard");
    	if(goldStandard == null) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The api requires a GoldStandard model");
    	} else if(goldStandard != null && goldStandard.getUid() == null) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The api requires a valid uid to be passed with GoldStandard model");
    	}
    	reciter.feedback.EntryPath entryPath = reciter.feedback.EntryPath.fromString(entryPathParam);
    	if(goldStandardUpdateFlag == null ||
    			goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE || goldStandardUpdateFlag == GoldStandardUpdateFlag.DELETE) {
    		if(goldStandardUpdateFlag == null) {
    			dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.UPDATE, provenanceSource, entryPath, curatedBy);
    		} else {
    			dynamoDbGoldStandardService.save(goldStandard, goldStandardUpdateFlag, provenanceSource, entryPath, curatedBy);
    		}
    	} else {
    		dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.REFRESH, provenanceSource, entryPath,curatedBy);
        }
    	reconcileExternalArticles(Collections.singletonList(goldStandard));
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(goldStandard);
    }

    @Operation(summary = "Update the goldstandard by passing  a list of GoldStandard model(uid, knownPmids, rejectedPmids)", description = "This api updates the goldstandard by passing list of GoldStandard model(uid, knownPmids, rejectedPmids).")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
    		@ApiResponse(responseCode = "200", description = "GoldStandard List creation successful"),
    		@ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
    		@ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
    		@ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    
    @PutMapping(value = "/reciter/goldstandard", produces = "application/json")
    public ResponseEntity<List<GoldStandard>> updateGoldStandard(@RequestBody List<GoldStandard> goldStandard,@RequestParam(required = false) GoldStandardUpdateFlag goldStandardUpdateFlag,
    		@RequestParam(value = "source", required = false) String provenanceSource,
    		@RequestParam(value = "entryPath", required = false) String entryPathParam) {
        StopWatch stopWatch = new StopWatch("Update GoldStandard with List");
        stopWatch.start("Update GoldStandard with List");
    	reciter.feedback.EntryPath entryPath = reciter.feedback.EntryPath.fromString(entryPathParam);
    	if(goldStandardUpdateFlag == null ||
    			goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE || goldStandardUpdateFlag == GoldStandardUpdateFlag.DELETE) {
    		if(goldStandardUpdateFlag == null) {
    			dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.UPDATE, provenanceSource, entryPath);
    		} else {
    			dynamoDbGoldStandardService.save(goldStandard, goldStandardUpdateFlag, provenanceSource, entryPath);
    		}
    	} else {
    		dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.REFRESH, provenanceSource, entryPath);
        }
    	reconcileExternalArticles(goldStandard);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(goldStandard);
    }

    @Operation(summary = "Get the goldStandard by passing an uid", description = "This api gets the goldStandard by passing an uid.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The goldstandard retrieval for supplied uid is successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/goldstandard/{uid}", produces = "application/json")
    public ResponseEntity<GoldStandard> retrieveGoldStandardByUid(@PathVariable String uid) {
        StopWatch stopWatch = new StopWatch("Get the goldStandard by passing an uid");
        stopWatch.start("Get the goldStandard by passing an uid");
        GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(goldStandard);
    }

    @Operation(summary = "Retrieve Articles for all UID in Identity Table", description = "This API retrieves candidate articles for all uid in Identity Table from pubmed and its complementing articles from scopus")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GoldStandard deletion successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @DeleteMapping(value = "/reciter/goldstandard/{uid}", produces = "application/json")
    @ResponseBody
    public ResponseEntity deleteGoldStandard(@PathVariable String uid) {
        StopWatch stopWatch = new StopWatch("Delete gold standard by uid");
        stopWatch.start("Delete gold standard by uid");
        dynamoDbGoldStandardService.delete(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete an analysis output by uid", description = "This api deletes an analysis output record from the AnalysisOutput table by uid.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AnalysisOutput deletion successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @DeleteMapping(value = "/reciter/analysis/{uid}", produces = "application/json")
    @ResponseBody
    public ResponseEntity deleteAnalysis(@PathVariable String uid) {
        StopWatch stopWatch = new StopWatch("Delete analysis by uid");
        stopWatch.start("Delete analysis by uid");
        analysisService.delete(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete an ESearchResult by uid", description = "This api deletes an ESearchResult record by uid.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ESearchResult deletion successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @DeleteMapping(value = "/reciter/esearchresult/{uid}", produces = "application/json")
    @ResponseBody
    public ResponseEntity deleteESearchResult(@PathVariable String uid) {
        StopWatch stopWatch = new StopWatch("Delete ESearchResult by uid");
        stopWatch.start("Delete ESearchResult by uid");
        eSearchResultService.delete(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Bulk cleanup: delete records from all UID-keyed tables", description = "Deletes Identity, GoldStandard, AnalysisOutput, and ESearchResult records for a list of UIDs. Intended for external validation cleanup.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk cleanup successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden")
    })
    @PostMapping(value = "/reciter/cleanup/by/uids", produces = "application/json")
    @ResponseBody
    public ResponseEntity cleanupByUids(@RequestBody List<String> uids) {
        StopWatch stopWatch = new StopWatch("Bulk cleanup by uids");
        stopWatch.start("Bulk cleanup by uids");
        Map<String, Integer> results = new HashMap<>();

        int analysisCount = 0;
        int esearchCount = 0;
        int goldStandardCount = 0;
        int identityCount = 0;

        for (String uid : uids) {
            try { analysisService.delete(uid); analysisCount++; } catch (Exception e) { log.warn("Failed to delete analysis for uid={}: {}", uid, e.getMessage()); }
            try { eSearchResultService.delete(uid); esearchCount++; } catch (Exception e) { log.warn("Failed to delete esearchresult for uid={}: {}", uid, e.getMessage()); }
            try { dynamoDbGoldStandardService.delete(uid); goldStandardCount++; } catch (Exception e) { log.warn("Failed to delete goldstandard for uid={}: {}", uid, e.getMessage()); }
            try { identityService.delete(uid); identityCount++; } catch (Exception e) { log.warn("Failed to delete identity for uid={}: {}", uid, e.getMessage()); }
        }

        results.put("analysisOutput", analysisCount);
        results.put("eSearchResult", esearchCount);
        results.put("goldStandard", goldStandardCount);
        results.put("identity", identityCount);
        results.put("totalUids", uids.size());

        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve Articles for all UID in Identity Table",  description = "This API retrieves candidate articles for all uid in Identity Table from pubmed and its complementing articles from scopus")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list for given list of uid"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/retrieve/articles/", produces = "application/json")
    public ResponseEntity retrieveArticles(@RequestParam(required = false) RetrievalRefreshFlag refreshFlag) {
        StopWatch stopWatch = new StopWatch("Retrieve Articles for all UID in Identity Table");
        stopWatch.start("Retrieve Articles for all UID in Identity Table");
        LocalDate initial = LocalDate.now();

        LocalDate startDate = initial.withDayOfMonth(1);
        LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());

        List<Identity> identities = identityService.findAll();
        try {
            aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate), refreshFlag);
        } catch (IOException e) {
            log.error("Failed to retrieve articles."+e);
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Retrieve Articles for an UID.", description = "This API retrieves candidate articles for a given uid from pubmed and its complementing articles from scopus")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/retrieve/articles/by/uid", produces = "application/json")
    public ResponseEntity retrieveArticlesByUid(@RequestParam(required = false) String uid,@RequestParam(required = false) RetrievalRefreshFlag refreshFlag) {
        StopWatch stopWatch = new StopWatch("Retrieve Articles for an UID");
        stopWatch.start("Retrieve Articles for an UID");
        List<Identity> identities = new ArrayList<>();
        LocalDate initial = LocalDate.now();
        LocalDate startDate = initial.withDayOfMonth(1);
        LocalDate endDate = LocalDate.parse("3000-01-01"); 
        Identity identity = identityService.findByUid(uid);
        if(identity == null) {
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
        }
        //Clean up the ESearchResult Table if set refreshFlag is set
        try {
        	ESearchResult eSearchResult = eSearchResultService.findByUid(uid.trim());
            if ((refreshFlag == RetrievalRefreshFlag.FALSE 
            		||
            		refreshFlag == null) 
            		&& 
            		eSearchResult != null) {
                log.info("Using the cached retrieval articles for " + uid + ". Skipping the retrieval process");
                stopWatch.stop();
                log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                return ResponseEntity.status(HttpStatus.OK).body("The cached results of uid " + uid + " will be used since refreshFlag is not set to true");
            } else if(refreshFlag == RetrievalRefreshFlag.ALL_PUBLICATIONS
            		||
            		eSearchResult == null){
                if (eSearchResult != null)
                    eSearchResultService.delete(uid.trim());
                
                if (identity != null)
                    identities.add(identity);

                try {
                    aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate), RetrievalRefreshFlag.ALL_PUBLICATIONS);
                } catch (IOException e) {
                    log.error("Failed to retrieve articles."+ e);
                    stopWatch.stop();
                    log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The uid supplied failed to retrieve articles");
                }

            } else if(refreshFlag == RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS) {
            	if (identity != null)
                    identities.add(identity);
            	eSearchResult = eSearchResultService.findByUid(uid.trim()) ;
            	if (eSearchResult != null) {
            		startDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(Date.from(eSearchResult.getRetrievalDate()))).minusDays(onlyNewlyAddedLookbackDays);
            	} else {
            		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid supplied failed to retrieve articles. Try running with ALL_PUBLICATIONS refreshFlag");
            	}
            	
            	try {
                    aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate), refreshFlag);
                } catch (IOException e) {
                    log.error("Failed to retrieve articles."+e);
                    stopWatch.stop();
                    log.error(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The uid supplied failed to retrieve articles");
                }
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("No such entity exists: "+e);
        }

        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().body("Successfully retrieved all candidate articles for " + uid + " and refreshed all search results");
    }
    
    @Operation(summary = "Retrieve pending articles for a group of users.",  description = "Retrieve pending articles for a group of users.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @PostMapping(value = "/reciter/feature-generator/by/group", produces = "application/json")
    public ResponseEntity retrieveBulkFeatureGenerator(@RequestBody(required = false) List<String> uids, @RequestParam(required =false) List<String> personType, @RequestParam(required = false) List<String> organizationalAffiliation, @RequestParam(required = false) List<String> departmentalAffiliation,
    		@RequestParam(required = true) Double totalStandardizedArticleScore, @RequestParam(required = true) int maxArticlesPerPerson) {
        
        if(uids == null && personType == null && organizationalAffiliation == null && departmentalAffiliation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please supply either the list of uids or any one of personType, organizationalAffiliation, departmentalAffiliation");
        }
        StopWatch stopWatch = new StopWatch("Retrieve pending articles for a group of users");
        stopWatch.start("Retrieve pending articles for a group of users");
        List<Identity> identities = null;
        if(uids != null && uids.size() > uidsMaxCount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The maximum number of uids allowed is " + uidsMaxCount);
        }
        if(uids == null || uids.isEmpty()) {
            
            try {
                identities = identityService.findAll();
            } catch (Exception ne) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Issue with the request" + ne);
            }
            if(identities == null || (identities != null && identities.isEmpty())){
                stopWatch.stop();
                log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The Identity table is empty.");
            }
        }
        final double totalScore;
        
        if(totalStandardizedArticleScore == null) {
        	totalScore = totalArticleScoreStandardizedDefault;
        } else {
        	totalScore = totalStandardizedArticleScore;
        }
        
        if((identities != null && !identities.isEmpty()) || (uids != null && !uids.isEmpty())) {
            List<String> identitySubset = null;
            if(identities != null && !identities.isEmpty()) {
                identitySubset = identities.parallelStream().filter(identity -> 
                        ((personType == null)?
                        true:
                        (identity.getPersonTypes() != null
                        &&
                        !identity.getPersonTypes().isEmpty() && !Collections.disjoint(identity.getPersonTypes(), personType)))
                        &&
                        ((organizationalAffiliation == null)?
                        true:
                        (identity.getInstitutions() != null
                        &&
                        !identity.getInstitutions().isEmpty()
                        &&
                        !Collections.disjoint(identity.getInstitutions(), organizationalAffiliation)))
                        &&
                        ((departmentalAffiliation == null)?
                        true:		
                        (identity.getOrganizationalUnits() != null
                        &&
                        !identity.getOrganizationalUnits().isEmpty()
                        &&
                        !Collections.disjoint(identity.getOrganizationalUnits().stream()
                                .map(OrganizationalUnit::getOrganizationalUnitLabel)
                                .collect(Collectors.toList()), departmentalAffiliation))))
                        .map(Identity::getUid)
                        .collect(Collectors.toList());
            }
            List<AnalysisOutput> analysis = null;
            try {
                if(uids != null && !uids.isEmpty()) {
                    analysis = analysisService.findByUids(uids);
                }  else if(identitySubset != null && !identitySubset.isEmpty()){
                    analysis = analysisService.findByUids(identitySubset);
                }
            } catch (Exception e) {
                log.warn("Failed to deserialize Analysis cache for group query, ignoring stale entries: {}",
                    e.getMessage());
                analysis = null;
            }

        	if (analysis != null && !analysis.isEmpty()) {
        		analysis.stream().forEach(anl -> {
        			if(anl.getReCiterFeature() != null
        			&& 
        			anl.getReCiterFeature().getReCiterArticleFeatures() != null
        			&& 
        			!anl.getReCiterFeature().getReCiterArticleFeatures().isEmpty()) {
        				anl.getReCiterFeature().setReCiterArticleFeatures(anl.getReCiterFeature().getReCiterArticleFeatures()
        						.stream()
        						.filter(article ->
			    					article.getUserAssertion() == PublicationFeedback.NULL
			    					&&
			    					//article.getTotalArticleScoreStandardized() >= totalScore)
			    					article.getAuthorshipLikelihoodScore() >= totalScore)
        						.limit(maxArticlesPerPerson) 
        						.collect(Collectors.toList()));
        			}
        		});
            	List<ReCiterFeature> analysisSubset= analysis.parallelStream()
            			.filter(anl -> anl.getReCiterFeature().getReCiterArticleFeatures() != null && !anl.getReCiterFeature().getReCiterArticleFeatures().isEmpty())
            			.map(AnalysisOutput::getReCiterFeature)
                        .collect(Collectors.toList());
            	
                stopWatch.stop();
                log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                //return new ResponseEntity<>((analysisSubset.size()> maxTotalArticles?analysisSubset.stream().limit(maxTotalArticles).collect(Collectors.toList()):analysisSubset), HttpStatus.OK);
                return new ResponseEntity<>(analysisSubset, HttpStatus.OK);
        	}
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no publications data for the group. Please wait while feature-generator re-runs tonight.");
    }

    @Operation(summary = "Feature generation for UID.",  description  = "This api generates all the suggestion for a given uid along with its relevant evidence.")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string")),
    	@Parameter(name = "fields", description = "Fields to return (e.g., reCiterArticleFeatures.pmid,reCiterArticleFeatures.publicationType.publicationTypeCanonical). Default is all.", in =ParameterIn.QUERY, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "The uid provided was not found in the Identity table")
    })
    @GetMapping(value = "/reciter/feature-generator/by/uid", produces = "application/json")
    public ResponseEntity runFeatureGenerator(@RequestParam String uid,
    		@RequestParam(required = false)	Double authorshipLikelihoodScore ,
    		@RequestParam(required = false) UseGoldStandard useGoldStandard, 
    		@RequestParam(required = false) FilterFeedbackType filterByFeedback, 
    		@RequestParam(required = false) boolean analysisRefreshFlag,
    		@RequestParam(required = false) RetrievalRefreshFlag retrievalRefreshFlag,
    		@RequestParam(value = "includeExternal", required = false, defaultValue = "false") boolean includeExternal) {

    	StopWatch stopWatch = new StopWatch("Feature generation for UID "+uid);
        stopWatch.start("Feature generation for UID");
        
        final double totalScore;
        if(authorshipLikelihoodScore == null) {
        	totalScore = totalArticleScoreStandardizedDefault; // Configuring the totalScore in multiple of 10's in application.properties file
        } else {
        	totalScore = authorshipLikelihoodScore; // Configuring the totalScore in multiple of 10's in application.properties file
        }
        log.info("totalScore from the request" + totalScore);
        EngineOutput engineOutput;
        EngineParameters parameters;
        List<ReCiterArticleFeature> originalFeatures = new ArrayList<ReCiterArticleFeature>();
        
        Identity identity = identityService.findByUid(uid);
        if(identity == null) {
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
        }
        AnalysisOutput analysis = null;
        try {
            analysis = analysisService.findByUid(uid.trim());
        } catch (Exception e) {
            log.warn("Failed to deserialize Analysis cache for {}, deleting stale entry: {}",
                uid, e.getMessage());
            analysisService.delete(uid.trim());
        }

        if (!analysisRefreshFlag
        		&&
        		analysis != null
        		&&
        		(useGoldStandard == UseGoldStandard.AS_EVIDENCE || useGoldStandard == null)) {//This was added to ensure to use analysis results only in evidence mode
        	List<Long> finalArticles =null;
			if(analysis.getReCiterFeature()!=null && analysis.getReCiterFeature().getReCiterArticleFeatures()!=null)
			{
				finalArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
				List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
        	}
			else
			{
				finalArticles = new ArrayList<Long>(Arrays.asList(0L));;
			}	
			GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
        	List<Long> knownPmids = null;
            if (goldStandard == null) {
            	knownPmids = new ArrayList<>();
            } else {
                knownPmids = goldStandard.getKnownPmids();
            }
            //Count pending pubs
			if(analysis.getReCiterFeature()!=null && analysis.getReCiterFeature().getReCiterArticleFeatures()!=null)
			{
				analysis.getReCiterFeature().setCountPendingArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
								.filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
								&&
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
								.count());
				
				
			}
			else if(analysis.getReCiterFeature()!=null)
			{
				analysis.getReCiterFeature().setCountPendingArticles(0);
			}
			//All the results are filtered based on filterByFeedback
        	if(analysis.getReCiterFeature()!=null && analysis.getReCiterFeature().getReCiterArticleFeatures()!=null)
			{
				// FIX (#640-A): Compute precision/recall/accuracy ONCE over the FULL candidate
				// set BEFORE any display filtering. Previously each filterByFeedback branch ran
				// performAnalysis over the already-filtered subset, so e.g. ACCEPTED_ONLY removed
				// every REJECTED (false-positive) row and precision collapsed to TP/(TP+0)=1.0.
				List<ReCiterArticle> fullCandidateSet = analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
					    .map(featureArticle -> {
					        ReCiterArticle article = new ReCiterArticle(featureArticle.getPmid());
					        article.setAuthorshipLikelihoodScore(featureArticle.getAuthorshipLikelihoodScore());
					        article.setGoldStandard(featureArticle.getUserAssertion() == PublicationFeedback.ACCEPTED ? 1 : featureArticle.getUserAssertion() == PublicationFeedback.REJECTED ? -1 : 0);
					        return article;
					    })
					    .collect(Collectors.toList());
				Analysis featureAnalysis = Analysis.performAnalysis(fullCandidateSet,knownPmids);
				analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
				analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
				analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());

				// Apply the display filter ONLY to the returned article list (metrics already set above).
				if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
								.filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
								&&
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
								||
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
								||
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
								)
								.collect(Collectors.toList()));
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
				} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_ONLY) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED)
							.collect(Collectors.toList()));
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
				} else if(filterByFeedback == FilterFeedbackType.REJECTED_ONLY) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
							.collect(Collectors.toList()));
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
				} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_NULL) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							//.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
							.filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
							&& 
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
							||
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
							)
							.collect(Collectors.toList()));
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
				} else if(filterByFeedback == FilterFeedbackType.REJECTED_AND_NULL) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							//.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
							.filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
							&&
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
							||
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
							)
							.collect(Collectors.toList()));
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
				} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_REJECTED) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
							||
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
							.collect(Collectors.toList()));
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
				} else if(filterByFeedback == FilterFeedbackType.NULL) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							//.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
							&&
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
							.collect(Collectors.toList()));
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
				}
			}
			else if(analysis.getReCiterFeature()!=null)
			{
				analysis.getReCiterFeature().setReCiterArticleFeatures(new ArrayList<reciter.engine.analysis.ReCiterArticleFeature>());
				analysis.getReCiterFeature().setCountSuggestedArticles(0);
				analysis.getReCiterFeature().setPrecision(0.0);
				analysis.getReCiterFeature().setRecall(0.0);
				analysis.getReCiterFeature().setOverallAccuracy(null);
			}	
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return featureGeneratorResponse(analysis.getReCiterFeature(), uid, includeExternal);
        } else {
            if (useGoldStandard == null) {
                strategyParameters.setUseGoldStandardEvidence(true);
            } else if (useGoldStandard == UseGoldStandard.FOR_TESTING_ONLY) {
                strategyParameters.setUseGoldStandardEvidence(false);
            } else if (useGoldStandard == UseGoldStandard.AS_EVIDENCE) {
                strategyParameters.setUseGoldStandardEvidence(true);
            }
            parameters = initializeEngineParameters(uid, authorshipLikelihoodScore, retrievalRefreshFlag);
            if (parameters == null) {
                stopWatch.stop();
                log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(String.format("The uid provided '%s' does not have any candidate records in " +
                                "ESearchResult table. Try running the candidate article retrieval api first with " +
                                "refreshFlag = true.", uid));
            }
            TargetAuthorSelection t = new TargetAuthorSelection();
            t.identifyTargetAuthor(parameters.getReciterArticles(), parameters.getIdentity());
            double filterScore = 0;
            if(parameters.getTotalStandardzizedArticleScore() >= strategyParameters.getMinimumStorageThreshold()) {
            	filterScore = strategyParameters.getMinimumStorageThreshold();
            } else {
            	filterScore = parameters.getTotalStandardzizedArticleScore();
            }
            Engine engine = new ReCiterEngine();
            engineOutput = engine.run(parameters, strategyParameters, filterScore, keywordsMax);
            originalFeatures.addAll(engineOutput.getReCiterFeature().getReCiterArticleFeatures());
           //Store Analysis only in evidence mode
            if(useGoldStandard == UseGoldStandard.AS_EVIDENCE || useGoldStandard == null) {
	            AnalysisOutput analysisOutput = new AnalysisOutput();
	            if(engineOutput != null) {
	            	if(filterScore == strategyParameters.getMinimumStorageThreshold()) {
						analysisOutput.setReCiterFeature(engineOutput.getReCiterFeature());
	            	} else {
	            		//Enforce Strict Minimum Storage Threshold
	            		ReCiterFeature reCiterFeature = new ReCiterFeature();
	            		reCiterFeature = engineOutput.getReCiterFeature();
	            				
	            		List<ReCiterArticleFeature> reCiterFilteredArticles = reCiterFeature.getReCiterArticleFeatures()
		            	.stream()
		            	//.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= strategyParameters.getMinimumStorageThreshold()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getAuthorshipLikelihoodScore() >= strategyParameters.getMinimumStorageThreshold()
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
		            	.collect(Collectors.toList());
	            		reCiterFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
	            		reCiterFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
	            		analysisOutput.setReCiterFeature(reCiterFeature);
	            	}
	            }
				analysisOutput.setUid(uid);
				if(analysisOutput.getReCiterFeature() != null) {
					analysisService.save(analysisOutput);
				} 
            }
        }
        
        ReCiterFeature reCiterOutputFeature = new ReCiterFeature();
        reCiterOutputFeature = engineOutput.getReCiterFeature();
        
        //Count pending pubs
        reCiterOutputFeature.setCountPendingArticles(originalFeatures.stream()
          //  .filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
              .filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
            &&
            reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
            .count());
        //All the results are filtered based on filterByFeedback
        if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
        List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
        		.stream()
            	//.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
            	.filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
            			&&
            			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
            			||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
		            	)
            	.collect(Collectors.toList());
        reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
        reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.ACCEPTED_ONLY){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.REJECTED_ONLY){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_NULL){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
            		.filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
                			&&
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
                			||
    		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
                			)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.REJECTED_AND_NULL){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
            		.filter(reCiterArticleFeature -> (reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
                			&&
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
                			||
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
                			)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_REJECTED){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
                			||
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else {
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
            		.filter(reCiterArticleFeature -> reCiterArticleFeature.getAuthorshipLikelihoodScore() >= totalScore
                			&&
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return featureGeneratorResponse(reCiterOutputFeature, uid, includeExternal);
    }

    /**
     * With includeExternal=true, appends the person's manually added external-source
     * articles (Scopus/WoS/OpenAlex) as a sibling "externalArticles" field — they are
     * read at serialization time and never enter feature generation or Analysis.
     * Suppressed rows (superseded by a PubMed record with the same DOI) are excluded.
     */
    /**
     * Post-update supersede reconciliation (#660) — runs for every flag including
     * DELETE, since removing an acceptance is what un-suppresses an external row.
     */
    private void reconcileExternalArticles(List<GoldStandard> goldStandards) {
        for (GoldStandard goldStandard : goldStandards) {
            if (goldStandard != null && goldStandard.getUid() != null) {
                externalArticleService.reconcileWithGoldStandard(goldStandard.getUid());
            }
        }
    }

    private ResponseEntity<Object> featureGeneratorResponse(ReCiterFeature reCiterFeature, String uid, boolean includeExternal) {
        if (!includeExternal || reCiterFeature == null) {
            return new ResponseEntity<>(reCiterFeature, HttpStatus.OK);
        }
        try {
            List<ExternalArticle> externalArticles = externalArticleService.findByUid(uid.trim()).stream()
                    .filter(externalArticle -> !Boolean.TRUE.equals(externalArticle.getSuppressed()))
                    .collect(Collectors.toList());
            ObjectNode responseBody = objectMapper.valueToTree(reCiterFeature);
            responseBody.set("externalArticles", objectMapper.valueToTree(externalArticles));
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } catch (Exception e) {
            // The scoring result must never fail on the optional external-article sidecar;
            // the absent externalArticles field (vs empty array) signals the degradation.
            log.warn("Could not append external articles for uid {}; returning scoring result without them: {}",
                    uid, e.getMessage());
            return new ResponseEntity<>(reCiterFeature, HttpStatus.OK);
        }
    }
    
    @Operation(summary = "Article retrieval by UID.", description =  "This api returns all the publication for a supplied uid.")
    @Parameters({
    	@Parameter(name = "api-key", description  = "api-key for this resource", in =ParameterIn.HEADER, schema =@Schema(type ="string")),
    	@Parameter(name = "fields", description = "Fields to return (e.g., reCiterArticleFeatures.pmid,reCiterArticleFeatures.publicationType.publicationTypeCanonical). Default is all.", in =ParameterIn.QUERY, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found"),
            @ApiResponse(responseCode = "500", description = "The uid provided was not found in the Identity table")
    })
    
    @GetMapping(value = "/reciter/article-retrieval/by/uid", produces = "application/json")
    public ResponseEntity runArticleRetrievalByUid(@RequestParam String uid, @RequestParam(required = false) Double totalStandardizedArticleScore,@RequestParam(required = false) FilterFeedbackType filterByFeedback) {
    	StopWatch stopWatch = new StopWatch("Feature generation for UID");
        stopWatch.start("Feature generation for UID");
        
        final double totalScore;
        
        if(totalStandardizedArticleScore == null) {
        	totalScore = totalArticleScoreStandardizedDefault;
        } else {
        	totalScore = totalStandardizedArticleScore;
        }
        try {
        	Identity identity = identityService.findByUid(uid);
            if(identity == null) {
                stopWatch.stop();
                log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            	return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
            }
        } catch (NullPointerException n) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
        }
        AnalysisOutput analysis = null;
        try {
            analysis = analysisService.findByUid(uid.trim());
        } catch (Exception e) {
            log.warn("Failed to deserialize Analysis cache for {}, deleting stale entry: {}",
                uid, e.getMessage());
            analysisService.delete(uid.trim());
        }

        if (analysis != null) {//This was added to ensure to use analysis results only in evidence mode
        	if(analysis.getReCiterFeature() != null) {
        		Optional.ofNullable(analysis)
        	    .map(AnalysisOutput::getReCiterFeature)
        	    .ifPresent(feature -> {

        	        feature.setInGoldStandardButNotRetrieved(null);
        	        feature.setPrecision(null);
        	        feature.setRecall(null);
        	        feature.setOverallAccuracy(null);

        	        Optional.ofNullable(feature.getReCiterArticleFeatures())
        	            .orElse(Collections.emptyList())
        	            .stream()
        	            .filter(Objects::nonNull)
        	            .forEach(f -> f.setEvidence(null));
        	    });
        	}
        	//All the results are filtered based on filterByFeedback
        	if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
	        	Optional.ofNullable(analysis)
	            .map(AnalysisOutput::getReCiterFeature)
	            .ifPresent(feature ->
	                feature.setReCiterArticleFeatures(
	                    Optional.ofNullable(feature.getReCiterArticleFeatures())
	                        .orElse(Collections.emptyList())
	                        .stream()
	                        .filter(Objects::nonNull)
	                        .filter(f -> {
	                            PublicationFeedback fb = f.getUserAssertion();

	                            return fb == PublicationFeedback.ACCEPTED
	                                    || fb == PublicationFeedback.REJECTED
	                                    || (fb == PublicationFeedback.NULL
	                                        && Optional.ofNullable(f.getAuthorshipLikelihoodScore())
	                                                   .map(score -> score >= totalScore)
	                                                   .orElse(false));
	                        })
	                        .collect(Collectors.toList())
	                )
	            );

        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_ONLY) {
        			Optional.ofNullable(analysis)
        	    .map(AnalysisOutput::getReCiterFeature)
        	    .ifPresent(feature ->
        	        feature.setReCiterArticleFeatures(
        	            Optional.ofNullable(feature.getReCiterArticleFeatures())
        	                .orElse(Collections.emptyList())
        	                .stream()
        	                .filter(Objects::nonNull)
        	                .filter(f -> f.getUserAssertion() == PublicationFeedback.ACCEPTED)
        	                .collect(Collectors.toList())
        	        )
        	    );
        	} else if(filterByFeedback == FilterFeedbackType.REJECTED_ONLY) {
        		Optional.ofNullable(analysis)
        	    .map(AnalysisOutput::getReCiterFeature)
        	    .ifPresent(feature ->
        	        feature.setReCiterArticleFeatures(
        	            Optional.ofNullable(feature.getReCiterArticleFeatures())
        	                .orElse(Collections.emptyList())
        	                .stream()
        	                .filter(Objects::nonNull)
        	                .filter(f -> f.getUserAssertion() == PublicationFeedback.REJECTED)
        	                .collect(Collectors.toList())
        	        )
        	    );
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_NULL) {
        		Optional.ofNullable(analysis)
        	    .map(AnalysisOutput::getReCiterFeature)
        	    .ifPresent(feature ->
        	        feature.setReCiterArticleFeatures(
        	            Optional.ofNullable(feature.getReCiterArticleFeatures())
        	                .orElse(Collections.emptyList())
        	                .stream()
        	                .filter(Objects::nonNull)
        	                .filter(f ->
        	                    (f.getUserAssertion() == PublicationFeedback.ACCEPTED)
        	                    ||
        	                    (f.getUserAssertion() == PublicationFeedback.NULL
        	                        && Optional.ofNullable(f.getAuthorshipLikelihoodScore())
        	                                   .map(score -> score >= totalScore)
        	                                   .orElse(false))
        	                )
        	                .collect(Collectors.toList())
        	        )
        	    );
        	} else if(filterByFeedback == FilterFeedbackType.REJECTED_AND_NULL) {
        		Optional.ofNullable(analysis)
        	    .map(AnalysisOutput::getReCiterFeature)
        	    .ifPresent(feature ->
        	        feature.setReCiterArticleFeatures(
        	            Optional.ofNullable(feature.getReCiterArticleFeatures())
        	                .orElse(Collections.emptyList())
        	                .stream()
        	                .filter(Objects::nonNull)
        	                .filter(f ->
        	                    (f.getUserAssertion() == PublicationFeedback.REJECTED)
        	                    ||
        	                    (f.getUserAssertion() == PublicationFeedback.NULL
        	                        && Optional.ofNullable(f.getAuthorshipLikelihoodScore())
        	                                   .map(score -> score >= totalScore)
        	                                   .orElse(false))
        	                )
        	                .collect(Collectors.toList())
        	        )
        	    );
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_REJECTED) {
        		       		
        		Optional.ofNullable(analysis)
        	    .map(AnalysisOutput::getReCiterFeature)
        	    .ifPresent(feature ->
        	        feature.setReCiterArticleFeatures(
        	            Optional.ofNullable(feature.getReCiterArticleFeatures())
        	                .orElse(Collections.emptyList())
        	                .stream()
        	                .filter(Objects::nonNull)
        	                .filter(f ->
        	                    f.getUserAssertion() == PublicationFeedback.ACCEPTED
        	                    || f.getUserAssertion() == PublicationFeedback.REJECTED
        	                )
        	                .collect(Collectors.toList())
        	        )
        	    );
        	} else if(filterByFeedback == FilterFeedbackType.NULL) {
        		       		
        		Optional.ofNullable(analysis)
        	    .map(AnalysisOutput::getReCiterFeature)
        	    .ifPresent(feature ->
        	        feature.setReCiterArticleFeatures(
        	            Optional.ofNullable(feature.getReCiterArticleFeatures())
        	                .orElse(Collections.emptyList())
        	                .stream()
        	                .filter(Objects::nonNull)
        	                .filter(f ->
        	                    f.getUserAssertion() == PublicationFeedback.NULL
        	                    && Optional.ofNullable(f.getAuthorshipLikelihoodScore())
        	                               .map(score -> score >= totalScore)
        	                               .orElse(false)
        	                )
        	                .collect(Collectors.toList())
        	        )
        	    );
        	}
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return new ResponseEntity<>(analysis.getReCiterFeature(), HttpStatus.OK);
        } 
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no publications data for uid " + uid + ". Please wait while feature-generator re-runs tonight.");
       
    }
    
    @Operation(summary = "Get curation audit history (FeedbackLog + ArticleProvenance) for a uid",
            description = "Returns the curator action history (accept/reject/pending) for the person, newest first, each row enriched with how the PMID first arrived (ArticleProvenance).")
    @Parameters({
    	@Parameter(name = "api-key", description = "api-key for this resource",in =ParameterIn.HEADER, schema =@Schema(type ="string"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit history retrieval successful"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "403", description = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(responseCode = "404", description = "The resource you were trying to reach is not found")
    })
    @GetMapping(value = "/reciter/feedback-log/{uid}", produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<AuditHistoryEntry>> retrieveFeedbackLogByUid(@PathVariable String uid) {
        StopWatch stopWatch = new StopWatch("Get curation audit history by uid");
        stopWatch.start("Get curation audit history by uid");
        List<AuditHistoryEntry> history = feedbackLogQueryService.getAuditHistory(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(history);
    }


    private EngineParameters initializeEngineParameters(String uid, Double totalStandardizedArticleScore, RetrievalRefreshFlag retrievalRefreshFlag) {
        // find identity
        Identity identity = identityService.findByUid(uid);
        ESearchResult eSearchResults = null;
	        // find search results for this identity
	        //To Avoid 404 errors when multi threading
        try {
        	eSearchResults = eSearchResultService.findByUid(uid);
            if (eSearchResults == null) {
                log.warn("No ESearchResult for uid={}; upgrading requested flag={} to ALL_PUBLICATIONS",
                         uid, retrievalRefreshFlag);
                retrieveArticlesByUid(uid, RetrievalRefreshFlag.ALL_PUBLICATIONS);
                eSearchResults = eSearchResultService.findByUid(uid);
            } else if(eSearchResults != null && (retrievalRefreshFlag == RetrievalRefreshFlag.ALL_PUBLICATIONS || retrievalRefreshFlag == RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS)) {
            	retrieveArticlesByUid(uid, retrievalRefreshFlag);
            	eSearchResults = eSearchResultService.findByUid(uid);
            }
        } catch (EmptyResultDataAccessException e) {
            log.error("No such entity exists: "+ e);
        }
        log.info("eSearchResults size {}", eSearchResults);
		if(eSearchResults!=null && eSearchResults.getESearchPmids()!=null)
			log.info("eSearchResults esearch pmids size {}", eSearchResults.getESearchPmids().size());
		/*
		 * //This is when Pubmed returns 0 results. if(eSearchResults == null) { return
		 * null; }
		 */
        Set<Long> pmids = new HashSet<>();
        if(eSearchResults != null && eSearchResults.getESearchPmids() != null) {
	        for (ESearchPmid eSearchPmid : eSearchResults.getESearchPmids()) {
	            if (!strategyParameters.isUseGoldStandardEvidence() && StringUtils.equalsIgnoreCase(eSearchPmid.getRetrievalStrategyName(), "GoldStandardRetrievalStrategy")) {
	                log.info("Running in Testing mode so goldStandardRetreivalStrategy is removed");
	            } else {
	                pmids.addAll(eSearchPmid.getPmids());
	            }
	        }
        }

        // create a list of pmids to pass to search
        List<Long> pmidList = new ArrayList<>(pmids);
        List<Long> filtered = new ArrayList<>();
        List<String> filteredString = new ArrayList<>();
        for (long pmid : pmidList) {
            filtered.add(pmid);
            filteredString.add(String.valueOf(pmid));
        }
		 log.info("filtered  {}", filtered);
		if(filtered!=null)
		  log.info("filtered size {}", filtered.size());
        List<PubMedArticle> pubMedArticles = pubMedService.findByPmids(filtered);
		if(pubMedArticles!=null)
			log.info("pubmedArticles from the PubMedService  {}", pubMedArticles.size());
        if (pubMedArticles == null) {
            return null;
        }
        List<ScopusArticle> scopusArticles = scopusService.findByPmids(filteredString);
		if(scopusArticles!=null)
			log.info("scopusArticles from the scopusService  {}", scopusArticles.size());
        // create temporary map to retrieve Scopus articles by PMID (at the stage below)
        Map<Long, ScopusArticle> map = new HashMap<>();

        if (useScopusArticles) {
            for (ScopusArticle scopusArticle : scopusArticles) {
                map.put(scopusArticle.getPubmedId(), scopusArticle);
            }
        }
		if(map!=null)
		  log.info("scopus map size {}", map.size());
        // combine PubMed and Scopus articles into a list of ReCiterArticle
        List<ReCiterArticle> reCiterArticles = new ArrayList<>();
        for (PubMedArticle pubMedArticle : pubMedArticles) {
        	if (pubMedArticle == null) continue;
            long pmid = pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid();
            if (map.containsKey(pmid)) {
                reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, map.get(pmid), nameIgnoredCoAuthors, strategyParameters));
            } else {
                reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null, nameIgnoredCoAuthors, strategyParameters));
            }
        }
		if(reCiterArticles!=null)
			log.info("reCiterArticles size {}", reCiterArticles.size());
        //Sanitize Identity names
        AuthorNameSanitizationUtils authorNameSanitizationUtils = new AuthorNameSanitizationUtils(strategyParameters);
        identity.setSanitizedNames(authorNameSanitizationUtils.sanitizeIdentityAuthorNames(identity));
        
        //Sanitize Identity Organizational Units(Division and Department)
        InstitutionSanitizationUtil institutionalSanitizationUtil = new InstitutionSanitizationUtil();
        institutionalSanitizationUtil.populateSanitizedIdentityInstitutions(identity);
        
        //Find gender probability
        GenderProbability.getGenderIdentityProbability(identity);
        
		if(reCiterArticles!=null)
			log.info("reCiterArticles size after sanitization {}", reCiterArticles.size());
		
        // calculate precision and recall
        EngineParameters parameters = new EngineParameters();
        parameters.setIdentity(identity);
        parameters.setPubMedArticles(pubMedArticles);
        parameters.setScopusArticles(Collections.emptyList());
        parameters.setReciterArticles(reCiterArticles);

        GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
        if (goldStandard == null) {
            parameters.setKnownPmids(new ArrayList<>());
            parameters.setRejectedPmids(new ArrayList<>());
        } else {
            parameters.setKnownPmids(goldStandard.getKnownPmids());
            parameters.setRejectedPmids(goldStandard.getRejectedPmids());
        }
        if (totalStandardizedArticleScore == null) {
            parameters.setTotalStandardzizedArticleScore(strategyParameters.getTotalArticleScoreStandardizedDefault());// Configuring the totalScore in multiple of 10's in application.properties file
        } else {
            parameters.setTotalStandardzizedArticleScore(totalStandardizedArticleScore); // Configuring the totalScore in multiple of 10's in application.properties file
        }
        return parameters;
    }
}
