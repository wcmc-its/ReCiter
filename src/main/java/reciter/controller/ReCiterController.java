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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.targetauthor.TargetAuthorSelection;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reciter.algorithm.util.ArticleTranslator;
import reciter.database.dynamodb.model.*;
import reciter.engine.Engine;
import reciter.engine.EngineOutput;
import reciter.engine.EngineParameters;
import reciter.engine.Feature;
import reciter.engine.ReCiterEngine;
import reciter.engine.StrategyParameters;
import reciter.engine.analysis.ReCiterFeature;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.features.ReCiterArticleFeatures;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.service.dynamo.DynamoDbMeshTermService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.service.dynamo.IDynamoDbInstitutionAfidService;
import reciter.service.AnalysisService;
import reciter.service.ESearchResultService;
import reciter.service.IdentityService;
import reciter.service.PubMedService;
import reciter.service.ScopusService;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Api(value="ReCiterController", description="Operations on ReCiter API.")
@Controller
public class ReCiterController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);

	private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	@Autowired
	private ESearchResultService eSearchResultService;
	
	@Autowired
    private MessageSource messageSource;

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
	private IDynamoDbInstitutionAfidService dynamoDbInstitutionAfidService;

	@Value("${use.scopus.articles}")
	private boolean useScopusArticles;

