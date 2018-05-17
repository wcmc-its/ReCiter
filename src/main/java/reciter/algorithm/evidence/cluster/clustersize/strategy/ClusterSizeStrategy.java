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
package reciter.algorithm.evidence.cluster.clustersize.strategy;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.cluster.AbstractRemoveClusterStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

public class ClusterSizeStrategy extends AbstractRemoveClusterStrategy {

	/**
	 * Large clusters should have more than one or two pieces of evidence supporting authorship assertion
	 * https://github.com/wcmc-its/ReCiter/issues/136
	 */
	@Override
	public double executeStrategy(ReCiterCluster reCiterCluster, Identity identity) {
		double sumOfArticleScores = 0;
		int clusterSize = reCiterCluster.getArticleCluster().size();
		for (ReCiterArticle reCiterArticle : reCiterCluster.getArticleCluster()) {
			
			if(reCiterArticle.getEmailStrategyScore() > 0)
				return 0;
			
			sumOfArticleScores += reCiterArticle.getDepartmentStrategyScore() + 
								  reCiterArticle.getKnownCoinvestigatorScore() +
								  reCiterArticle.getAffiliationScore() + 
								  reCiterArticle.getScopusStrategyScore() + 
								  reCiterArticle.getCoauthorStrategyScore() + 
								  reCiterArticle.getJournalStrategyScore() + 
								  reCiterArticle.getCitizenshipStrategyScore() + 
								  reCiterArticle.getEducationStrategyScore();
		}
		
		/*
		 * Assert that the target author did not write the publication under these circumstances:
		 * The number of records in the cluster is between 15 and 22 and there is only 1 or no pieces of evidence supporting authorship.
		 * The number of records in the cluster is between 23 and 30 and there is only 2 or fewer pieces of evidence supporting authorship.
		 * The number of records in the cluster is above 31 and there is only 3 or fewer pieces of evidence supporting authorship.
		 */
		if ((clusterSize >= 15 && clusterSize <= 22 && sumOfArticleScores <= 1) ||
			(clusterSize >= 23 && clusterSize <= 30 && sumOfArticleScores <= 2) || 
			(clusterSize >= 31 && sumOfArticleScores <= 3)) {
			
			reCiterCluster.setClusterInfo(reCiterCluster.getClusterInfo() + ", [clusterSize=" + clusterSize + " and score=" + sumOfArticleScores + "]");
			return 1;
		}
		
		return 0;
	}
}
