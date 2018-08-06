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

import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.article.scorer.ArticleScorer;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.engine.analysis.ReCiterFeature;
import reciter.engine.analysis.ReCiterFeatureGenerator;
import reciter.engine.erroranalysis.Analysis;
import reciter.engine.erroranalysis.UseGoldStandard;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReCiterEngine implements Engine {

    public double clusterSimilarityThresholdScore;

    public double clutseringGrantsThreshold;

    @Override
    public EngineOutput run(EngineParameters parameters, StrategyParameters strategyParameters) {

        Identity identity = parameters.getIdentity();
        clusterSimilarityThresholdScore = strategyParameters.getClusterSimilarityThresholdScore();
        clutseringGrantsThreshold = strategyParameters.getClusteringGrantsThreshold();

        List<ReCiterArticle> reCiterArticles = parameters.getReciterArticles();

        Analysis.assignGoldStandard(reCiterArticles, parameters.getKnownPmids(), parameters.getRejectedPmids());

        // Perform Phase 1 clustering.
        Clusterer clusterer = new ReCiterClusterer(identity, reCiterArticles);
        clusterer.cluster();

        ArticleScorer articleScorer = new ReCiterArticleScorer(clusterer.getClusters(), identity, strategyParameters);
        articleScorer.runArticleScorer(clusterer.getClusters(), identity);

        Analysis analysis = Analysis.performAnalysis(clusterer, parameters.getKnownPmids(), parameters.getTotalStandardzizedArticleScore());
        log.info(clusterer.toString());
        log.info("Analysis for uid=[" + identity.getUid() + "]");
        log.info("Precision=" + analysis.getPrecision());
        log.info("Recall=" + analysis.getRecall());

        double accuracy = (analysis.getPrecision() + analysis.getRecall()) / 2.0;
        log.info("Accuracy=" + accuracy);

        log.info("True Positive List [" + analysis.getTruePositiveList().size() + "]: " + analysis.getTruePositiveList());
        log.info("True Negative List: [" + analysis.getTrueNegativeList().size() + "]: " + analysis.getTrueNegativeList());
        log.info("False Positive List: [" + analysis.getFalsePositiveList().size() + "]: " + analysis.getFalsePositiveList());
        log.info("False Negative List: [" + analysis.getFalseNegativeList().size() + "]: " + analysis.getFalseNegativeList());
        log.info("\n");

        EngineOutput engineOutput = new EngineOutput();
        engineOutput.setAnalysis(analysis);
        List<ReCiterCluster> reCiterClusters = new ArrayList<>();
        for (ReCiterCluster cluster : clusterer.getClusters().values()) {
            reCiterClusters.add(cluster);
        }
        engineOutput.setReCiterClusters(reCiterClusters);
        ReCiterFeatureGenerator reCiterFeatureGenerator = new ReCiterFeatureGenerator();
        UseGoldStandard mode;
        if (strategyParameters.isUseGoldStandardEvidence()) {
            mode = UseGoldStandard.AS_EVIDENCE;
        } else {
            mode = UseGoldStandard.FOR_TESTING_ONLY;
        }
        ReCiterFeature reCiterFeature = reCiterFeatureGenerator.computeFeatures(
                mode, parameters.getTotalStandardzizedArticleScore(),
                clusterer, parameters.getKnownPmids(), parameters.getRejectedPmids(), analysis);
        engineOutput.setReCiterFeature(reCiterFeature);
        return engineOutput;
    }

}
