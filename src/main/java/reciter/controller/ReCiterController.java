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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reciter.algorithm.evidence.targetauthor.TargetAuthorSelection;
import reciter.algorithm.util.ArticleTranslator;
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
import reciter.engine.erroranalysis.FilterFeedbackType;
import reciter.engine.erroranalysis.UseGoldStandard;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.features.ReCiterArticleFeatures;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.AnalysisService;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ScienceMetrixDepartmentCategoryService;
import reciter.service.ScienceMetrixService;
import reciter.service.ScopusService;
import reciter.service.dynamo.DynamoDbInstitutionAfidService;
import reciter.service.dynamo.DynamoDbMeshTermService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;
import reciter.xml.retriever.engine.RetrievalRefreshFlag;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Api(value = "ReCiterController", description = "Operations on ReCiter API.")
@Controller
public class ReCiterController {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);

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
    private DynamoDbMeshTermService dynamoDbMeshTermService;

    @Autowired
    private StrategyParameters strategyParameters;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private IDynamoDbGoldStandardService dynamoDbGoldStandardService;
    
    @Autowired
    private DynamoDbInstitutionAfidService dynamoDbInstitutionAfidService;

    @Autowired
    private ScienceMetrixService scienceMetrixService;
    
    @Autowired
    private ScienceMetrixDepartmentCategoryService scienceMetrixDepartmentCategoryService;

    @Value("${use.scopus.articles}")
    private boolean useScopusArticles;
    
    @Value("${totalArticleScore-standardized-default}")
    private double totalArticleScoreStandardizedDefault;



    @ApiOperation(value = "Update the goldstandard by passing GoldStandard model(uid, knownPmids, rejectedPmids)", notes = "This api updates the goldstandard by passing GoldStandard model(uid, knownPmids, rejectedPmids).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GoldStandard creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/goldstandard/", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GoldStandard> updateGoldStandard(@RequestBody GoldStandard goldStandard) {
        dynamoDbGoldStandardService.save(goldStandard);
        return ResponseEntity.ok(goldStandard);
    }

    @ApiOperation(value = "Update the goldstandard by passing  a list of GoldStandard model(uid, knownPmids, rejectedPmids)", notes = "This api updates the goldstandard by passing list of GoldStandard model(uid, knownPmids, rejectedPmids).")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "GoldStandard List creation successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/goldstandard/", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<List<GoldStandard>> updateGoldStandard(@RequestBody List<GoldStandard> goldStandard) {
        dynamoDbGoldStandardService.save(goldStandard);
        return ResponseEntity.ok(goldStandard);
    }

    @ApiOperation(value = "Get the goldStandard by passing an uid", notes = "This api gets the goldStandard by passing an uid.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The goldstandard retrieval for supplied uid is successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/goldstandard/{uid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<GoldStandard> retrieveGoldStandardByUid(@PathVariable String uid) {
        long startTime = System.currentTimeMillis();
        slf4jLogger.info("Start time is: " + startTime);
        GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
        return ResponseEntity.ok(goldStandard);
    }

    @ApiOperation(value = "Retrieve Articles for all UID in Identity Table", response = ResponseEntity.class, notes = "This API retrieves candidate articles for all uid in Identity Table from pubmed and its complementing articles from scopus")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list for given list of uid"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/retrieve/articles/", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity retrieveArticles(RetrievalRefreshFlag refreshFlag) {
        long startTime = System.currentTimeMillis();
        slf4jLogger.info("Start time is: " + startTime);
        LocalDate initial = LocalDate.now();

        LocalDate startDate = initial.withDayOfMonth(1);
        LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());

        List<Identity> identities = identityService.findAll();
        try {
            aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate), refreshFlag);
        } catch (IOException e) {
            slf4jLogger.info("Failed to retrieve articles.", e);
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        slf4jLogger.info("elapsed time: " + estimatedTime);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Retrieve Articles for an UID.", response = ResponseEntity.class, notes = "This API retrieves candidate articles for a given uid from pubmed and its complementing articles from scopus")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved list"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/retrieve/articles/by/uid", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseEntity retrieveArticlesByUid(String uid, RetrievalRefreshFlag refreshFlag) {
        long startTime = System.currentTimeMillis();
        long estimatedTime = 0;
        slf4jLogger.info("Start time is: " + startTime);
        List<Identity> identities = new ArrayList<>();
        LocalDate initial = LocalDate.now();
        LocalDate startDate = initial.withDayOfMonth(1);
        LocalDate endDate = LocalDate.parse("3000-01-01"); 
        Identity identity;

        try {
            identity = identityService.findByUid(uid);
        } catch (NullPointerException ne) {
            estimatedTime = System.currentTimeMillis() - startTime;
            slf4jLogger.info("elapsed time: " + estimatedTime);
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
                slf4jLogger.info("Using the cached retrieval articles for " + uid + ". Skipping the retrieval process");
                estimatedTime = System.currentTimeMillis() - startTime;
                slf4jLogger.info("elapsed time: " + estimatedTime);
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
                    slf4jLogger.info("Failed to retrieve articles.", e);
                    estimatedTime = System.currentTimeMillis() - startTime;
                    slf4jLogger.info("elapsed time: " + estimatedTime);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The uid supplied failed to retrieve articles");
                }

            } else if(refreshFlag == RetrievalRefreshFlag.ONLY_NEWLY_ADDED_PUBLICATIONS) {
            	if (identity != null)
                    identities.add(identity);
            	eSearchResult = eSearchResultService.findByUid(uid.trim()) ;
            	if (eSearchResult != null) {
            		startDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(eSearchResult.getRetrievalDate()));
            	} else {
            		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid supplied failed to retrieve articles. Try running with ALL_PUBLICATIONS refreshFlag");
            	}
            	
            	try {
                    aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate), refreshFlag);
                } catch (IOException e) {
                    slf4jLogger.info("Failed to retrieve articles.", e);
                    estimatedTime = System.currentTimeMillis() - startTime;
                    slf4jLogger.info("elapsed time: " + estimatedTime);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The uid supplied failed to retrieve articles");
                }
            	
            	
            }
        } catch (EmptyResultDataAccessException e) {
            slf4jLogger.info("No such entity exists: ", e);
        }

        estimatedTime = System.currentTimeMillis() - startTime;
        slf4jLogger.info("elapsed time: " + estimatedTime);
        return ResponseEntity.ok().body("Successfully retrieved all candidate articles for " + uid + " and refreshed all search results");
    }

    @ApiOperation(value = "Feature generation for UID.", response = ReCiterFeature.class, notes = "This api generates all the suggestion for a given uid along with its relevant evidence.")
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
    	long startTime = System.currentTimeMillis();
        long estimatedTime = 0;
        slf4jLogger.info("Start time is: " + startTime);
        
        final double totalScore;
        
        if(totalStandardizedArticleScore == null) {
        	totalScore = totalArticleScoreStandardizedDefault;
        } else {
        	totalScore = totalStandardizedArticleScore;
        }
    	
        EngineOutput engineOutput;
        EngineParameters parameters;
        List<ReCiterArticleFeature> originalFeatures = new ArrayList<ReCiterArticleFeature>();
        try {
            identityService.findByUid(uid);
        } catch (NullPointerException n) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '" + uid + "' was not found in the Identity table");
        }
        AnalysisOutput analysis = analysisService.findByUid(uid.trim());
        if (!analysisRefreshFlag 
        		&& 
        		analysis != null 
        		&& 
        		useGoldStandard == UseGoldStandard.AS_EVIDENCE) {//This was added to ensure to use analysis results only in evidence mode
        	//All the results are filtered based on filterByFeedback
        	if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
	        	analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
			            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
			            	&&
			            	(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
			            	||
			            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
			            	||
			            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
			            	.collect(Collectors.toList()));
	        	analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_ONLY) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED)
		            	.collect(Collectors.toList()));
        		analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
        	} else if(filterByFeedback == FilterFeedbackType.REJECTED_ONLY) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
		            	.collect(Collectors.toList()));
        		analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_NULL) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
		            	.collect(Collectors.toList()));
        		analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
        	} else if(filterByFeedback == FilterFeedbackType.REJECTED_AND_NULL) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
		            	.collect(Collectors.toList()));
        		analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
        	} else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_REJECTED) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED))
		            	.collect(Collectors.toList()));
        		analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
        	} else if(filterByFeedback == FilterFeedbackType.NULL) {
        		analysis.getReCiterFeature().setReCiterArticleFeatures(analysis.getReCiterFeature().getReCiterArticleFeatures().stream()
		            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
		            	&&
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL)
		            	.collect(Collectors.toList()));
        		analysis.getReCiterFeature().setCountSuggestedArticles(analysis.getReCiterFeature().getReCiterArticleFeatures().size());
        	}
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
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(String.format("The uid provided '%s' does not have any candidate records in " +
                                "ESearchResult table. Try running the candidate article retrieval api first with " +
                                "refreshFlag = true.", uid));
            }
            TargetAuthorSelection t = new TargetAuthorSelection();
            t.identifyTargetAuthor(parameters.getReciterArticles(), parameters.getIdentity());
            Iterator<ReCiterArticle> it = parameters.getReciterArticles().iterator();
            while (it.hasNext()) {
                ReCiterArticle reciterArticle = it.next();
                reciterArticle.setReCiterArticleFeatures(new ReCiterArticleFeatures(reciterArticle));
            }
            double filterScore = 0;
            if(parameters.getTotalStandardzizedArticleScore() >= strategyParameters.getMinimumStorageThreshold()) {
            	filterScore = strategyParameters.getMinimumStorageThreshold();
            } else {
            	filterScore = parameters.getTotalStandardzizedArticleScore();
            }
            Engine engine = new ReCiterEngine();
            engineOutput = engine.run(parameters, strategyParameters, filterScore);
            originalFeatures.addAll(engineOutput.getReCiterFeature().getReCiterArticleFeatures());
            
            //Store Analysis only in evidence mode
            if(useGoldStandard == UseGoldStandard.AS_EVIDENCE) {
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
        
        
        //All the results are filtered based on filterByFeedback
        if(filterByFeedback == FilterFeedbackType.ALL || filterByFeedback == null) {
        List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
        		.stream()
            	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
            			&&
		            	(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
		            	||
		            	reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
            	.collect(Collectors.toList());
        reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
        reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.ACCEPTED_ONLY){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
                			&&
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.REJECTED_ONLY){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
                			&&
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED)
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_NULL){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
                			&&
    		            	(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
                			||
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.REJECTED_AND_NULL){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
                			&&
                			(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED
                			||
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.NULL))
                	.collect(Collectors.toList());
            reCiterOutputFeature.setReCiterArticleFeatures(reCiterFilteredArticles);
            reCiterOutputFeature.setCountSuggestedArticles(reCiterFilteredArticles.size());
        } else if(filterByFeedback == FilterFeedbackType.ACCEPTED_AND_REJECTED){
        	List<ReCiterArticleFeature> reCiterFilteredArticles = originalFeatures
            		.stream()
                	.filter(reCiterArticleFeature -> reCiterArticleFeature.getTotalArticleScoreStandardized() >= totalScore
                			&&
                			(reCiterArticleFeature.getUserAssertion() == PublicationFeedback.ACCEPTED
                			||
                			reCiterArticleFeature.getUserAssertion() == PublicationFeedback.REJECTED))
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
        estimatedTime = System.currentTimeMillis() - startTime;
        slf4jLogger.info("elapsed time: " + estimatedTime);
        return new ResponseEntity<>(reCiterOutputFeature, HttpStatus.OK);
    }

    /*@ApiOperation(value = "Get the ScienceMetrix by eissn ", notes = "This api retrieves a ScienceMetrix object by eissn.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The retrieval of a ScienceMetrix object by eissn is successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/sciencemetrix/eissn/{eissn}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ScienceMetrix> retrieveScienceMetrixByEissn(@PathVariable String eissn) {
        ScienceMetrix scienceMetrices = scienceMetrixService.findByEissn(eissn);
        return ResponseEntity.ok(scienceMetrices);
    }

    @ApiOperation(value = "Get the ScienceMetrix by issn ", notes = "This api retrieves a ScienceMetrix object by issn.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The retrieval of a ScienceMetrix object by issn is successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/sciencemetrix/issn/{issn}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ScienceMetrix> retrieveScienceMetrixByIssn(@PathVariable String issn) {
        ScienceMetrix scienceMetrices = scienceMetrixService.findByIssn(issn);
        return ResponseEntity.ok(scienceMetrices);
    }


    @ApiOperation(value = "Get the ScienceMetrix by smsid ", notes = "This api retrieves a ScienceMetrix object by smsid.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The retrieval of a ScienceMetrix object by smsid is successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/sciencemetrix/smsid/{smsid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ScienceMetrix> retrieveScienceMetrixBySmsid(@PathVariable Long smsid) {
        ScienceMetrix scienceMetrices = scienceMetrixService.findBySmsid(smsid);
        return ResponseEntity.ok(scienceMetrices);
    }
    
    @ApiOperation(value = "Get the ScienceMetrix by eissn ", notes = "This api retrieves a ScienceMetrix object by eissn.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The retrieval of a ScienceMetrix object by eissn is successful"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
    })
    @RequestMapping(value = "/reciter/sciencemetrixdeptcategory/subfieldid/{subfieldid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<ScienceMetrixDepartmentCategory>> retrieveScienceMetrixDepartmentCategoryBySubfieldId(@PathVariable Long subfieldid) {
        List<ScienceMetrixDepartmentCategory> scienceMetrixDepartmentCategory = scienceMetrixDepartmentCategoryService.findByScienceMetrixJournalSubfieldId(subfieldid);
        return ResponseEntity.ok(scienceMetrixDepartmentCategory);
    }*/

    private EngineParameters initializeEngineParameters(String uid, Double totalStandardizedArticleScore, RetrievalRefreshFlag retrievalRefreshFlag) {
        // find identity
        Identity identity = identityService.findByUid(uid);
        ESearchResult eSearchResults = null;
        // find search results for this identity
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
            slf4jLogger.info("No such entity exists: ", e);
        }
        slf4jLogger.info("eSearchResults size {}", eSearchResults);
        //This is when Pubmed returns 0 results.
        if(eSearchResults == null) {
        	return null;
        }
        Set<Long> pmids = new HashSet<>();
        
        for (ESearchPmid eSearchPmid : eSearchResults.getESearchPmids()) {
            if (!strategyParameters.isUseGoldStandardEvidence() && StringUtils.equalsIgnoreCase(eSearchPmid.getRetrievalStrategyName(), "GoldStandardRetrievalStrategy")) {
                slf4jLogger.info("Running in Testing mode so goldStandardRetreivalStrategy is removed");
            } else {
                pmids.addAll(eSearchPmid.getPmids());
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
                reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, map.get(pmid)));
            } else {
                reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null));
            }
        }

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