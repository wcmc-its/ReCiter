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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.evidence.targetauthor.TargetAuthorSelection;
import reciter.algorithm.util.ArticleTranslator;
import reciter.api.parameters.FilterFeedbackType;
import reciter.api.parameters.GoldStandardUpdateFlag;
import reciter.api.parameters.RetrievalRefreshFlag;
import reciter.api.parameters.UseGoldStandard;
import reciter.database.dynamodb.model.AnalysisOutput;
import reciter.database.dynamodb.model.ESearchPmid;
import reciter.database.dynamodb.model.ESearchResult;
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
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.utils.AuthorNameSanitizationUtils;
import reciter.utils.GenderProbability;
import reciter.utils.InstitutionSanitizationUtil;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Api(value = "ReCiterController", description = "Operations on ReCiter API.")
@Slf4j
@Controller
public class ReCiterController {

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

    @Value("${use.scopus.articles}")
    private boolean useScopusArticles;
    
    @Value("${totalArticleScore-standardized-default}")
    private double totalArticleScoreStandardizedDefault;
    
    @Value("${namesIgnoredCoauthors}")
    private String nameIgnoredCoAuthors;
    
    @Value("${reciter.feature.generator.keywordCountMax}")
    private double keywordsMax;

    @Value("${reciter.feature.generator.group.uids.maxCount}")
    private int uidsMaxCount;

