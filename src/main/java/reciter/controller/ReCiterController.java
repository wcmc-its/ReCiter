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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import reciter.Uids;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.util.ArticleTranslator;
import reciter.database.dynamodb.model.*;
//import reciter.database.mongo.model.InstitutionAfid;
//import reciter.database.mongo.model.PubMedArticleFeature;
import reciter.engine.Engine;
import reciter.engine.EngineOutput;
import reciter.engine.EngineParameters;
import reciter.engine.Feature;
import reciter.engine.ReCiterEngine;
import reciter.engine.StrategyParameters;
import reciter.engine.analysis.ReCiterFeature;
import reciter.engine.erroranalysis.Analysis;
import reciter.engine.erroranalysis.ReCiterAnalysis;
import reciter.engine.erroranalysis.ReCiterAnalysisTranslator;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;
import reciter.scopus.retriever.ScopusArticleRetriever;
import reciter.service.dynamo.DynamoDbMeshTermService;
import reciter.service.dynamo.IDynamoDbGoldStandardService;
import reciter.service.dynamo.IDynamoDbInstitutionAfidService;
import reciter.service.ldap.LdapIdentityService;
import reciter.service.AnalysisService;
import reciter.service.ESearchResultService;
import reciter.service.GoldStandardService;
import reciter.service.IdentityService;
import reciter.service.InstitutionAfidService;
import reciter.service.MeshTermService;
import reciter.service.PubMedArticleFeatureService;
import reciter.service.PubMedService;
import reciter.service.ReCiterClusterService;
import reciter.service.ScopusService;
import reciter.xml.retriever.engine.ReCiterRetrievalEngine;

@Controller
public class ReCiterController {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterController.class);

	private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
	private MeshTermService meshTermService;

	@Autowired
	private DynamoDbMeshTermService dynamoDbMeshTermService;

	@Autowired
	private PubMedArticleFeatureService pubMedArticleFeatureService;

//	@Autowired
//	private GoldStandardService goldStandardService;

	@Autowired
	private AnalysisService analysisService;

	@Autowired
	private ReCiterClusterService reCiterClusterService;

	@Autowired
	private StrategyParameters strategyParameters;
	
//	@Autowired
//	private InstitutionAfidService institutionAfidService;
	
	@Autowired
	private LdapIdentityService ldapIdentityService;

	@Autowired
	private IDynamoDbGoldStandardService dynamoDbGoldStandardService;

	@Autowired
	private IDynamoDbInstitutionAfidService dynamoDbInstitutionAfidService;

	@Value("${use.scopus.articles}")
	private boolean useScopusArticles;

	@RequestMapping(value = "/reciter/identity/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity addIdentity(@RequestBody Identity identity) {
		slf4jLogger.info("Inserting: " + identity);
		identityService.save(identity);
		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/reciter/identity/{uid}", method = RequestMethod.GET)
	@ResponseBody
	public Identity getIdentityByUid(@PathVariable String uid) {
		return identityService.findByUid(uid);
	}

	@RequestMapping(value = "/reciter/ldap/get/all/identity/", method = RequestMethod.GET)
	@ResponseBody
	public String getAllIdentity() {
		for (String uid : Uids.uids) {
			Identity identity = ldapIdentityService.getIdentity(uid);
			if (identity != null) {
				identityService.save(identity);
			} else {
				slf4jLogger.info("uid doesn't exist: " + uid);
			}
		}
		return "done";
	}

	@RequestMapping(value = "/reciter/ldap/get/identity/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public Identity getIdentity(@RequestParam String uid) {
		Identity identity = ldapIdentityService.getIdentity(uid);
		if (identity != null) {
			identityService.save(identity);
		} else {
			slf4jLogger.info("uid doesn't exist: " + uid);
		}
		return identity;
	}
	
	@RequestMapping(value = "/reciter/retrieval/and/analysis/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public ReCiterAnalysis runRetrievalAndAnalysisByUid(String uid) {
		Identity identity = getIdentity(uid);
		List<Identity> identities = new ArrayList<>();
		LocalDate initial = LocalDate.now();
		LocalDate startDate = initial.withDayOfMonth(1);
		LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());
		boolean result = false;
		identities.add(identity);
		try {
			result = aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate));
		} catch (IOException e) {
			slf4jLogger.info("Failed to retrieve articles.", e);
		}
		return result ? runReCiterAnalysis(uid) : null;
	}
	
	@RequestMapping(value = "/reciter/retrieve/afid/by/institution", method = RequestMethod.GET)
	@ResponseBody
	public List<String> retrieveAfids(String institution) {
		return dynamoDbInstitutionAfidService.findByInstitution(institution).getAfids();
	}
	
