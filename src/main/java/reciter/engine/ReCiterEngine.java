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
import reciter.engine.erroranalysis.FilterFeedbackType;
import reciter.engine.erroranalysis.UseGoldStandard;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReCiterEngine implements Engine {

    public static double clusterSimilarityThresholdScore;

    public static double clutseringGrantsThreshold;

    @Override
    public EngineOutput run(EngineParameters parameters, StrategyParameters strategyParameters, double filterScore) {

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

        log.info(clusterer.toString());

        EngineOutput engineOutput = new EngineOutput();
        //engineOutput.setAnalysis(analysis);
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
                mode, filterScore,
                clusterer, parameters.getKnownPmids(), parameters.getRejectedPmids());
        engineOutput.setReCiterFeature(reCiterFeature);
        return engineOutput;
    }

}
