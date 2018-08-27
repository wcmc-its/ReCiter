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
package reciter.algorithm.evidence.targetauthor.degree.strategy;

import java.time.LocalDate;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import reciter.algorithm.cluster.article.scorer.ReCiterArticleScorer;
import reciter.algorithm.evidence.article.AbstractRemoveReCiterArticleStrategy;
import reciter.engine.analysis.evidence.EducationYearEvidence;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

/**
 * Year-based matching for TargetAuthor (Phase II)
 * 
 * <p> Returns a year discrepancy score between an article's journal's
 * issue pub date year and a target author's terminal degree.
 * 
 * <p>Because this class extends {@code AbstractTargetAuthorStrategy}, it
 * implements the year-based matching for a target author (i.e., Phase II)
 * matching).
 * 
 * @author jil3004
 *
 */
@Slf4j
public class YearDiscrepancyStrategy extends AbstractRemoveReCiterArticleStrategy {

	/**
	 * DegreeType used by this strategy.
	 */
	private final DegreeType degreeType;

	/**
	 * Constructor for YearDiscrepancyStrategy. Requires a DegreeType.
	 * 
	 * @param degreeType DegreeType used by this strategy.
	 */
	public YearDiscrepancyStrategy(DegreeType degreeType) {
		this.degreeType = degreeType;
	}

