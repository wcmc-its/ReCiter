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
package reciter.algorithm.evidence.targetauthor.knownrelationship.strategy;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.evidence.targetauthor.AbstractTargetAuthorStrategy;
import reciter.engine.Feature;
import reciter.engine.analysis.evidence.RelationshipEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;
import reciter.model.identity.KnownRelationship;

@Slf4j
public class KnownRelationshipStrategy extends AbstractTargetAuthorStrategy {

	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		double score = 0;
		List<KnownRelationship> relationships = identity.getKnownRelationships();
		List<RelationshipEvidence> relationshipEvidences = new ArrayList<>();
		if (relationships != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				// do not match target author's name
				if (!author.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName())) {
					for (KnownRelationship authorName : relationships) {
						if (authorName.getName().isFullNameMatch(author.getAuthorName())) {
							log.info("[known relationship match: " +  authorName + "] ");
							reCiterArticle.getKnownRelationships().add(author);
							reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[known relationship match: " +  authorName + "] ");
							score += 1;
							reCiterArticle.getKnownRelationship().add(authorName);
							RelationshipEvidence relationshipEvidence = new RelationshipEvidence();
							relationshipEvidence.setRelationshipName(authorName.getName());
							relationshipEvidence.setRelationshipType(authorName.getType());
							relationshipEvidences.add(relationshipEvidence);
						}
					}
				}
			}
			reCiterArticle.setKnownCoinvestigatorScore(score);
		}
		reCiterArticle.setRelationshipEvidence(relationshipEvidences);
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			sum += executeStrategy(reCiterArticle, identity);
		}
		return sum;
	}

	@Override
	public void populateFeature(ReCiterArticle reCiterArticle, Identity identity, Feature feature) {
		int score = 0;
		List<KnownRelationship> relationships = identity.getKnownRelationships();

		if (relationships != null) {
			for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
				// do not match target author's name
				if (!author.getAuthorName().firstInitialLastNameMatch(identity.getPrimaryName())) {
					for (KnownRelationship authorName : relationships) {
						if (authorName.getName().isFullNameMatch(author.getAuthorName())) {
							score += 1;
						}
					}
				}
			}
			reCiterArticle.setKnownCoinvestigatorScore(score);
		}
		feature.setNumKnownRelationships(score);
	}

}
