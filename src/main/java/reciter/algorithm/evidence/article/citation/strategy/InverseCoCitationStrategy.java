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
package reciter.algorithm.evidence.article.citation.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticle.CoCitation;

/**
 * Cites: A (positive) and B (candidate article)
 * CitedBy:
 * @author Jie
 *
 */
public class InverseCoCitationStrategy extends AbstractReCiterArticleStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		return checkInverseCoCitationReference(reCiterArticle, otherReCiterArticle);
	}

	private double checkInverseCoCitationReference(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		int count = 0;
		Set<Long> sharedPmids = new HashSet<>();
		if (reCiterArticle.getCommentsCorrectionsPmids() != null && otherReCiterArticle.getCommentsCorrectionsPmids() != null) {

			Set<Long> pmids = reCiterArticle.getCommentsCorrectionsPmids();
			for (long pmid : pmids) {
				if (otherReCiterArticle.getCommentsCorrectionsPmids().contains(pmid)) {
					count++;
					sharedPmids.add(pmid);
				}
			}
		}

		reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
				"[article " + reCiterArticle.getArticleId() + "(" + reCiterArticle.getGoldStandard() + ") " +
				" and article " + otherReCiterArticle.getArticleId() + "(" + otherReCiterArticle.getGoldStandard() + ")" + 
				" share " + count + " references and those are + " + sharedPmids + "], ");
		
		if (!sharedPmids.isEmpty()) {
			reCiterArticle.getCoCitationInfo().append("This article and clustered article with PMID of " + otherReCiterArticle.getArticleId() +
					" both cites articles " + sharedPmids);
		}
		if (count > 0) {
			CoCitation coCitation = new CoCitation();
			coCitation.setPmid(reCiterArticle.getArticleId());
			coCitation.setPmids(new ArrayList<>(sharedPmids));
			reCiterArticle.getCoCitation().add(coCitation);
			otherReCiterArticle.getCoCitation().add(coCitation);
		}
		return count;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

}