//	@RequestMapping(value = "/reciter/retrieve/goldstandard", method = RequestMethod.GET)
//	@ResponseBody
//	public void retrieveGoldStandard() {
//		long startTime = System.currentTimeMillis();
//		slf4jLogger.info("Start time is: " + startTime);
//
//		for (String uid : Uids.uids) {
//			GoldStandard goldStandard = goldStandardService.findByUid(uid);
//			try {
//				aliasReCiterRetrievalEngine.retrieveByPmids(goldStandard.getUid(), goldStandard.getKnownPmids());
//			} catch (IOException e) {
//				slf4jLogger.info("Failed to retrieve articles.", e);
//			}
//		}
//		long estimatedTime = System.currentTimeMillis() - startTime;
//		slf4jLogger.info("elapsed time: " + estimatedTime);
//	}

	@RequestMapping(value = "/reciter/goldstandard/{uid}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<GoldStandard> retrieveGoldStandardByUid(@PathVariable String uid) {
		long startTime = System.currentTimeMillis();
		slf4jLogger.info("Start time is: " + startTime);
		GoldStandard goldStandard = dynamoDbGoldStandardService.findByUid(uid);
		return ResponseEntity.ok(goldStandard);
	}

//	@RequestMapping(value = "/reciter/goldstandard/migrate", method = RequestMethod.GET)
//	@ResponseBody
//	public ResponseEntity<String> migrateGoldStandard() {
//		long startTime = System.currentTimeMillis();
//		slf4jLogger.info("Start time is: " + startTime);
//		List<Identity> identities = identityService.findAll();
//		for (Identity identity : identities) {
//			reciter.database.mongo.model.GoldStandard goldStandard = goldStandardService.findByUid(identity.getUid());
//			slf4jLogger.info("Found goldstand:" + identity.getUid());
//			dynamoDbGoldStandardService.save(new GoldStandard(identity.getUid(), goldStandard.getKnownPmids(), goldStandard.getRejectedPmids()));
//			slf4jLogger.info("Saved goldstandard:" + identity.getUid());
//		}
//		return ResponseEntity.ok("Done");
//	}

	@RequestMapping(value = "/reciter/scopusarticle/{pmid}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ScopusArticle>> retrieveScopusArticleByPmid(@PathVariable String pmid) {
		List<Long> pmids = new ArrayList<>();
		pmids.add(Long.parseLong(pmid));
		ScopusArticleRetriever<Long> scopusArticleRetriever = new ScopusArticleRetriever<>();
		List<ScopusArticle> scopusArticles =
				scopusArticleRetriever.retrieveScopus(ScopusArticleRetriever.PMID_MODIFIER, new ArrayList<>(pmids));
		return ResponseEntity.ok(scopusArticles);
	}

	@RequestMapping(value = "/reciter/goldstandard/", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<GoldStandard> updateGoldStandard(@RequestBody GoldStandard goldStandard) {
//		goldStandardService.save(goldStandard);
		return ResponseEntity.ok(goldStandard);
	}

	/**
	 * Retrieve all articles in Uids.java.
	 */
	@RequestMapping(value = "/reciter/retrieve/articles/", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity retrieveArticles() {
		long startTime = System.currentTimeMillis();
		slf4jLogger.info("Start time is: " + startTime);
		List<Identity> identities = new ArrayList<>();
		LocalDate initial = LocalDate.now();
		LocalDate startDate = initial.withDayOfMonth(1);
		LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());
		for (String uid : Uids.uids) {
			slf4jLogger.info("Retrieving uid {}.", uid);
			Identity identity = identityService.findByUid(uid);
			identities.add(identity);
		}
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
	@RequestMapping(value = "/reciter/retrieve/articles/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity retrieveArticlesByUid(String uid) {
		long startTime = System.currentTimeMillis();
		slf4jLogger.info("Start time is: " + startTime);
		List<Identity> identities = new ArrayList<>();
		LocalDate initial = LocalDate.now();
		LocalDate startDate = initial.withDayOfMonth(1);
		LocalDate endDate = initial.withDayOfMonth(initial.lengthOfMonth());
		Identity identity = identityService.findByUid(uid);
		identities.add(identity);
		try {
			aliasReCiterRetrievalEngine.retrieveArticlesByDateRange(identities, Date.valueOf(startDate), Date.valueOf(endDate));
		} catch (IOException e) {
			slf4jLogger.info("Failed to retrieve articles.", e);
			return ResponseEntity.notFound().build();
		}
		long estimatedTime = System.currentTimeMillis() - startTime;
		slf4jLogger.info("elapsed time: " + estimatedTime);
		return ResponseEntity.ok().build();
	}

	/**
	 * Run analysis for all uids in Uids.java.
	 * @return
	 */
	@RequestMapping(value = "/reciter/all/analysis/", method = RequestMethod.GET)
	@ResponseBody
	public String runAllAnalysis() {
		for (String uid : Uids.uids) {
			runAnalysis(uid);
		}
		return "Success";
	}

//	@RequestMapping(value = "/reciter/all/feature/", method = RequestMethod.GET)
//	@ResponseBody
//	public String generateAllFeatures() {
//		for (String uid : Uids.uids) {
//			runAnalysis(uid);
//			EngineParameters parameters = initializeEngineParameters(uid);
//			Engine engine = new ReCiterEngine();
//			List<Feature> features = engine.generateFeature(parameters);
//			PubMedArticleFeature articleFeatures = new PubMedArticleFeature();
//			articleFeatures.setUid(uid);
//			articleFeatures.setFeatures(features);
//			pubMedArticleFeatureService.save(articleFeatures);
//		}
//		return "Success";
//	}
	
	@RequestMapping(value = "/reciter/feature/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public List<Feature> generateFeatures(@RequestParam(value="uid") String uid) {
		EngineParameters parameters = initializeEngineParameters(uid);
		Engine engine = new ReCiterEngine();
		return engine.generateFeature(parameters);
	}

	@RequestMapping(value = "/reciter/analysis/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public Analysis runAnalysis(@RequestParam(value="uid") String uid) {

		EngineParameters parameters = initializeEngineParameters(uid);
		Engine engine = new ReCiterEngine();
		EngineOutput engineOutput = engine.run(parameters, strategyParameters);

		slf4jLogger.info(engineOutput.getAnalysis().toString());
//		analysisService.save(engineOutput.getAnalysis(), uid);
		// TODO uncomment
//		reCiterClusterService.save(engineOutput.getReCiterClusters(), uid);

		return engineOutput.getAnalysis();
	}

	@RequestMapping(value = "/reciter/feature-generator/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public ReCiterFeature runFeatureGenerator(@RequestParam(value="uid") String uid) {
		EngineParameters parameters = initializeEngineParameters(uid);
		Engine engine = new ReCiterEngine();
		EngineOutput engineOutput = engine.run(parameters, strategyParameters);
		return engineOutput.getReCiterFeature();
	}

	@RequestMapping(value = "/reciter/meshterms", method = RequestMethod.GET)
	@ResponseBody
	public int meshTerms() {
		List<reciter.database.mongo.model.MeshTerm> meshTerms = meshTermService.findAll();
		List<MeshTerm> meshTermsToSave = new ArrayList<>();
		for (reciter.database.mongo.model.MeshTerm meshTerm : meshTerms) {
			MeshTerm meshTerm1 = new MeshTerm(meshTerm.getMesh(), meshTerm.getCount());
			meshTermsToSave.add(meshTerm1);
		}
		dynamoDbMeshTermService.save(meshTermsToSave);
		return meshTermsToSave.size();
	}

//	@RequestMapping(value = "/reciter/institutionafids", method = RequestMethod.GET)
//	@ResponseBody
//	public int institutionAfids() {
//		List<reciter.database.mongo.model.InstitutionAfid> mongoInstitutionAfids = institutionAfidService.findAll();
//		Map<String, List<String>> mapping = new HashMap<>();
//		for (reciter.database.mongo.model.InstitutionAfid institutionAfid : mongoInstitutionAfids) {
//			if (!mapping.containsKey(institutionAfid.getInstitution())) {
//				List<String> afids = new ArrayList<>();
//				afids.add(institutionAfid.getAfid());
//				mapping.put(institutionAfid.getInstitution(), afids);
//			} else {
//				mapping.get(institutionAfid.getInstitution()).add(institutionAfid.getAfid());
//			}
//		}
//		List<reciter.database.dynamodb.model.InstitutionAfid> toSave = new ArrayList<>();
//		for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
//			reciter.database.dynamodb.model.InstitutionAfid institutionAfidToSave =
//					new reciter.database.dynamodb.model.InstitutionAfid(entry.getKey(), entry.getValue());
//			toSave.add(institutionAfidToSave);
//		}
//		dynamoDbInstitutionAfidService.save(toSave);
//		return toSave.size();
//	}

	@RequestMapping(value = "/reciter/pubmed/pmid", method = RequestMethod.GET)
	@ResponseBody
	public PubMedArticle pubMedArticle(@RequestParam(value="pmid") Long pmid) {
		return pubMedService.findByPmid(pmid);
	}

	@RequestMapping(value = "/reciter/scopus/id", method = RequestMethod.GET)
	@ResponseBody
	public ScopusArticle scopusArticle(@RequestParam(value="id") String id) {
		return scopusService.findByPmid(id);
	}

	@RequestMapping(value = "/reciter/analysis/web/by/uid", method = RequestMethod.GET)
	@ResponseBody
	public ReCiterAnalysis runReCiterAnalysis(@RequestParam(value="uid") String uid) {
		EngineParameters parameters = initializeEngineParameters(uid);
		Engine engine = new ReCiterEngine();
		EngineOutput engineOutput = engine.run(parameters, strategyParameters);

		slf4jLogger.info(engineOutput.getAnalysis().toString());
		analysisService.save(engineOutput.getAnalysis(), uid);
		reCiterClusterService.save(engineOutput.getReCiterClusters(), uid);

		Analysis analysis = engineOutput.getAnalysis();
		List<ReCiterCluster> reCiterClusters = engineOutput.getReCiterClusters();
		return ReCiterAnalysisTranslator.convert(uid, parameters.getKnownPmids(), analysis, reCiterClusters);
	}

	private EngineParameters initializeEngineParameters(String uid) {
		// find identity
		Identity identity = identityService.findByUid(uid);

		// find search results for this identity
		ESearchResult eSearchResults = eSearchResultService.findByUid(uid);
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
			if (pmid <= 27090613) {
				filtered.add(pmid);
				filteredString.add(String.valueOf(pmid));
			}
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
		} else {
			parameters.setKnownPmids(goldStandard.getKnownPmids());
		}
		return parameters;
	}
}