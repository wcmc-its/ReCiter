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
package reciter.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.cluster.model.ReCiterCluster.MeshTermCount;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.algorithm.cluster.targetauthor.ReCiterClusterSelector;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.mesh.MeshMajorStrategyContext;
import reciter.algorithm.evidence.article.mesh.strategy.MeshMajorStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.CommonAffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.degree.DegreeStrategyContext;
import reciter.algorithm.evidence.targetauthor.degree.strategy.DegreeType;
import reciter.algorithm.evidence.targetauthor.degree.strategy.YearDiscrepancyStrategy;
import reciter.algorithm.evidence.targetauthor.department.DepartmentStrategyContext;
import reciter.algorithm.evidence.targetauthor.department.strategy.DepartmentStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.email.EmailStrategyContext;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.knownrelationship.KnownRelationshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.knownrelationship.strategy.KnownRelationshipStrategy;
import reciter.algorithm.evidence.targetauthor.scopus.ScopusStrategyContext;
import reciter.algorithm.evidence.targetauthor.scopus.strategy.ScopusCommonAffiliation;
import reciter.algorithm.util.ArticleTranslator;
import reciter.engine.analysis.ReCiterFeature;
import reciter.engine.analysis.ReCiterFeatureGenerator;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleMeshHeading;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;

