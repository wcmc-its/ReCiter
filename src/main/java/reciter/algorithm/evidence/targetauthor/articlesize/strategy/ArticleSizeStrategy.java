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
package reciter.algorithm.evidence.targetauthor.articlesize.strategy;

import static org.hamcrest.CoreMatchers.sameInstance;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.ArticleCountEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleAuthors;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class ArticleSizeStrategy extends AbstractTargetAuthorStrategy {
	
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ArticleSizeStrategy.class);

	private static final int FIRST_LEVEL = 200;
	private static final int SECOND_LEVEL = 500;
	private int numberOfArticles;
	
	/**
	 * If a person has < 200 candidate publications, assume that the person wrote it in these circumstances:
	 * 1. Matching full first name (Richard Granstein, e.g., 6605225)
	 * 2. Matching first initial and middle initial (RD Granstein, e.g., 8288913)
	 * 
	 * If a person has < 500 candidate publications, assume that the person wrote it in these circumstances:
	 * 3. Both full first name and matching middle initial (Richard D. Granstein, e.g., 6231484, or Carl F. Nathan, e.g., 3989315)
	 */
	public ArticleSizeStrategy(int numberOfArticles) {
		this.numberOfArticles = numberOfArticles;
	}
	
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		ReCiterArticleAuthors authors = reCiterArticle.getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {

				String lastName = author.getAuthorName().getLastName();
				String firstName = author.getAuthorName().getFirstName();
				String middleInitial = author.getAuthorName().getMiddleInitial();

				String targetAuthorFirstName = identity.getPrimaryName().getFirstName();
				String targetAuthorMiddleInitial = identity.getPrimaryName().getMiddleInitial();
				String targetAuthorLastName = identity.getPrimaryName().getLastName();

				if (lastName.equals(targetAuthorLastName)) {
					if ((targetAuthorFirstName.equalsIgnoreCase(firstName) || 
							(middleInitial.length() > 0 && middleInitial.equalsIgnoreCase(targetAuthorMiddleInitial))) &&
							numberOfArticles < FIRST_LEVEL) {
						
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
								" [article size < 200 : pmid=" + reCiterArticle.getArticleId() + " is gold standard=" + reCiterArticle.getGoldStandard() + "]");
						
						return 1;
					} else if (targetAuthorFirstName.equalsIgnoreCase(firstName) && middleInitial.length() > 0 &&
							middleInitial.equalsIgnoreCase(targetAuthorMiddleInitial) &&
							numberOfArticles < SECOND_LEVEL) {
						
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + 
								" [article size < 500 : pmid=" + reCiterArticle.getArticleId() + " is gold standard=" + reCiterArticle.getGoldStandard() + "]");
						return 1;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		reCiterArticles.forEach(reCiterArticle -> {
		ArticleCountEvidence articleCountEvidence = new ArticleCountEvidence();
		if(this.numberOfArticles > 0) {
			articleCountEvidence.setCountArticlesRetrieved(this.numberOfArticles);
			articleCountEvidence.setArticleCountScore(-(this.numberOfArticles - ReCiterArticleScorer.strategyParameters.getArticleCountThresholdScore())/ReCiterArticleScorer.strategyParameters.getArticleCountWeight());
			reCiterArticle.setArticleCountEvidence(articleCountEvidence);
			slf4jLogger.info("Pmid: " + reCiterArticle.getArticleId() + " " + articleCountEvidence.toString());
		
		}
		});
		return 0;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		// TODO Auto-generated method stub
		
	}

}