    @ApiOperation(value = "Update the goldstandard by passing GoldStandard model(uid, knownPmids, rejectedPmids)", notes = "This api updates the goldstandard by passing GoldStandard model(uid, knownPmids, rejectedPmids).")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GoldStandard creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/goldstandard", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public ResponseEntity updateGoldStandard(@RequestBody GoldStandard goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag) {
        StopWatch stopWatch = new StopWatch("Update GoldStandard");
        stopWatch.start("Update GoldStandard");
    	if(goldStandard == null) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The api requires a GoldStandard model");
    	} else if(goldStandard != null && goldStandard.getUid() == null) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The api requires a valid uid to be passed with GoldStandard model");
    	}
    	if(goldStandardUpdateFlag == null ||
    			goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE || goldStandardUpdateFlag == GoldStandardUpdateFlag.DELETE) {
    		if(goldStandardUpdateFlag == null) {
    			dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.UPDATE);
    		} else {
    			dynamoDbGoldStandardService.save(goldStandard, goldStandardUpdateFlag);
    		}
    	} else {
    		dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.REFRESH);
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(goldStandard);
    }

    @ApiOperation(value = "Update the goldstandard by passing  a list of GoldStandard model(uid, knownPmids, rejectedPmids)", notes = "This api updates the goldstandard by passing list of GoldStandard model(uid, knownPmids, rejectedPmids).")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GoldStandard List creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/goldstandard", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<GoldStandard>> updateGoldStandard(@RequestBody List<GoldStandard> goldStandard, GoldStandardUpdateFlag goldStandardUpdateFlag) {
        StopWatch stopWatch = new StopWatch("Update GoldStandard with List");
        stopWatch.start("Update GoldStandard with List");
    	if(goldStandardUpdateFlag == null ||
    			goldStandardUpdateFlag == GoldStandardUpdateFlag.UPDATE || goldStandardUpdateFlag == GoldStandardUpdateFlag.DELETE) {
    		if(goldStandardUpdateFlag == null) {
    			dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.UPDATE);
    		} else {
    			dynamoDbGoldStandardService.save(goldStandard, goldStandardUpdateFlag);
    		}
    	} else {
    		dynamoDbGoldStandardService.save(goldStandard, GoldStandardUpdateFlag.REFRESH);
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(goldStandard);
    }

    @ApiOperation(value = "Get the goldStandard by passing an uid", notes = "This api gets the goldStandard by passing an uid.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The goldstandard retrieval for supplied uid is successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/goldstandard/{uid}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity<GoldStandard> retrieveGoldStandardByUid(@PathVariable String uid) {
        StopWatch stopWatch = new StopWatch("Get the goldStandard by passing an uid");
        stopWatch.start("Get the goldStandard by passing an uid");
        GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok(goldStandard);
    }

    @ApiOperation(value = "Retrieve Articles for all UID in Identity Table", response = ResponseEntity.class, notes = "This API retrieves candidate articles for all uid in Identity Table from pubmed and its complementing articles from scopus")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list for given list of uid"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/retrieve/articles/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity retrieveArticles(RetrievalRefreshFlag refreshFlag) {
        StopWatch stopWatch = new StopWatch("Retrieve Articles for all UID in Identity Table");
        stopWatch.start("Retrieve Articles for all UID in Identity Table");
        LocalDate initial = LocalDate.now();

        LocalDate startDate = initial.withDayOfMonth(1);
        LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());

        List<Identity> identities = identityService.findAll();
        try {
            aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate), refreshFlag);
        } catch (IOException e) {
            log.info("Failed to retrieve articles.", e);
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Retrieve Articles for an UID.", response = ResponseEntity.class, notes = "This API retrieves candidate articles for a given uid from pubmed and its complementing articles from scopus")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/retrieve/articles/by/uid", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity retrieveArticlesByUid(String uid, RetrievalRefreshFlag refreshFlag) {
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
                    log.info("Failed to retrieve articles.", e);
                    stopWatch.stop();
                    log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The uid supplied failed to retrieve articles");
                }

            } else if(refreshFlag == RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS) {
            	if (identity != null)
                    identities.add(identity);
            	eSearchResult = eSearchResultService.findByUid(uid.trim()) ;
            	if (eSearchResult != null) {
            		startDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(eSearchResult.getRetrievalDate())).minusDays(1);
            	} else {
            		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid supplied failed to retrieve articles. Try running with ALL_PUBLICATIONS refreshFlag");
            	}
            	
            	try {
                    aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate), refreshFlag);
                } catch (IOException e) {
                    log.info("Failed to retrieve articles.", e);
                    stopWatch.stop();
                    log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The uid supplied failed to retrieve articles");
                }
            	
            	
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("No such entity exists: ", e);
        }

        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return ResponseEntity.ok().body("Successfully retrieved all candidate articles for " + uid + " and refreshed all search results");
    }
    
    @ApiOperation(value = "Retrieve pending articles for a group of users.", response = ResponseEntity.class, notes = "Retrieve pending articles for a group of users.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/feature-generator/by/group", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
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
            if(uids != null && !uids.isEmpty()) {
                analysis = analysisService.findByUids(uids);
            }  else if(identitySubset != null && !identitySubset.isEmpty()){
                analysis = analysisService.findByUids(identitySubset);
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
			    					article.getTotalArticleScoreStandardized() >= totalScore)   
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

    @ApiOperation(value = "Feature generation for UID.", response = ReCiterFeature.class, notes = "This api generates all the suggestion for a given uid along with its relevant evidence.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class),
    	@ApiImplicitParam(name = "fields", value = "Fields to return (e.g., reCiterArticleFeatures.pmid,reCiterArticleFeatures.publicationType.publicationTypeCanonical). Default is all.", paramType = "query", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list", response = ReCiterFeature.class),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "The uid provided was not found in the Identity table")
    })
    @RequestMapping(value = "/reciter/feature-generator/by/uid", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity runFeatureGenerator(@RequestParam(value = "uid") String uid, Double totalStandardizedArticleScore, UseGoldStandard useGoldStandard, FilterFeedbackType filterByFeedback, boolean analysisRefreshFlag, RetrievalRefreshFlag retrievalRefreshFlag) {
    	StopWatch stopWatch = new StopWatch("Feature generation for UID");
        stopWatch.start("Feature generation for UID");
        
        final double totalScore;
        
        if(totalStandardizedArticleScore == null) {
        	totalScore = totalArticleScoreStandardizedDefault;
        } else {
        	totalScore = totalStandardizedArticleScore;
        }
    	
        EngineOutput engineOutput;
        EngineParameters parameters;
        List<ReCiterArticleFeature> originalFeatures = new ArrayList<ReCiterArticleFeature>();
        
        Identity identity = identityService.findByUid(uid);
        if(identity == null) {
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
        }
        AnalysisOutput analysis = analysisService.findByUid(uid.trim());
        if (!analysisRefreshFlag 
        		&& 
        		analysis != null 
        		&& 
        		(useGoldStandard == UseGoldStandard.AS_EVIDENCE || useGoldStandard == null)) {//This was added to ensure to use analysis results only in evidence mode
        	List<Long> finalArticles =null;
			if(analysis.getReCiterFeature()!=null && analysis.getReCiterFeature().getReCiterArticleFeatures()!=null)
			{
				finalArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
        	}
			else
			{
				finalArticles = new ArrayList<>().add(0);
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
								.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
								&&
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
								.count());
			}
			else if(analysis.getReCiterFeature()!=null)
			{
				analysis.getReCiterFeature().setCountPendingArticles(0);
			}
        	//All the results are filtered based on filterByFeedback
        	if(if(analysis.getReCiterFeature()!=null && analysis.getReCiterFeature().getReCiterArticleFeatures()!=null)
			{
			
				if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
								.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
								&&
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
								||
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
								||
								reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
								)
								.collect(Collectors.toList()));
					List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
					Analysis featureAnalysis = Analysis.performAnalysis(finalArticles, selectedArticles, knownPmids);
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
					analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
					analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
					analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());
				} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_ONLY) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED)
							.collect(Collectors.toList()));
					List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
					Analysis featureAnalysis = Analysis.performAnalysis(finalArticles, selectedArticles, knownPmids);
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
					analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
					analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
					analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());
				} else if(filterByFeedback == FilterFeedbackType.REJECTED_ONLY) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
							.collect(Collectors.toList()));
					List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
					Analysis featureAnalysis = Analysis.performAnalysis(finalArticles, selectedArticles, knownPmids);
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
					analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
					analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
					analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());
				} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_NULL) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore 
							&& 
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
							||
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
							)
							.collect(Collectors.toList()));
					List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
					Analysis featureAnalysis = Analysis.performAnalysis(finalArticles, selectedArticles, knownPmids);
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
					analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
					analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
					analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());
				} else if(filterByFeedback == FilterFeedbackType.REJECTED_AND_NULL) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
							&&
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
							||
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
							)
							.collect(Collectors.toList()));
					List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
					Analysis featureAnalysis = Analysis.performAnalysis(finalArticles, selectedArticles, knownPmids);
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
					analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
					analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
					analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());
				} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_REJECTED) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
							||
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
							.collect(Collectors.toList()));
					List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
					Analysis featureAnalysis = Analysis.performAnalysis(finalArticles, selectedArticles, knownPmids);
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
					analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
					analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
					analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());
				} else if(filterByFeedback == FilterFeedbackType.NULL) {
					analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
							.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
							&&
							reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
							.collect(Collectors.toList()));
					List<Long> selectedArticles = analysis.getReCiterFeature().getReCiterArticleFeatures().stream().map(article -> article.getPmid()).collect(Collectors.toList());
					Analysis featureAnalysis = Analysis.performAnalysis(finalArticles, selectedArticles, knownPmids);
					analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
					analysis.getReCiterFeature().setPrecision(featureAnalysis.getPrecision());
					analysis.getReCiterFeature().setRecall(featureAnalysis.getRecall());
					analysis.getReCiterFeature().setOverallAccuracy(featureAnalysis.getAccuracy());
				}
			}
			else if(analysis.getReCiterFeature()!=null)
			{
				analysis.getReCiterFeature().setReCiterArticleFeatures(new ArrayList<String>());
				analysis.getReCiterFeature().setCountSuggestedArticles(0);
				analysis.getReCiterFeature().setPrecision(0.0);
				analysis.getReCiterFeature().setRecall(0.0);
				analysis.getReCiterFeature().setOverallAccuracy(null);
			}	
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return new ResponseEntity<>(analysis.getReCiterFeature(), HttpStatus.OK);
        } else {
            if (useGoldStandard == null) {
                strategyParameters.setUseGoldStandardEvidence(true);
            } else if (useGoldStandard == UseGoldStandard.FOR_TESTING_ONLY) {
                strategyParameters.setUseGoldStandardEvidence(false);
            } else if (useGoldStandard == UseGoldStandard.AS_EVIDENCE) {
                strategyParameters.setUseGoldStandardEvidence(true);
            }

            parameters = initializeEngineParameters(uid, totalStandardizedArticleScore, retrievalRefreshFlag);
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
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= strategyParameters.getMinimumStorageThreshold()
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
            .filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
            &&
            reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
            .count());
        //All the results are filtered based on filterByFeedback
        if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
        List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
        		.stream()
            	.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
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
                	.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
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
                	.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
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
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
                			&&
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        }
        stopWatch.stop();
        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
        return new ResponseEntity<>(reCiterOutputFeature, HttpStatus.OK);
    }
    
    @ApiOperation(value = "Article retrieval by UID.", response = ReCiterFeature.class, notes = "This api returns all the publication for a supplied uid.")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "api-key", value = "api-key for this resource", paramType = "header", dataTypeClass = String.class),
    	@ApiImplicitParam(name = "fields", value = "Fields to return (e.g., reCiterArticleFeatures.pmid,reCiterArticleFeatures.publicationType.publicationTypeCanonical). Default is all.", paramType = "query", dataTypeClass = String.class)
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list", response = ReCiterFeature.class),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "The uid provided was not found in the Identity table")
    })
    @RequestMapping(value = "/reciter/article-retrieval/by/uid", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity runArticleRetrievalByUid(@RequestParam(value = "uid") String uid, Double totalStandardizedArticleScore, FilterFeedbackType filterByFeedback) {
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
        AnalysisOutput analysis = analysisService.findByUid(uid.trim());
        if (analysis != null) {//This was added to ensure to use analysis results only in evidence mode
        	if(analysis.getReCiterFeature() != null) {
        		analysis.getReCiterFeature().setInGoldStandardButNotRetrieved(null);
        		analysis.getReCiterFeature().setPrecision(null);
        		analysis.getReCiterFeature().setRecall(null);
        		analysis.getReCiterFeature().setOverallAccuracy(null);
        		if(analysis.getReCiterFeature().getReCiterArticleFeatures() != null && analysis.getReCiterFeature().getReCiterArticleFeatures().size() > 0) {
        			for(ReCiterArticleFeature reCiterArticleFeatures: analysis.getReCiterFeature().getReCiterArticleFeatures()) {
        				reCiterArticleFeatures.setEvidence(null);
        			}
        		}
        	}
        	//All the results are filtered based on filterByFeedback
        	if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
	        	analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
			            	.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
			            	&&
			            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
			            	||
			            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
			            	||
			            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
			            	)
			            	.collect(Collectors.toList()));
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_ONLY) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED)
		            	.collect(Collectors.toList()));
        	} else if(filterByFeedback == FilterFeedbackType.REJECTED_ONLY) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
		            	.collect(Collectors.toList()));
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_NULL) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore 
		            	&& 
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
		            	)
		            	.collect(Collectors.toList()));
        	} else if(filterByFeedback == FilterFeedbackType.REJECTED_AND_NULL) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> (reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
		            	)
		            	.collect(Collectors.toList()));
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_REJECTED) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
		            	.collect(Collectors.toList()));
        	} else if(filterByFeedback == FilterFeedbackType.NULL) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
		            	.collect(Collectors.toList()));
        	}
            stopWatch.stop();
            log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
            return new ResponseEntity<>(analysis.getReCiterFeature(), HttpStatus.OK);
        } 
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("There is no publications data for uid " + uid + ". Please wait while feature-generator re-runs tonight.");
       
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
                retrieveArticlesByUid(uid, RetrievalRefreshFlag.ALL_PUBLICATIONS);
                eSearchResults = eSearchResultService.findByUid(uid);
            } else if(eSearchResults != null && (retrievalRefreshFlag == RetrievalRefreshFlag.ALL_PUBLICATIONS || retrievalRefreshFlag == RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS)) {
            	retrieveArticlesByUid(uid, retrievalRefreshFlag);
            	eSearchResults = eSearchResultService.findByUid(uid);
            }
            
            
        } catch (EmptyResultDataAccessException e) {
            log.info("No such entity exists: ", e);
        }
        log.info("eSearchResults size {}", eSearchResults);
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

        List<PubMedArticle> pubMedArticles = pubMedService.findByPmids(filtered);
        if (pubMedArticles == null) {
            return null;
        }
        List<ScopusArticle> scopusArticles = scopusService.findByPmids(filteredString);

        // create temporary map to retrieve Scopus articles by PMID (at the stage below)
        Map<Long, ScopusArticle> map = new HashMap<>();

        if (useScopusArticles) {
            for (ScopusArticle scopusArticle : scopusArticles) {
                map.put(scopusArticle.getPubmedId(), scopusArticle);
            }
        }

        // combine PubMed and Scopus articles into a list of ReCiterArticle
        List<ReCiterArticle> reCiterArticles = new ArrayList<>();
        for (PubMedArticle pubMedArticle : pubMedArticles) {
            long pmid = pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid();
            if (map.containsKey(pmid)) {
                reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, map.get(pmid), nameIgnoredCoAuthors, strategyParameters));
            } else {
                reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null, nameIgnoredCoAuthors, strategyParameters));
            }
        }
        
        //Sanitize Identity names
        AuthorNameSanitizationUtils authorNameSanitizationUtils = new AuthorNameSanitizationUtils(strategyParameters);
        identity.setSanitizedNames(authorNameSanitizationUtils.sanitizeIdentityAuthorNames(identity));
        
        //Sanitize Identity Organizational Units(Division and Department)
        InstitutionSanitizationUtil institutionalSanitizationUtil = new InstitutionSanitizationUtil(strategyParameters);
        institutionalSanitizationUtil.populateSanitizedIdentityInstitutions(identity);
        
        //Find gender probability
        GenderProbability.getGenderIdentityProbability(identity);
        
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
            parameters.setTotalStandardzizedArticleScore(strategyParameters.getTotalArticleScoreStandardizedDefault());
        } else {
            parameters.setTotalStandardzizedArticleScore(totalStandardizedArticleScore);
        }
        return parameters;
    }
}