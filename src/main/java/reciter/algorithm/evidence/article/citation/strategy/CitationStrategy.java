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

import java.util.List;

import reciter.algorithm.evidence.article.AbstractReCiterArticleStrategy;
import reciter.model.article.ReCiterArticle;

public class CitationStrategy extends AbstractReCiterArticleStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {

		// check citation references in both ways.
		if (checkCitationReference(reCiterArticle, otherReCiterArticle) == 0) {
			return checkCitationReference(otherReCiterArticle, reCiterArticle);
		} else {
			return 1;
		}
	}

	private double checkCitationReference(ReCiterArticle reCiterArticle, ReCiterArticle otherReCiterArticle) {
		if (reCiterArticle.getCommentsCorrectionsPmids() != null && 
				reCiterArticle.getCommentsCorrectionsPmids().contains(otherReCiterArticle.getArticleId())) {

			reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[article " + reCiterArticle.getArticleId() + 
					"(" + reCiterArticle.getGoldStandard() + ")" + 
					" references article " + otherReCiterArticle.getArticleId() + "(" + otherReCiterArticle.getGoldStandard() + ")]");
			reCiterArticle.getCitations().add(otherReCiterArticle.getArticleId());
			reCiterArticle.getCitesInfo().append("This article cites another article with PMID of " + otherReCiterArticle.getArticleId() + ". ");
			otherReCiterArticle.getCitedByInfo().append("This article is cited by another article with PMID of " + reCiterArticle.getArticleId() + ". ");
			return 1;
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, ReCiterArticle otherReCiterArticle) {
		// TODO Auto-generated method stub
		return 0;
	}

}