/*	@RequestMapping(value = "/reciter/retrieve/afid/by/institution", method = RequestMethod.GET)
	@ResponseBody
	public List<String> retrieveAfids(String institution) {
		return dynamoDbInstitutionAfidService.findByInstitution(institution).getAfids();
	}*/
	
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

	/**
	 * Retrieve all articles in Uids.java.
	 */
	@ApiOperation(value = "Retrieve Articles for all UID in Identity Table", response = ResponseEntity.class, notes = "This API retrieves candidate articles for all uid in Identity Table from pubmed and its complementing articles from scopus")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved list for given list of uid"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
			})
	@RequestMapping(value = "/reciter/retrieve/articles/", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity retrieveArticles(boolean refreshFlag) {
		long startTime = System.currentTimeMillis();
		slf4jLogger.info("Start time is: " + startTime);
		LocalDate initial = LocalDate.now();

		LocalDate startDate = initial.withDayOfMonth(1);
		LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());
		/* Commented hard coded uids
		    List<Identity> identities = new ArrayList<Identity>();
		 	for (String uid : Uids.uids) {
			slf4jLogger.info("Retrieving uid {}.", uid);
			Identity identity = identityService.findByUid(uid);
			identities.add(identity);
		}*/

		List<Identity> identities =  identityService.findAll();
		try {
			aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate));
		} catch (IOException e) {
			slf4jLogger.info("Failed to retrieve articles.", e);
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		slf4jLogger.info("elapsed time: " + estimatedTime);
		return ResponseEntity.ok().build();
	}

	/**
	 * Retrieve all articles in Uids.java.
	 */
	@ApiOperation(value = "Retrieve Articles for an UID.", response = ResponseEntity.class, notes = "This API retrieves candidate articles for a given uid from pubmed and its complementing articles from scopus")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved list"),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
			})
	@RequestMapping(value = "/reciter/retrieve/articles/by/uid", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public ResponseEntity retrieveArticlesByUid(String uid, boolean refreshFlag) {
		long startTime = System.currentTimeMillis();
		long estimatedTime = 0;
		slf4jLogger.info("Start time is: " + startTime);
		List<Identity> identities = new ArrayList<>();
		LocalDate initial = LocalDate.now();
		LocalDate startDate = initial.withDayOfMonth(1);
		LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());
		Identity identity = null;

		try {
			identity = identityService.findByUid(uid);
		}
		catch(NullPointerException ne) {
			estimatedTime = System.currentTimeMillis() - startTime;
			slf4jLogger.info("elapsed time: " + estimatedTime);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '"  + uid + "' was not found in the Identity table");
		}
		//Clean up the ESearchResult Table if set refreshFlag is set
		try {
			if(!refreshFlag && eSearchResultService.findByUid(uid.trim()) != null) {
				slf4jLogger.info("Using the cached retrieval articles for " + uid + ". Skipping the retrieval process");
				estimatedTime = System.currentTimeMillis() - startTime;
				slf4jLogger.info("elapsed time: " + estimatedTime);
				return ResponseEntity.status(HttpStatus.OK).body("The cached results of uid " + uid + " will be used since refreshFlag is not set to true");
			}

		else {
				if(eSearchResultService.findByUid(uid.trim()) != null)
					eSearchResultService.delete(uid.trim());
				if(identity != null)
					identities.add(identity);

				try {
					aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate));
				} catch (IOException e) {
					slf4jLogger.info("Failed to retrieve articles.", e);
					estimatedTime = System.currentTimeMillis() - startTime;
					slf4jLogger.info("elapsed time: " + estimatedTime);
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("The uid supplied failed to retreieve articles");
				}

			}
		}
		catch(EmptyResultDataAccessException e) {
			slf4jLogger.info("No such entity exists: " , e);
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
	public ResponseEntity runFeatureGenerator(@RequestParam(value="uid") String uid, boolean refreshFlag) {
		EngineOutput engineOutput = null;
		EngineParameters parameters = null;
		try {
			identityService.findByUid(uid);
		}
		catch(NullPointerException n) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The uid provided '"  + uid + "' was not found in the Identity table");
		}
		if(!refreshFlag && analysisService.findByUid(uid.trim()) !=null) {
			return new ResponseEntity<ReCiterFeature>(analysisService.findByUid(uid.trim()).getReCiterFeature(), HttpStatus.OK);
		}
		else {
			parameters = initializeEngineParameters(uid);
			TargetAuthorSelection t = new TargetAuthorSelection();
			t.identifyTargetAuthor(parameters.getReciterArticles(), parameters.getIdentity());
			Iterator<ReCiterArticle> it = parameters.getReciterArticles().iterator();
			while(it.hasNext()) {
				ReCiterArticle reciterArticle = it.next();
				//populate Features
				reciterArticle.setReCiterArticleFeatures(new ReCiterArticleFeatures(reciterArticle));
				/*boolean targetAuthor = reciterArticle.getArticleCoAuthors().getAuthors().stream().anyMatch(authors -> authors.isTargetAuthor()==true);
				if(!targetAuthor) {
					slf4jLogger.info("No Target Author for " + reciterArticle.getArticleId() + " Removing from list of articles to be analyzed");
					it.remove();
				}*/
			}
			
			Engine engine = new ReCiterEngine();
			engineOutput = engine.run(parameters, strategyParameters);
			AnalysisOutput analysisOutput = new AnalysisOutput();
			if(engineOutput != null)
				analysisOutput.setReCiterFeature(engineOutput.getReCiterFeature());
			analysisOutput.setUid(uid);
			//if(analysisOutput.getReCiterFeature() != null)
			//	analysisService.save(analysisOutput);
		}
		
		//return engineOutput.getReCiterFeature();
		return new ResponseEntity<ReCiterFeature>(engineOutput.getReCiterFeature(), HttpStatus.OK);
	}

	/*@RequestMapping(value = "/reciter/pubmed/pmid", method = RequestMethod.GET)
	@ResponseBody
	public PubMedArticle pubMedArticle(@RequestParam(value="pmid") Long pmid) {
		return pubMedService.findByPmid(pmid);
	}*/

	private EngineParameters initializeEngineParameters(String uid) {
		// find identity
		Identity identity = identityService.findByUid(uid);
		ESearchResult eSearchResults = null;
		// find search results for this identity
		try{
			 eSearchResults = eSearchResultService.findByUid(uid);
			 if(eSearchResults == null) {
				 retrieveArticlesByUid(uid, true);
				 eSearchResults = eSearchResultService.findByUid(uid);
			 }
		}
		catch(EmptyResultDataAccessException e) {
			slf4jLogger.info("No such entity exists: " , e);
		}
		slf4jLogger.info("eSearchResults size {}", eSearchResults);
		Set<Long> pmids = new HashSet<>();
		for (ESearchPmid eSearchPmid : eSearchResults.getESearchPmids()) {
			pmids.addAll(eSearchPmid.getPmids());
		}

		// create a list of pmids to pass to search
		List<Long> pmidList = new ArrayList<>(pmids);
		List<Long> filtered = new ArrayList<>();
		List<String> filteredString = new ArrayList<>();
		for (long pmid : pmidList) {
			//if (pmid <= 27090613) {
				filtered.add(pmid);
				filteredString.add(String.valueOf(pmid));
			//}
		}

		List<PubMedArticle> pubMedArticles = pubMedService.findByPmids(filtered);
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

		if (EngineParameters.getMeshCountMap() == null) {
			List<MeshTerm> meshTerms = dynamoDbMeshTermService.findAll();
			Map<String, Long> meshCountMap = new HashMap<>();
			for (MeshTerm meshTerm : meshTerms) {
				meshCountMap.put(meshTerm.getMesh(), meshTerm.getCount());
			}
			EngineParameters.setMeshCountMap(meshCountMap);
		}
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
		if (goldStandard == null) {
			parameters.setKnownPmids(new ArrayList<>());
			parameters.setRejectedPmids(new ArrayList<>());
		} else {
			parameters.setKnownPmids(goldStandard.getKnownPmids());
			parameters.setRejectedPmids(goldStandard.getRejectedPmids());
		}
		return parameters;
	}
}