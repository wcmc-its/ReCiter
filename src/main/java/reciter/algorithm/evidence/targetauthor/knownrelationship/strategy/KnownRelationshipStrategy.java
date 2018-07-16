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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
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
		/*List<KnownRelationship> relationships = identity.getKnownRelationships();
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
		reCiterArticle.setRelationshipEvidence(relationshipEvidences);*/
		return score;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		double sum = 0;
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			//sum += executeStrategy(reCiterArticle, identity);
			if(reCiterArticle.getArticleId() == 29503865) {
				log.info("here");
			}
			List<KnownRelationship> relationships = identity.getKnownRelationships();
			List<RelationshipEvidence> relationshipEvidences = new ArrayList<>();
			
			if (relationships != null) {
				for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
					Set<String> relationshipTypes = new HashSet<String>();
					// do not match target author's name
					if (!author.isTargetAuthor()) {
						for (KnownRelationship authorName : relationships) {
							if (authorName.getName().firstInitialLastNameMatch(author.getAuthorName())) {
								RelationshipEvidence relationshipEvidence = new RelationshipEvidence();
								//if(StringUtils.equalsIgnoreCase(authorName.getName().getFirstName(), author.getAuthorName().getFirstName())) {
								if(authorName.getName().getFirstName().length() > 1 
										&&
										author.getAuthorName().getFirstName().startsWith(authorName.getName().getFirstName())) {
									relationshipEvidence.setRelationshipMatchType("verbose");
									relationshipEvidence.setRelationshipVerboseMatchModifierScore(ReCiterArticleScorer.strategyParameters.getRelationshipVerboseMatchModifier());
								} else {
									relationshipEvidence.setRelationshipMatchType("initial");
								}
								//log.info("[known relationship match: " +  authorName + "] ");
								reCiterArticle.getKnownRelationships().add(author);
								reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() + "[known relationship match: " +  authorName + "] ");
								sum += 1;
								reCiterArticle.getKnownRelationship().add(authorName);
								relationshipEvidence.setRelationshipMatchingScore(ReCiterArticleScorer.strategyParameters.getRelationshipMatchingScore());
								relationshipEvidence.setRelationshipName(authorName.getName());
								relationshipEvidence.setRelationshipType(relationshipTypes);
								
								if(authorName.getType().equals("mentor")) {
									relationshipEvidence.setRelationshipMatchModifierMentor(ReCiterArticleScorer.strategyParameters.getRelationshipMatchModifierMentor());
									if(reCiterArticle.getArticleCoAuthors().getNumberOfAuthors() > 0 
											&& 
											author.getAuthorName().equals(reCiterArticle.getArticleCoAuthors().getAuthors().get(reCiterArticle.getArticleCoAuthors().getNumberOfAuthors() - 1).getAuthorName())
											) { //If the matching author is the last author or senior author
										relationshipEvidence.setRelationshipMatchModifierMentorSeniorAuthor(ReCiterArticleScorer.strategyParameters.getRelationshipMatchModifierMentorSeniorAuthor());
									}
								}
								
								if(relationshipEvidences.size() > 0 
										&&
										relationshipEvidences.stream().anyMatch(evidence -> authorName.getName().getFirstName().equalsIgnoreCase(evidence.getRelationshipName().getFirstName())
												&&
												authorName.getName().getLastName().equalsIgnoreCase(evidence.getRelationshipName().getLastName()))) {
										RelationshipEvidence relationshipEvidenceInList = relationshipEvidences.stream().filter(evidence -> authorName.getName().getFirstName().equalsIgnoreCase(evidence.getRelationshipName().getFirstName())
												&&
												authorName.getName().getLastName().equalsIgnoreCase(evidence.getRelationshipName().getLastName())
												).findFirst().get();
										
										if(relationshipEvidenceInList != null) {
											if(authorName.getType().equals("mentor")) {
												relationshipEvidenceInList.setRelationshipMatchModifierMentor(ReCiterArticleScorer.strategyParameters.getRelationshipMatchModifierMentor());
												if(reCiterArticle.getArticleCoAuthors().getNumberOfAuthors() > 0 
														&& 
														author.getAuthorName().equals(reCiterArticle.getArticleCoAuthors().getAuthors().get(reCiterArticle.getArticleCoAuthors().getNumberOfAuthors() - 1).getAuthorName())
														) { //If the matching author is the last author or senior author
													relationshipEvidenceInList.setRelationshipMatchModifierMentorSeniorAuthor(ReCiterArticleScorer.strategyParameters.getRelationshipMatchModifierMentorSeniorAuthor());
												}
											}
											relationshipEvidenceInList.getRelationshipType().add(authorName.getType());
											continue;
										}
								} else {
									relationshipTypes.add(authorName.getType());
								}
								relationshipEvidences.add(relationshipEvidence);
							}
						}
					}
					
				}
				reCiterArticle.setKnownCoinvestigatorScore(sum);
			}
			if(relationshipEvidences.size() > 0) {
				reCiterArticle.setRelationshipEvidence(relationshipEvidences);
				log.info("Pmid: " + reCiterArticle.getArticleId() + " " + relationshipEvidences.toString());
			}
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
