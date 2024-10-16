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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.Clusterer;
import reciter.algorithm.cluster.ReCiterClusterer;
import reciter.algorithm.cluster.article.scorer.ArticleScorer;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.feedback.article.scorer.ArticleFeedbackScorer;
import reciter.algorithm.feedback.article.scorer.ReciterFeedbackArticleScorer;
import reciter.api.parameters.UseGoldStandard;
import reciter.engine.analysis.ReCiterFeature;
import reciter.engine.erroranalysis.Analysis;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;




@Slf4j
public class ReCiterEngine implements Engine {

    public static double clusterSimilarityThresholdScore;

    public static double clutseringGrantsThreshold;
    
    @Override
    public EngineOutput run(EngineParameters parameters, StrategyParameters strategyParameters, double filterScore, double keywordsMax) {

        Identity identity = parameters.getIdentity();
        clusterSimilarityThresholdScore = strategyParameters.getClusterSimilarityThresholdScore();
        clutseringGrantsThreshold = strategyParameters.getClusteringGrantsThreshold();

        List<ReCiterArticle> reCiterArticles = parameters.getReciterArticles();

        Analysis.assignGoldStandard(reCiterArticles, parameters.getKnownPmids(), parameters.getRejectedPmids());

        //Moved to else block and eventually removed 
       /* // Perform Phase 1 clustering.
        Clusterer clusterer = new ReCiterClusterer(identity, reCiterArticles);
        clusterer.cluster();*/ 
        Clusterer clusterer=null;
        EngineOutput engineOutput = new EngineOutput();
        
        ReCiterFeatureGenerator reCiterFeatureGenerator = new ReCiterFeatureGenerator();
        UseGoldStandard mode;
        if (strategyParameters.isUseGoldStandardEvidence()) {
            mode = UseGoldStandard.AS_EVIDENCE;
        } else {
            mode = UseGoldStandard.FOR_TESTING_ONLY;
        }
      
        //Checking for Accepted and Rejected articles 
        Map<Integer, List<ReCiterArticle>> groupedByGoldStandard = reCiterArticles.stream()
	            .collect(Collectors.groupingBy(ReCiterArticle::getGoldStandard));
		
		//articles with the user assertion 1
		int acceptedArticles = groupedByGoldStandard.getOrDefault(1, Collections.emptyList()).size();
		
		//articles with the user assertion -1
		int rejectedArticles = groupedByGoldStandard.getOrDefault(-1, Collections.emptyList()).size();
        
		  System.out.println("Accepted articles: " +acceptedArticles +"  and Rejected articles:  "+ rejectedArticles);

        if(mode == UseGoldStandard.FOR_TESTING_ONLY || (acceptedArticles ==0 && rejectedArticles == 0))
        {
        	System.out.println("Coming into ReCiter Article Scorer Section***********************");
        	clusterer = new ReCiterClusterer(identity, reCiterArticles);
            clusterer.cluster();
            
	        StopWatch stopWatch = new StopWatch("Article Scorer");
	        stopWatch.start("Article Scorer");
	        ArticleScorer articleScorer = new ReCiterArticleScorer(clusterer.getClusters(), identity, strategyParameters);
	         articleScorer.runArticleScorer(clusterer.getClusters(), identity);
	         List<ReCiterArticle> allArticles = clusterer.getClusters().values().stream()
	 			    .map(ReCiterCluster::getArticleCluster) // Get each list of articles
	 			    .flatMap(List::stream)                  // Flatten the lists into a single stream
	 			    .collect(Collectors.toList());
	 		System.out.println("******************** size of teh list " + allArticles.size());
	 		if(allArticles!=null && allArticles.size() > 0)
	 			articleScorer.executePythonScriptForArticleIdentityTotalScore(allArticles,identity);
	        stopWatch.stop();
	        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
	        
        
	        log.info(clusterer.toString());
	
	       // EngineOutput engineOutput = new EngineOutput();
	        //engineOutput.setAnalysis(analysis);
	        List<ReCiterCluster> reCiterClusters = new ArrayList<>();
	        for (ReCiterCluster cluster : clusterer.getClusters().values()) {
	            reCiterClusters.add(cluster);
	        }
	        engineOutput.setReCiterClusters(reCiterClusters);
         
	        
	        ReCiterFeature reCiterFeature = reCiterFeatureGenerator.computeFeatures(
	                mode, filterScore, keywordsMax,
	                clusterer, parameters.getKnownPmids(), parameters.getRejectedPmids());
	        engineOutput.setReCiterFeature(reCiterFeature);
	
        	
        }
        else if(strategyParameters.isUseGoldStandardEvidence() && (acceptedArticles > 0 || rejectedArticles > 0)) //useGoldstandardEvidence = true then it runs.
        {	
        	System.out.println("Coming into ReCiter Article Feedback Scorer Section***********************");
        	//Identity Scoring
        	clusterer = new ReCiterClusterer(identity, reCiterArticles);
            clusterer.cluster();
            
	        StopWatch stopWatch = new StopWatch("Article Scorer");
	        stopWatch.start("Article Scorer");
	        ArticleScorer articleScorer = new ReCiterArticleScorer(clusterer.getClusters(), identity, strategyParameters);
	         articleScorer.runArticleScorer(clusterer.getClusters(), identity);
	         
	        stopWatch.stop();
	        log.info(stopWatch.getId() + " took " + stopWatch.getTotalTimeSeconds() + "s");
	        
	        //Feedback scoring
	        StopWatch stopWatchforFeedback = new StopWatch("Article Feedback Scorer");
	        stopWatchforFeedback.start("Article Feedback Scorer");
	        ArticleFeedbackScorer feedbackArticleScorer = new ReciterFeedbackArticleScorer(reCiterArticles,identity,parameters,strategyParameters);
	        feedbackArticleScorer.runFeedbackArticleScorer(reCiterArticles,identity);
	        stopWatchforFeedback.stop();
	        log.info(stopWatchforFeedback.getId() + " took " + stopWatchforFeedback.getTotalTimeSeconds() + "s");
	        log.info("knownPMIDs and Rejected PMIDs**********", parameters.getKnownPmids(), parameters.getRejectedPmids() );
	        ReCiterFeature reCiterFeature = reCiterFeatureGenerator.computeFeatures(
	                mode, filterScore, keywordsMax,
	                reCiterArticles, parameters.getKnownPmids(), parameters.getRejectedPmids(),identity);
	        engineOutput.setReCiterFeature(reCiterFeature);
        }
       
        return engineOutput;       
    }

}
