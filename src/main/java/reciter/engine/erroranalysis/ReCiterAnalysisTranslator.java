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
package reciter.engine.erroranalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.Citation;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.Citation.Journal;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.PositiveEvidence;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.PositiveEvidence.ClusteredWithOtherMatchingArticle;
import reciter.model.article.ReCiterArticle;

public class ReCiterAnalysisTranslator {

	public static ReCiterAnalysis convert(String uid, List<Long> goldStandard, Analysis analysis, List<ReCiterCluster> reCiterClusters) {
		ReCiterAnalysis reCiterAnalysis = new ReCiterAnalysis();
		reCiterAnalysis.setCwid(uid);
		List<ReCiterAnalysisArticle> reCiterAnalysisArticles = new ArrayList<>();
		reCiterAnalysis.setReCiterAnalysisArticles(reCiterAnalysisArticles);
		Set<Long> goldStandardNotRetrieved = new HashSet<>(goldStandard);
		for (ReCiterCluster cluster : reCiterClusters) {
			for (ReCiterArticle reCiterArticle : cluster.getArticleCluster()) {
				if (goldStandardNotRetrieved.contains(reCiterArticle.getArticleId())) {
					goldStandardNotRetrieved.remove(reCiterArticle.getArticleId());
				}
				if (reCiterArticle.getGoldStandard() == 1 && cluster.isSelected()) { // https://github.com/wcmc-its/ReCiter/issues/156
					ReCiterAnalysisArticle article = new ReCiterAnalysisArticle();
					reCiterAnalysisArticles.add(article);
					article.setPmid(reCiterArticle.getArticleId());
					Citation citation = new Citation();
					article.setCitation(citation);
					citation.setPubDate(reCiterArticle.getJournal().getJournalIssuePubDateYear());
					citation.setAuthorList(reCiterArticle.getArticleCoAuthors().getAuthors());
					citation.setVolume(reCiterArticle.getJournal().getJournalTitle());
					Journal journal = new Journal();
					citation.setJournal(journal);
					journal.setVerbose(reCiterArticle.getJournal().getIsoAbbreviation());
					journal.setMedlineTA(reCiterArticle.getJournal().getJournalTitle());

					if (reCiterArticle.getScopusArticle() != null && reCiterArticle.getScopusArticle().getDoi() != null) {
						citation.setDoi(reCiterArticle.getScopusArticle().getDoi());
					}

					article.setUserAssertion(null);

					PositiveEvidence positiveEvidence = new PositiveEvidence();
					article.setPositiveEvidence(positiveEvidence);

					positiveEvidence.setMatchingNameVariant(reCiterArticle.getCorrectAuthor());
					positiveEvidence.setMatchingDepartment(reCiterArticle.getMatchingDepartment());
					positiveEvidence.setMatchingRelationships(reCiterArticle.getKnownRelationship());
					positiveEvidence.setMatchingInstitutionTargetAuthors(reCiterArticle.getFrequentInstitutionalCollaborators());
					// TODO matchingInstitutionFrequentCollaborator
					positiveEvidence.setMatchingGrantIDs(reCiterArticle.getMatchingGrantList());
					positiveEvidence.setMatchingEmails(reCiterArticle.getMatchingEmails());
					positiveEvidence.setPublishedPriorAcademicDegreeBachelors(reCiterArticle.getPublishedPriorAcademicDegreeBachelors());
					positiveEvidence.setPublishedPriorAcademicDegreeDoctoral(reCiterArticle.getPublishedPriorAcademicDegreeDoctoral());

					ClusteredWithOtherMatchingArticle clusteredWithOtherMatchingArticle = new ClusteredWithOtherMatchingArticle();
					positiveEvidence.setClusteredWithOtherMatchingArticle(clusteredWithOtherMatchingArticle);

					clusteredWithOtherMatchingArticle.setMeshMajor(reCiterArticle.getMeshMajorInfo().toString());
					clusteredWithOtherMatchingArticle.setCites(reCiterArticle.getCitesInfo().toString());
					clusteredWithOtherMatchingArticle.setCitedBy(reCiterArticle.getCitedByInfo().toString());
					clusteredWithOtherMatchingArticle.setCoCitation(reCiterArticle.getCoCitationInfo().toString());
					clusteredWithOtherMatchingArticle.setJournalTitle(reCiterArticle.getJournalTitleInfo().toString());

					// set score:
					/* +10 for email match
					 * +2 for full exact name match (firstName, middle initial, lastName)
					 * +1 for abbreviated name match (firstInitial, middle initial, lastName)
					 * +1 for every other type of evidence... Count each instance of department, institutional affiliation, 
					 * common collaborator, known relationship, etc. separately as an additional point. */
					int score = 0;
					if (reCiterArticle.getEmailStrategyScore() > 0) {
						score += 10;
					}
					if (reCiterArticle.getCorrectAuthor() != null) {
						score += 2;
					}
					// TODO: +1 for abbreviated name match.

					// +1 for other types of evidence
					score += reCiterArticle.getAffiliationScore() 
							+ reCiterArticle.getBoardCertificationStrategyScore()
							+ reCiterArticle.getCitizenshipStrategyScore()
							+ reCiterArticle.getCoauthorStrategyScore()
							+ reCiterArticle.getDepartmentStrategyScore()
							+ reCiterArticle.getEducationStrategyScore()
							+ reCiterArticle.getInternshipAndResidenceStrategyScore()
							+ reCiterArticle.getJournalStrategyScore()
							+ reCiterArticle.getKnownCoinvestigatorScore()
							+ reCiterArticle.getMeshMajorStrategyScore()
							+ reCiterArticle.getScopusStrategyScore();
					article.setScore(score);
				}
			}
		}
		reCiterAnalysis.setNotRetrievedGoldStandards(new ArrayList<>(goldStandardNotRetrieved));
		reCiterAnalysis.setNumSuggestedArticles(reCiterAnalysisArticles.size());
		
		return reCiterAnalysis;
	}
}