public class ReCiterEngine implements Engine {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterEngine.class);
	
	@Override
	public List<Feature> generateFeature(EngineParameters parameters) {
		
		Identity identity = parameters.getIdentity();
		List<PubMedArticle> pubMedArticles = parameters.getPubMedArticles();
		List<ScopusArticle> scopusArticles = parameters.getScopusArticles();
		
		Map<Long, ScopusArticle> map = new HashMap<>();
		for (ScopusArticle scopusArticle : scopusArticles) {
			map.put(scopusArticle.getPubmedId(), scopusArticle);
		}
		List<ReCiterArticle> reCiterArticles = new ArrayList<>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			long pmid = pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid();
			if (map.containsKey(pmid)) {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, map.get(pmid)));
			} else {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null));
			}
		}
		
		Analysis.assignGoldStandard(reCiterArticles, parameters.getKnownPmids());
		
		List<Feature> features = new ArrayList<>();
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			Feature feature = new Feature();
			feature.setPmid(reCiterArticle.getArticleId());
			feature.setIsGoldStandard(reCiterArticle.getGoldStandard());
			
			TargetAuthorStrategyContext emailStrategyContext = new EmailStrategyContext(new EmailStringMatchStrategy());
			emailStrategyContext.populateFeature(reCiterArticle, identity, feature);
			
			TargetAuthorStrategyContext departmentStringMatchStrategyContext = new DepartmentStrategyContext(new DepartmentStringMatchStrategy());
			departmentStringMatchStrategyContext.populateFeature(reCiterArticle, identity, feature);
			
			TargetAuthorStrategyContext grantCoauthorStrategyContext = new KnownRelationshipStrategyContext(new KnownRelationshipStrategy());
			grantCoauthorStrategyContext.populateFeature(reCiterArticle, identity, feature);
			
			TargetAuthorStrategyContext affiliationStrategyContext = new AffiliationStrategyContext(new CommonAffiliationStrategy());
			affiliationStrategyContext.populateFeature(reCiterArticle, identity, feature);
			
			TargetAuthorStrategyContext scopusStrategyContext = new ScopusStrategyContext(new ScopusCommonAffiliation());
			scopusStrategyContext.populateFeature(reCiterArticle, identity, feature);
			
			features.add(feature);
		}
		return features;
	}
	
	@Override
	public EngineOutput run(EngineParameters parameters, StrategyParameters strategyParameters) {
		
		Identity identity = parameters.getIdentity();
		List<PubMedArticle> pubMedArticles = parameters.getPubMedArticles();
		List<ScopusArticle> scopusArticles = parameters.getScopusArticles();
		
		Map<Long, ScopusArticle> map = new HashMap<>();
		for (ScopusArticle scopusArticle : scopusArticles) {
			map.put(scopusArticle.getPubmedId(), scopusArticle);
		}
		/*List<ReCiterArticle> reCiterArticles = new ArrayList<>();
		for (PubMedArticle pubMedArticle : pubMedArticles) {
			long pmid = pubMedArticle.getMedlinecitation().getMedlinecitationpmid().getPmid();
			if (map.containsKey(pmid)) {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, map.get(pmid)));
			} else {
				reCiterArticles.add(ArticleTranslator.translate(pubMedArticle, null));
			}
		}*/
		
		List<ReCiterArticle> reCiterArticles = parameters.getReciterArticles();
		Analysis.assignGoldStandard(reCiterArticles, parameters.getKnownPmids());

		// Perform Phase 1 clustering.
		Clusterer clusterer = new ReCiterClusterer(identity, reCiterArticles);
//		int seedSize = ((Double) (parameters.getKnownPmids().size() * 1.0)).intValue();
//		Set<Long> initialSeed = new HashSet<Long>();
//		slf4jLogger.info("Initial seed size=[" + seedSize + "].");
//		int taken = 0;
//		for (long pmid : parameters.getKnownPmids()) {
//			if (taken < seedSize) {
//				initialSeed.add(pmid);
//			} else {
//				break;
//			}
//			taken++;
//		}
		
//		if (!initialSeed.isEmpty()) {
//			clusterer.cluster(initialSeed);
//		} else {
//			clusterer.cluster();
//		}
		clusterer.cluster();
		slf4jLogger.info("Phase 1 Clustering result");
		slf4jLogger.info(clusterer.toString());

		// Perform Phase 2 clusters selection.
		ClusterSelector clusterSelector = new ReCiterClusterSelector(clusterer.getClusters(), identity, strategyParameters);
		clusterSelector.runSelectionStrategy(clusterer.getClusters(), identity);
		slf4jLogger.info(clusterSelector.getSelectedClusterIds().size() +"Size");
		// Perform Mesh Heading recall improvement.
		// Use MeSH major to improve recall after phase two (https://github.com/wcmc-its/ReCiter/issues/131)
		List<ReCiterArticle> selectedArticles = new ArrayList<>();

		for (long id : clusterSelector.getSelectedClusterIds()) {
			selectedArticles.addAll(clusterer.getClusters().get(id).getArticleCluster());
		}
		
		StrategyContext meshMajorStrategyContext = new MeshMajorStrategyContext(new MeshMajorStrategy(selectedArticles, EngineParameters.getMeshCountMap()));
		clusterSelector.handleNonSelectedClusters((MeshMajorStrategyContext) meshMajorStrategyContext, clusterer.getClusters(), identity);

		StrategyContext bachelorsYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.BACHELORS));
		StrategyContext doctoralYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.DOCTORAL));
		clusterSelector.handleStrategyContext(bachelorsYearDiscrepancyStrategyContext, clusterer.getClusters(), identity);
		clusterSelector.handleStrategyContext(doctoralYearDiscrepancyStrategyContext, clusterer.getClusters(), identity);
		
		Analysis analysis = Analysis.performAnalysis(clusterer, clusterSelector, parameters.getKnownPmids());
		slf4jLogger.info(clusterer.toString());
		slf4jLogger.info("Analysis for uid=[" + identity.getUid() + "]");
		slf4jLogger.info("Precision=" + analysis.getPrecision());
		slf4jLogger.info("Recall=" + analysis.getRecall());

		double accuracy = (analysis.getPrecision() + analysis.getRecall()) / 2;
		slf4jLogger.info("Accuracy=" + accuracy);

		slf4jLogger.info("True Positive List [" + analysis.getTruePositiveList().size() + "]: " + analysis.getTruePositiveList());
		slf4jLogger.info("True Negative List: [" + analysis.getTrueNegativeList().size() + "]: " + analysis.getTrueNegativeList());
		slf4jLogger.info("False Positive List: [" + analysis.getFalsePositiveList().size() + "]: " + analysis.getFalsePositiveList());
		slf4jLogger.info("False Negative List: [" + analysis.getFalseNegativeList().size() + "]: " + analysis.getFalseNegativeList());
		slf4jLogger.info("\n");

		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			slf4jLogger.info(reCiterArticle.getArticleId() + ": " + reCiterArticle.getClusterInfo());
		}
		
		// add mesh major to analysis
		// for each cluster, count the number of MeSH terms
		for (ReCiterCluster cluster : clusterer.getClusters().values()) {
			Map<String, Long> meshCount = new HashMap<>();
			for (ReCiterArticle reCiterArticle : cluster.getArticleCluster()) {
				// calculate correct author.
				reCiterArticle.setCorrectAuthor(identity);
				
				List<ReCiterArticleMeshHeading> meshHeadings = reCiterArticle.getMeshHeadings();
				for (ReCiterArticleMeshHeading meshHeading : meshHeadings) {
					String descriptorName = meshHeading.getDescriptorName().getDescriptorName();
					if (MeshMajorStrategy.isMeshMajor(meshHeading)) { // check if this is a mesh major. (i.e., An article A may say mesh
						if (!meshCount.containsKey(descriptorName)) {
							meshCount.put(descriptorName, 1L);
						} else {
							long count = meshCount.get(descriptorName);
							meshCount.put(descriptorName, ++count);
						}
					}
				}
			}
			List<MeshTermCount> meshTermCounts = new ArrayList<>(meshCount.size());
			for (Map.Entry<String, Long> entry : meshCount.entrySet()) {
				MeshTermCount meshTermCount = new MeshTermCount();
				meshTermCount.setMesh(entry.getKey());
				meshTermCount.setCount(entry.getValue());
				meshTermCounts.add(meshTermCount);
			}
			cluster.setMeshTermCounts(meshTermCounts);
		}

		// Evidence - Gold standard is used to refine judgment of ReCiter
		if (strategyParameters.isUseGoldStandardEvidence()) {
			Set<Long> selectedClusterIds = clusterSelector.getSelectedClusterIds();
			List<ReCiterArticle> goldStandardArticles = new ArrayList<>();
			for (ReCiterCluster cluster : clusterer.getClusters().values()) {
				if (selectedClusterIds.contains(cluster.getClusterID())) {
					Iterator<ReCiterArticle> itr = cluster.getArticleCluster().iterator();
					while (itr.hasNext()) {
						ReCiterArticle reCiterArticle = itr.next();
						if (reCiterArticle.getGoldStandard() == 0) {
							goldStandardArticles.add(reCiterArticle);
						}
						itr.remove();
					}
				}
			}
			if (!goldStandardArticles.isEmpty()) {
				ReCiterCluster rejectedCluster = new ReCiterCluster();
				for (ReCiterArticle reject : goldStandardArticles) {
					rejectedCluster.add(reject);
				}
				clusterer.getClusters().put(rejectedCluster.getClusterID(), rejectedCluster);
			}
		}
		
		EngineOutput engineOutput = new EngineOutput();
		engineOutput.setAnalysis(analysis);
		List<ReCiterCluster> reCiterClusters = new ArrayList<>();
		for (ReCiterCluster cluster : clusterer.getClusters().values()) {
			// set cluster's selected field to true if this cluster has been selected.
			if (clusterSelector.getSelectedClusterIds().contains(cluster.getClusterID())) {
				cluster.setSelected(true);
			}
			reCiterClusters.add(cluster);
		}
		engineOutput.setReCiterClusters(reCiterClusters);
		ReCiterFeatureGenerator reCiterFeatureGenerator = new ReCiterFeatureGenerator();
		ReCiterFeature reCiterFeature = reCiterFeatureGenerator.computeFeatures("test", clusterer, clusterSelector, parameters.getKnownPmids(), analysis);
		engineOutput.setReCiterFeature(reCiterFeature);
		return engineOutput;
	}
	
}