	/**
	 * <p>
	 * Identify year of publications for the article.
	 * Get the target author's terminal degree from rc_identity_degree.

	 * If discrepancy between pub and doctoral degree < -5, mark as false 
	 * </p>
	 * 
	 * <p>
	 * Example: 
	 * pubyear = 1990, doctoral degree = 1994, difference is -4, -5 < -4, therefore do nothing
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * pubyear = 1998, doctoral degree = 1994, difference is 4, -5 < 4, therefore do nothing.
	 * </p>
	 * 
	 * <p>
	 * If discrepancy between pub and bachelor degree < 1, mark as false
	 * Example:
	 * pubyear = 1998, bachelor degree = 1998, difference is 0, 1 < 0 is not true, therefore mark as false
	 * </p>
	 */
	@Override
	public double executeStrategy(ReCiterArticle reCiterArticle, Identity identity) {
		if (reCiterArticle != null 												&&
				reCiterArticle.getJournal() != null 							&& 
				reCiterArticle.getJournal().getJournalIssuePubDateYear() != 0) {

			int year = reCiterArticle.getJournal().getJournalIssuePubDateYear();

			int difference;
			if (degreeType.equals(DegreeType.BACHELORS)) {
				if (identity.getDegreeYear().getBachelorYear() != 0) {
					difference = year - identity.getDegreeYear().getBachelorYear();
					reCiterArticle.setBachelorsYearDiscrepancy(difference);
					if (difference < 1) {
						log.info("Bachelors: Identity degree and reCiter article {} journal issue publication date difference < 1. Remove from cluster.", reCiterArticle.getArticleId());
						reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
								+ " [Bachelors Degree Difference=" + difference + "]");
						reCiterArticle.setBachelorsYearDiscrepancyScore(1);
						reCiterArticle.setPublishedPriorAcademicDegreeBachelors("Target Author bachelors graduation year: " +
								identity.getDegreeYear().getBachelorYear() + " publication date: " + year + ". Diff="+ difference);
						return 1;
					}
				}
			} else if (degreeType.equals(DegreeType.DOCTORAL)) {
				if (identity.getDegreeYear().getDoctoralYear() != 0) {
					int doctoral = identity.getDegreeYear().getDoctoralYear();
					difference = year - doctoral;
					reCiterArticle.setDoctoralYearDiscrepancy(difference);
					if (doctoral < 1998) {
						if (difference < -6) {
							log.info("DOCTORAL 1998: Identity degree and reCiter article {} journal issue publication date difference < -6" +
									". Remove from cluster.", reCiterArticle.getArticleId());
							reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
									+ " [Doctoral Degree Difference (<1988) =" + difference + "]");
							reCiterArticle.setDoctoralYearDiscrepancyScore(1);
							reCiterArticle.setPublishedPriorAcademicDegreeDoctoral("(Case doctoral < 1998) Target Author doctoral graduation year: " +
									identity.getDegreeYear().getDoctoralYear() + " publication date: " + year + ". Diff="+ difference);
							return 1;
						}
					} else {
						if (difference < -13) {
							log.info("DOCTORAL: Identity degree and reCiter article {} journal issue publication date difference < -13. " +
									"Remove from cluster.", reCiterArticle.getArticleId());

							reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
									+ " [Doctoral Degree Difference (>=1998) =" + difference + "]");
							reCiterArticle.setDoctoralYearDiscrepancyScore(1);
							reCiterArticle.setPublishedPriorAcademicDegreeDoctoral("Target Author doctoral graduation year: " +
									identity.getDegreeYear().getDoctoralYear() + " publication date: " + year + ". Diff="+ difference);
							return 1;
						}
					}
				}
			}
		}
		return 0;
	}

	@Override
	public double executeStrategy(List<ReCiterArticle> reCiterArticles, Identity identity) {
		for(ReCiterArticle reCiterArticle: reCiterArticles) {
			EducationYearEvidence educationYearEvidence = null;
			if (reCiterArticle != null 												&&
					reCiterArticle.getPublicationDateStandardized() != null) {
					//reCiterArticle.getJournal() != null 							&& 
					//reCiterArticle.getJournal().getJournalIssuePubDateYear() != 0) {
				LocalDate date = LocalDate.parse(reCiterArticle.getPublicationDateStandardized());

				int year = date.getYear();//reCiterArticle.getJournal().getJournalIssuePubDateYear();

				int difference;
				if (degreeType.equals(DegreeType.BACHELORS)) {
					if (identity.getDegreeYear().getBachelorYear() != 0) {
						difference = year - identity.getDegreeYear().getBachelorYear();
						reCiterArticle.setBachelorsYearDiscrepancy(difference);
						educationYearEvidence = new EducationYearEvidence();
						educationYearEvidence.setIdentityBachelorYear(identity.getDegreeYear().getBachelorYear());
						educationYearEvidence.setArticleYear(year);
						reCiterArticle.setEducationYearEvidence(educationYearEvidence);
						//if (difference < 1) {
						if(year < ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearBachelorThreshold() + identity.getDegreeYear().getBachelorYear()) {
							//log.info("Bachelors: Identity degree and reCiter article {} journal issue publication date difference < 1. Remove from cluster.", reCiterArticle.getArticleId());
							reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
									+ " [Bachelors Degree Difference=" + difference + "]");
							reCiterArticle.setBachelorsYearDiscrepancyScore(1);
							reCiterArticle.setPublishedPriorAcademicDegreeBachelors("Target Author bachelors graduation year: " +
									identity.getDegreeYear().getBachelorYear() + " publication date: " + year + ". Diff="+ difference);
							educationYearEvidence.setDiscrepancyDegreeYearBachelor(difference);
							educationYearEvidence.setDiscrepancyDegreeYearBachelorScore(ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearBachelorScore());
							
							//return 1;
						} else {
							educationYearEvidence.setDiscrepancyDegreeYearBachelor(difference);
							educationYearEvidence.setDiscrepancyDegreeYearBachelorScore(0);
						}
					}
				} else if (degreeType.equals(DegreeType.DOCTORAL)) {
					if (identity.getDegreeYear().getDoctoralYear() != 0) {
						int doctoral = identity.getDegreeYear().getDoctoralYear();
						difference = year - doctoral;
						reCiterArticle.setDoctoralYearDiscrepancy(difference);
						if (doctoral < ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers()) {
							if (year < doctoral + ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearDoctoralThreshold1()) {
								//log.info("DOCTORAL 1998: Identity degree and reCiter article {} journal issue publication date difference < -6" +
								//		". Remove from cluster.", reCiterArticle.getArticleId());
								reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
										+ " [Doctoral Degree Difference (<1988) =" + difference + "]");
								reCiterArticle.setDoctoralYearDiscrepancyScore(1);
								reCiterArticle.setPublishedPriorAcademicDegreeDoctoral("(Case doctoral < 1998) Target Author doctoral graduation year: " +
										identity.getDegreeYear().getDoctoralYear() + " publication date: " + year + ". Diff="+ difference);
								if(reCiterArticle.getEducationYearEvidence() != null) {
									reCiterArticle.getEducationYearEvidence().setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoral(difference);
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoralScore(ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearDoctoralScore());
								} else {
									educationYearEvidence = new EducationYearEvidence();
									educationYearEvidence.setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									educationYearEvidence.setArticleYear(year);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoral(difference);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoralScore(ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearDoctoralScore());
									reCiterArticle.setEducationYearEvidence(educationYearEvidence);
								}
								//return 1;
							} else {
								if(reCiterArticle.getEducationYearEvidence() != null) {
									reCiterArticle.getEducationYearEvidence().setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoral(difference);
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoralScore(0);
								} else {
									educationYearEvidence = new EducationYearEvidence();
									educationYearEvidence.setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									educationYearEvidence.setArticleYear(year);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoral(difference);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoralScore(0);
									reCiterArticle.setEducationYearEvidence(educationYearEvidence);
								}
							}
							
						} else {
							if (year < doctoral + ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearDoctoralThreshold2()) {
								//log.info("DOCTORAL: Identity degree and reCiter article {} journal issue publication date difference < -13. " +
								//		"Remove from cluster.", reCiterArticle.getArticleId());

								reCiterArticle.setClusterInfo(reCiterArticle.getClusterInfo() 
										+ " [Doctoral Degree Difference (>=1998) =" + difference + "]");
								reCiterArticle.setDoctoralYearDiscrepancyScore(1);
								reCiterArticle.setPublishedPriorAcademicDegreeDoctoral("Target Author doctoral graduation year: " +
										identity.getDegreeYear().getDoctoralYear() + " publication date: " + year + ". Diff="+ difference);
								//return 1;
								if(reCiterArticle.getEducationYearEvidence() != null) {
									reCiterArticle.getEducationYearEvidence().setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoral(difference);
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoralScore(ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearDoctoralScore());
								} else {
									educationYearEvidence = new EducationYearEvidence();
									educationYearEvidence.setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									educationYearEvidence.setArticleYear(year);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoral(difference);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoralScore(ReCiterArticleScorer.strategyParameters.getDiscrepancyDegreeYearDoctoralScore());
									reCiterArticle.setEducationYearEvidence(educationYearEvidence);
								}
							} else {
								if(reCiterArticle.getEducationYearEvidence() != null) {
									reCiterArticle.getEducationYearEvidence().setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoral(difference);
									reCiterArticle.getEducationYearEvidence().setDiscrepancyDegreeYearDoctoralScore(0);
								} else {
									educationYearEvidence = new EducationYearEvidence();
									educationYearEvidence.setIdentityDoctoralYear(identity.getDegreeYear().getDoctoralYear());
									educationYearEvidence.setArticleYear(year);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoral(difference);
									educationYearEvidence.setDiscrepancyDegreeYearDoctoralScore(0);
									reCiterArticle.setEducationYearEvidence(educationYearEvidence);
								}
							}
						}
					}
					if(reCiterArticle.getEducationYearEvidence() != null) {
						log.info("Pmid: " + reCiterArticle.getArticleId() + " " + reCiterArticle.getEducationYearEvidence().toString());
					}
				}
			}
			
		}
		return 0;
	}

	public DegreeType getDegreeType() {
		return degreeType;
	}
}
