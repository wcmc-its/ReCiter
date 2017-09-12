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
package reciter.algorithm.evidence.targetauthor.grant.strategy;

import java.util.List;

import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.GrantEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleGrant;
import reciter.model.identity.Identity;

public class GrantStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		double score = 0;
		for (ReCiterArticleGrant grant : reCiterArticle.getGrantList()) {
			for (String knownGrantIds : identity.getGrants()) {
				if (grant.getGrantID() != null && grant.getGrantID().contains(knownGrantIds)) {
					reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + " [known grant ids match=" + knownGrantIds + "], ");
					GrantEvidence grantEvidence = new GrantEvidence();
					grantEvidence.setArticleGrant(knownGrantIds);
//					grantEvidence.setInstitutionGrant();
					score += 1;
					reCiterArticle.getMatchingGrantList().add(grant);
				}
			}
		}
		return score;
	}
	
	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double score = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			score += executeStrategy(reCiterArticle, identity);
		}
		return score;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}
}
