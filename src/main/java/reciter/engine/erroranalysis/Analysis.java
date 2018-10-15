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

import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.engine.analysis.ReCiterArticleFeature;
import reciter.model.article.ReCiterArticle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that performs analysis such as calculating precision and recall.
 *
 * @author jil3004
 */
public class Analysis {

    private double precision;
    private double recall;
    private double accuracy;

    private int truePos;
    private int trueNeg;
    private int falseNeg;
    private int falsePos;
    private int goldStandardSize;
    private int selectedClusterSize;
    private List<Long> truePositiveList = new ArrayList<>();
    private List<Long> trueNegativeList = new ArrayList<>();
    private List<Long> falsePositiveList = new ArrayList<>();
    private List<Long> falseNegativeList = new ArrayList<>();

    public Analysis() {
    }

    /**
     * Assign gold standard to each ReCiterArticle.
     *
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
            } 
        }
        if (rejectedPmids != null) {
            if (pmidSet.size() > 0) {
                pmidSet.clear();
            }
            rejectedPmids.stream().forEach(rejectedPmid -> {
                pmidSet.add(rejectedPmid);
            });

            for (ReCiterArticle reCiterArticle : reCiterArticles) {
                if (pmidSet.contains(reCiterArticle.getArticleId())) {
                    reCiterArticle.setGoldStandard(-1);
                }
            }
        }

    }

    public static Analysis performAnalysis(List<Long> finalArticles, List<Long> selectedArticles, List<Long> goldStandardPmids) {

        //Map<Long, ReCiterCluster> finalCluster = reCiterClusterer.getClusters();

        Analysis analysis = new Analysis();

        analysis.setGoldStandardSize(goldStandardPmids.size());

        // Combine all articles into a single list.
        //List<ReCiterArticle> articleList = reCiterClusterer.getReCiterArticles().stream().filter(reCiterArticle -> reCiterArticle.getTotalArticleScoreStandardized() >= totalStandardzizedArticleScore).collect(Collectors.toList());//new ArrayList<ReCiterArticle>();

        analysis.setSelectedClusterSize(selectedArticles.size());

            for (Long selectedArticle : selectedArticles) {
                StatusEnum statusEnum;
                if (finalArticles.contains(selectedArticle) && goldStandardPmids.contains(selectedArticle)) {
                    analysis.getTruePositiveList().add(selectedArticle);
                    statusEnum = StatusEnum.TRUE_POSITIVE;
                } else if (finalArticles.contains(selectedArticle) && !goldStandardPmids.contains(selectedArticle)) {
                    analysis.getFalsePositiveList().add(selectedArticle);
                    statusEnum = StatusEnum.FALSE_POSITIVE;

                } else if (!finalArticles.contains(selectedArticle) && goldStandardPmids.contains(selectedArticle)) {
                    analysis.getFalseNegativeList().add(selectedArticle);
                    statusEnum = StatusEnum.FALSE_NEGATIVE;

                } else {
                    analysis.getTrueNegativeList().add(selectedArticle);
                    statusEnum = StatusEnum.TRUE_NEGATIVE;
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

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        if (goldStandardSize == 0)
            return 0;
        return (double) truePos / goldStandardSize;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }
    
    public double getAccuracy() {
		return (this.precision + this.recall)/2;
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

    @Override
    public String toString() {
        return "Analysis [precision=" + precision + ", recall=" + recall + ", truePos=" + truePos + ", trueNeg="
                + trueNeg + ", falseNeg=" + falseNeg + ", falsePos=" + falsePos + ", goldStandardSize="
                + goldStandardSize + ", selectedClusterSize=" + selectedClusterSize + ", truePositiveList="
                + truePositiveList + ", trueNegativeList=" + trueNegativeList + ", falsePositiveList="
                + falsePositiveList + ", falseNegativeList=" + falseNegativeList + "]";
    }

}
