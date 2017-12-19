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
package reciter.algorithm.evidence.article.mesh.strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.Feature;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleMeshHeading;
import reciter.model.article.ReCiterCitationYNEnum;
import reciter.model.article.ReCiterMeshHeadingQualifierName;
import reciter.model.identity.Identity;

/**
 * https://github.com/wcmc-its/ReCiter/issues/131
 * 
 * @author Jie
 *
 */
public class MeshMajorStrategy extends AbstractTargetAuthorStrategy {

	private Map<String, List<Long>> meshMajorToPmid;
	private Set<String> generatedMeshMajors;
	private Map<String, Long> meshFrequency;
	private final double threshold = 0.4;
	private Map<String, Long> meshTermCache;

	private final static Logger slf4jLogger = LoggerFactory.getLogger(MeshMajorStrategy.class);

	public MeshMajorStrategy(List<ReCiterArticle> selectedReCiterArticles, Map<String, Long> meshTermCache) {
		this.meshTermCache = meshTermCache;
		meshMajorToPmid = new HashMap<>();
		meshFrequency = buildMeshFrequency(selectedReCiterArticles);
		generatedMeshMajors = buildGeneratedMesh(meshFrequency, threshold);
	}

	/**
	 * <p>
	 * Identify candidate records (currently in the negative pile) that have one or MeSH majors from the list we've 
	 * generated.
	 * <p>
	 * Discard where a name match is not possible. For example, Aaron J. Marcus (the person) could not have authored 
	 * a paper by Marcus, AF.
	 * <p>
	 * Assign any remaining articles to the positive pile. Assume that the list of MeSH major terms that we generated 
	 * step 1 is constant or fixed. i.e., Once a record is assigned to the positive pile, its list of MeSH major terms 
	 * is NOT added to the list of MeSH major terms that we generated in step 1.
	 * 
	 * @param reCiterArticle
	 * @param identity
	 * @return
	 */
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		slf4jLogger.info("Executing mesh major strategy for reCiterArticle id {} and identity {}.",
				reCiterArticle.getArticleId(), identity.getUid());
		boolean isAuthorNameMatch = matchAuthorName(reCiterArticle, identity);
		double score = 0;
		if (isAuthorNameMatch) {
			List<ReCiterArticleMeshHeading> meshHeadings = reCiterArticle.getMeshHeadings();
			for (ReCiterArticleMeshHeading meshHeading : meshHeadings) {
				String descriptorName = meshHeading.getDescriptorName().getDescriptorName();
				if (isMeshMajor(meshHeading)) { // check if this is a mesh major. (i.e., An article A may say mesh
					// heading H is a major, but another article B may say otherwise.)
					if (generatedMeshMajors.contains(descriptorName)) {
						slf4jLogger.info("Moved reCiterArticle=[" + reCiterArticle.getArticleId() + "] to 'yes` pile using "
								+ "mesh=[" + descriptorName + "] gold standard=[" + reCiterArticle.getGoldStandard() + "]");
						reCiterArticle.getMeshMajorInfo().append("This article shares MeSH major term of '" + descriptorName 
								+ "' with an article with PMIDs=" + meshMajorToPmid.get(descriptorName));
						reCiterArticle.getOverlappingMeSHMajorNegativeArticles().add(descriptorName);
						if (reCiterArticle.getClusteringEvidence().getMeshMajors() == null) {
							reCiterArticle.getClusteringEvidence().setMeshMajors(new ArrayList<>());
						}
						slf4jLogger.info("reCiter {} hashcode mesh {}", reCiterArticle.getArticleId(), reCiterArticle.hashCode());
						reCiterArticle.getClusteringEvidence().getMeshMajors().add(descriptorName);
						score += 1;
					}
				} else {
					slf4jLogger.info("reCiterArticle=[" + reCiterArticle.getArticleId() + "] " + descriptorName + " is not a mesh major.");
				}
			}
		} else {
			slf4jLogger.info("reCiterArticle=[" + reCiterArticle.getArticleId() + "] author name doesn't match.");
		}
		reCiterArticle.setMeshMajorStrategyScore(score);
		return score;
	}

	/**
	 * Constructs a map of mesh majors to its frequency of occurrence in list of ReCiterArticles.
	 * @param reCiterArticles
	 * @return
	 */
	private Map<String, Long> buildMeshFrequency(List<ReCiterArticle> reCiterArticles) {
		Map<String, Long> map = new HashMap<String, Long>();
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			List<ReCiterArticleMeshHeading> meshHeadings = reCiterArticle.getMeshHeadings();
			for (ReCiterArticleMeshHeading meshHeading : meshHeadings) {
				if (isMeshMajor(meshHeading)) { // check if it's a mesh heading.

					String descriptorName = meshHeading.getDescriptorName().getDescriptorName();
					
					// create a descriptorName to list of PMIDS map.
					if (!meshMajorToPmid.containsKey(descriptorName)) {
						List<Long> pmids = new ArrayList<>();
						pmids.add(reCiterArticle.getArticleId());
						meshMajorToPmid.put(descriptorName, pmids);
					} else {
						meshMajorToPmid.get(descriptorName).add(reCiterArticle.getArticleId());
					}
					if (!map.containsKey(descriptorName)) {
						map.put(descriptorName, 1L);
					} else {
						long currentFrequency = map.get(descriptorName);
						currentFrequency++;
						map.put(descriptorName, currentFrequency);
					}
				}
			}
		}
		return map;
	}

	/**
	 * <p>
	 * Lookup the total count of the term in all of Medline using the wcmc_mesh_raw_count table. 
	 * (If there is no such term in the table, ignore it.) For example, the count of "blood platelets" 
	 * in that table is 85,807.
	 * 
	 * <p>
	 * Divide the latter by the former and multiply by 10,000. For example, (47 / 85,807) x 10,000 = 5.477408603.
	 * 
	 * @param meshFrequency
	 * @param threshold
	 * @return
	 */
	private Set<String> buildGeneratedMesh(Map<String, Long> meshFrequency, double threshold) {
		Set<String> generatedMeshMajors = new HashSet<String>();
		for (Map.Entry<String, Long> entry : meshFrequency.entrySet()) {
			String mesh = entry.getKey();
			long meshCountInTargetAuthorArticles = entry.getValue();
			Long rawCountFromPubmed = meshTermCache.get(mesh);
			if (rawCountFromPubmed != null) {
				double score = meshCountInTargetAuthorArticles * 10000.0 / (rawCountFromPubmed) ;
				slf4jLogger.info("mesh count=[" + meshCountInTargetAuthorArticles + "], raw=[" + rawCountFromPubmed + "], mesh=[" + mesh + "], score=[" + score + "]");
				if (score > threshold) {
					generatedMeshMajors.add(mesh);
				}
			}
		}
		slf4jLogger.info("generated mesh majors = " + generatedMeshMajors);
		return generatedMeshMajors;
	}

	/**
	 * <p>
	 * MeSH major parsing
	 * <p>
	 * Easy: no subheading (e.g., for 23919362, �Contracts" is MeSH major)
	 * <p>
	 * Single subheading (e.g., for 21740463, "Cold Temperature� is MeSH major)
	 * <p>
	 * Harder: Multiple subheadings modifying the same MeSH term. (e.g., for 6358887, "Aspirin" is mesh major even 
	 * though there are cases where aspirin is not listed as MeSH major.)
	 * @param meshHeading
	 * @return
	 */
	public static boolean isMeshMajor(ReCiterArticleMeshHeading meshHeading) {
		// Easy case:
		if (meshHeading.getDescriptorName().getMajorTopicYN() == ReCiterCitationYNEnum.Y)
			return true;

		// Single subheading or Multiple subheading:
		for (ReCiterMeshHeadingQualifierName qualifierName : meshHeading.getQualifierNameList()) {
			if (qualifierName.getMajorTopicYN() == ReCiterCitationYNEnum.Y) {
				return true;
			}
		}

		return false;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
}
