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
package reciter.engine.erroranalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.cluster.targetauthor.ClusterSelector;
import reciter.model.article.ReCiterArticle;

/**
 * Class that performs analysis such as calculating precision and recall.
 * @author jil3004
 *
 */
public class Analysis {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(Analysis.class);	

	private double precision;
	private double recall;
	
	private int truePos;
	private int trueNeg;
	private int falseNeg;
	private int falsePos;
	private int goldStandardSize;
	private int selectedClusterSize;
	private List<Long> truePositiveList = new ArrayList<Long>();
	private List<Long> trueNegativeList = new ArrayList<Long>();
	private List<Long> falsePositiveList = new ArrayList<Long>();
	private List<Long> falseNegativeList = new ArrayList<Long>();
	
	public Analysis() {}

	/**
	 * Assign gold standard to each ReCiterArticle.
	 * @param reCiterArticles
	 * @param uid
	 */
	public static void assignGoldStandard(List<ReCiterArticle> reCiterArticles, List<Long> acceptedPmids, List<Long> rejectedPmids) {
		Set<Long> pmidSet = new HashSet<>();
		acceptedPmids.stream().forEach(acceptedPmid -> {
			pmidSet.add(acceptedPmid);
		});
		
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			if (pmidSet.contains(reCiterArticle.getArticleId())) {
				reCiterArticle.setGoldStandard(1);
			} else {
				reCiterArticle.setGoldStandard(0);
			}
		}
		if(rejectedPmids != null) {
			if(pmidSet.size() > 0) {
				pmidSet.clear();
			}
			rejectedPmids.stream().forEach(rejectedPmid -> {
				pmidSet.add(rejectedPmid);
			});
			
			for (ReCiterArticle reCiterArticle : reCiterArticles) {
				if (pmidSet.contains(reCiterArticle.getArticleId())) {
					reCiterArticle.setGoldStandard(-1);
				} else {
					reCiterArticle.setGoldStandard(0);
				}
			}
		}
		
	}

	/**
	 * List of selections.
	 * @param finalCluster
	 * @param selection
	 * @param uid
	 * @return
	 */
	//public static Analysis performAnalysis(Clusterer reCiterClusterer, ClusterSelector clusterSelector, List<Long> goldStandardPmids) {
	public static Analysis performAnalysis(Clusterer reCiterClusterer, List<Long> goldStandardPmids) {
	    
		Map<Long, ReCiterCluster> finalCluster = reCiterClusterer.getClusters();
		//Set<Long> selection = clusterSelector.getSelectedClusterIds();
		String uid = reCiterClusterer.getIdentity().getUid();
		
		Analysis analysis = new Analysis();
		slf4jLogger.info("Gold Standard [" + goldStandardPmids.size() + "]: " + goldStandardPmids);

		analysis.setGoldStandardSize(goldStandardPmids.size());

		// Combine all articles into a single list.
		List<ReCiterArticle> articleList = reCiterClusterer.getReCiterArticles();//new ArrayList<ReCiterArticle>();
		/*for (long s : selection) {
			for (ReCiterArticle reCiterArticle : finalCluster.get(s).getArticleCluster()) {
				articleList.add(reCiterArticle);
			}
		}*/

		analysis.setSelectedClusterSize(articleList.size());

		for (Entry<Long, ReCiterCluster> entry : finalCluster.entrySet()) {
			for (ReCiterArticle reCiterArticle : entry.getValue().getArticleCluster()) {
				long pmid = reCiterArticle.getArticleId();
				StatusEnum statusEnum;
				if (articleList.contains(reCiterArticle) && goldStandardPmids.contains(pmid)) {
					analysis.getTruePositiveList().add(pmid);
					statusEnum = StatusEnum.TRUE_POSITIVE;
				} else if (articleList.contains(reCiterArticle) && !goldStandardPmids.contains(pmid)) {
					
					analysis.getFalsePositiveList().add(pmid);
					statusEnum = StatusEnum.FALSE_POSITIVE;
					
				} else if (!articleList.contains(reCiterArticle) && goldStandardPmids.contains(pmid)) {
					analysis.getFalseNegativeList().add(pmid);
					statusEnum = StatusEnum.FALSE_NEGATIVE;
					
				} else {
					analysis.getTrueNegativeList().add(pmid);
					statusEnum = StatusEnum.TRUE_NEGATIVE;
				}

				/*boolean isClusterOriginator = false;
				long clusterOriginator = entry.getValue().getClusterOriginator();
				if (pmid == clusterOriginator) {
					isClusterOriginator = true;
				}

				boolean isArticleSelected = false;
				if (articleList.contains(reCiterArticle)) {
					isArticleSelected = true;
				}
				
				AnalysisObject analysisObject = AnalysisTranslator.translate(
						reCiterArticle, 
						statusEnum, 
						uid, 
						reCiterClusterer.getIdentity(), 
						isClusterOriginator, 
						entry.getValue().getClusterID(), 
						entry.getValue().getArticleCluster().size(), 
						isArticleSelected);*/
				
			}
		}

		analysis.setTruePos(analysis.getTruePositiveList().size());
		analysis.setTrueNeg(analysis.getTrueNegativeList().size());
		analysis.setFalseNeg(analysis.getFalseNegativeList().size());
		analysis.setFalsePos(analysis.getFalsePositiveList().size());
		analysis.setPrecision(analysis.getPrecision());
		analysis.setRecall(analysis.getRecall());
		return analysis;
	}

	public double getPrecision() {
		if (selectedClusterSize == 0) 
			return 0;
		return (double) truePos / selectedClusterSize;
	}

	public double getRecall() {
		if (goldStandardSize == 0)
			return 0;
		return (double) truePos / goldStandardSize;
	}

	public int getTruePos() {
		return truePos;
	}

	public void setTruePos(int truePos) {
		this.truePos = truePos;
	}

	public int getGoldStandardSize() {
		return goldStandardSize;
	}

	public void setGoldStandardSize(int goldStandardSize) {
		this.goldStandardSize = goldStandardSize;
	}

	public int getSelectedClusterSize() {
		return selectedClusterSize;
	}

	public void setSelectedClusterSize(int selectedClusterSize) {
		this.selectedClusterSize = selectedClusterSize;
	}

	public List<Long> getFalsePositiveList() {
		return falsePositiveList;
	}

	public void setFalsePositiveList(List<Long> falsePositiveList) {
		this.falsePositiveList = falsePositiveList;
	}

	public List<Long> getFalseNegativeList() {
		return falseNegativeList;
	}

	public void setFalseNegativeList(List<Long> falseNegativeList) {
		this.falseNegativeList = falseNegativeList;
	}

	public int getFalseNeg() {
		return falseNeg;
	}

	public void setFalseNeg(int falseNeg) {
		this.falseNeg = falseNeg;
	}

	public int getTrueNeg() {
		return trueNeg;
	}

	public void setTrueNeg(int trueNeg) {
		this.trueNeg = trueNeg;
	}

	public int getFalsePos() {
		return falsePos;
	}

	public void setFalsePos(int falsePos) {
		this.falsePos = falsePos;
	}

	public List<Long> getTruePositiveList() {
		return truePositiveList;
	}

	public void setTruePositiveList(List<Long> truePositiveList) {
		this.truePositiveList = truePositiveList;
	}

	public List<Long> getTrueNegativeList() {
		return trueNegativeList;
	}

	public void setTrueNegativeList(List<Long> trueNegativeList) {
		this.trueNegativeList = trueNegativeList;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}
	
	@Override
	public String toString() {
		return "Analysis [precision=" + precision + ", recall=" + recall + ", truePos=" + truePos + ", trueNeg="
				+ trueNeg + ", falseNeg=" + falseNeg + ", falsePos=" + falsePos + ", goldStandardSize="
				+ goldStandardSize + ", selectedClusterSize=" + selectedClusterSize + ", truePositiveList="
				+ truePositiveList + ", trueNegativeList=" + trueNegativeList + ", falsePositiveList="
				+ falsePositiveList + ", falseNegativeList=" + falseNegativeList + "]";
	}
	
}
