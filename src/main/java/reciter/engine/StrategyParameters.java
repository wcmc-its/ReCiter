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

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class StrategyParameters {
	
	@Value("${reciter.minimumStorageThreshold}")
	private double minimumStorageThreshold;
	
	@Value("${use.scopus.articles}")
	private boolean isUseScopusArticles;
	
    @Value("${strategy.email}")
    private boolean isEmail;

    @Value("${strategy.department}")
    private boolean isDepartment;
    
    @Value("${strategy.journalcategory}")
    private boolean isJournalCategory;

    @Value("${strategy.known.relationship}")
    private boolean isKnownRelationship;

    @Value("${strategy.affiliation}")
    private boolean isAffiliation;

    @Value("${strategy.scopus.common.affiliation}")
    private boolean isScopusCommonAffiliation;

    @Value("${strategy.coauthor}")
    private boolean isCoauthor;

    @Value("${strategy.journal}")
    private boolean isJournal;

    @Value("${strategy.education}")
    private boolean isEducation;

    @Value("${strategy.grant}")
    private boolean isGrant;

    @Value("${strategy.citation}")
    private boolean isCitation;

    @Value("${strategy.cocitation}")
    private boolean isCoCitation;

    @Value("${strategy.article.size}")
    private boolean isArticleSize;

    @Value("${strategy.persontype}")
    private boolean isPersonType;

    @Value("${strategy.averageclustering}")
    private boolean isAverageClustering;

    @Value("${strategy.bachelors.year.discrepancy}")
    private boolean isBachelorsYearDiscrepancy;

    @Value("${strategy.doctoral.year.discrepancy}")
    private boolean isDoctoralYearDiscrepancy;

    @Value("${strategy.cluster.size}")
    private boolean isClusterSize;

    @Value("${strategy.mesh.major}")
    private boolean isMeshMajor;

    private boolean useGoldStandardEvidence;

    @Value("${cluster.similarity.threshold.score}")
    private double clusterSimilarityThresholdScore;

    @Value("${clusteringGrants-threshold}")
    private double clusteringGrantsThreshold;

    @Value("${nameMatchFirstType.full-exact}")
    private double nameMatchFirstTypeFullExactScore;

    @Value("${nameMatchFirstType.inferredInitials-exact}")
    private double nameMatchFirstTypeInferredInitialsExactScore;

    @Value("${nameMatchFirstType.full-fuzzy}")
    private double nameMatchFirstTypeFullFuzzyScore;

    @Value("${nameMatchFirstType.noMatch}")
    private double nameMatchFirstTypeNoMatchScore;

    @Value("${nameMatchFirstType.full-conflictingAllButInitials}")
    private double nameMatchFirstTypeFullConflictingAllButInitialsScore;

    @Value("${nameMatchFirstType.full-conflictingEntirely}")
    private double nameMatchFirstTypeFullConflictingEntirelyScore;

    @Value("${nameMatchFirstType.nullTargetAuthor-MatchNotAttempted}")
    private double nameMatchFirstTypeNullTargetAuthorMatchNotAttemptedScore;

    @Value("${nameMatchLastType.full-exact}")
    private double nameMatchLastTypeFullExactScore;

    @Value("${nameMatchLastType.full-fuzzy}")
    private double nameMatchLastTypeFullFuzzyScore;

    @Value("${nameMatchLastType.full-conflictingEntirely}")
    private double nameMatchLastTypeFullConflictingEntirelyScore;

    @Value("${nameMatchLastType.nullTargetAuthor-MatchNotAttempted}")
    private double nameMatchLastTypeNullTargetAuthorMatchNotAttemptedScore;

    @Value("${nameMatchMiddleType.inferredInitials-exact}")
    private double nameMatchMiddleTypeInferredInitialsExactScore;

    @Value("${nameMatchMiddleType.full-exact}")
    private double nameMatchMiddleTypeFullExactScore;

    @Value("${nameMatchMiddleType.exact-singleInitial}")
    private double nameMatchMiddleTypeExactSingleInitialScore;

    @Value("${nameMatchMiddleType.noMatch}")
    private double nameMatchMiddleTypeNoMatchScore;

    @Value("${nameMatchMiddleType.full-fuzzy}")
    private double nameMatchMiddleTypeFullFuzzyScore;

    @Value("${nameMatchMiddleType.full-conflictingEntirely}")
    private double nameMatchMiddleTypeFullConflictingEntirelyScore;

    @Value("${nameMatchMiddleType.nullTargetAuthor-MatchNotAttempted}")
    private double nameMatchMiddleTypeNullTargetAuthorMatchNotAttemptedScore;

    @Value("${nameMatchMiddleType.identityNull-MatchNotAttempted}")
    private double nameMatchMiddleTypeIdentityNullMatchNotAttemptedScore;

    @Value("${nameMatchModifier.incorrectOrder}")
    private double nameMatchModifierIncorrectOrderScore;

    @Value("${nameMatchModifier.articleSubstringOfIdentity-lastName}")
    private double nameMatchModifierArticleSubstringOfIdentityLastnameScore;

    @Value("${nameMatchModifier.articleSubstringOfIdentity-firstMiddleName}")
    private double nameMatchModifierArticleSubstringOfIdentityFirstMiddlenameScore;

    @Value("${nameMatchModifier.identitySubstringOfArticle-lastName}")
    private double nameMatchModifierIdentitySubstringOfArticleLastnameScore;

    @Value("${nameMatchModifier.identitySubstringOfArticle-firstName}")
    private double nameMatchModifierIdentitySubstringOfArticleFirstnameScore;

    @Value("${nameMatchModifier.identitySubstringOfArticle-middleName}")
    private double nameMatchModifierIdentitySubstringOfArticleMiddlenameScore;

    @Value("${nameMatchModifier.identitySubstringOfArticle-firstMiddleName}")
    private double nameMatchModifierIdentitySubstringOfArticleFirstMiddlenameScore;

    @Value("${nameMatchModifier.combinedMiddleNameLastName}")
    private double nameMatchModifierCombinedMiddleNameLastNameScore;

    @Value("${strategy.email.emailMatchScore}")
    private double emailMatchScore;

    @Value("${strategy.email.default.suffixes}")
    private String defaultSuffixes;

    @Value("${strategy.grant.grantMatchScore}")
    private double grantMatchScore;

    @Value("${strategy.knownrelationships.relationshipMatchingScore}")
    private double relationshipMatchingScore;

    @Value("${strategy.knownrelationships.relationshipVerboseMatchModifier}")
    private double relationshipVerboseMatchModifier;

    @Value("${strategy.knownrelationships.relationshipMatchModifier-Mentor}")
    private double relationshipMatchModifierMentor;

    @Value("${strategy.knownrelationships.relationshipMatchModifier-Mentor-SeniorAuthor}")
    private double relationshipMatchModifierMentorSeniorAuthor;
    
    @Value("${strategy.knownrelationships.relationshipMatchModifier-Manager}")
    private double relationshipMatchModifierManager;

    @Value("${strategy.knownrelationships.relationshipMatchModifier-Manager-SeniorAuthor}")
    private double relationshipMatchModifierManagerSeniorAuthor;

    @Value("${strategy.discrepancyDegreeYear-BachelorThreshold}")
    private double discrepancyDegreeYearBachelorThreshold;

    @Value("${strategy.discrepancyDegreeYear-BachelorScore}")
    private double discrepancyDegreeYearBachelorScore;

    @Value("${strategy.discrepancyDegreeYear-YearWhichPhDStudentsStartedToAuthorMorePapers}")
    private int discrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers;

    @Value("${strategy.discrepancyDegreeYear-DoctoralThreshold1}")
    private double discrepancyDegreeYearDoctoralThreshold1;

    @Value("${strategy.discrepancyDegreeYear-DoctoralThreshold2}")
    private double discrepancyDegreeYearDoctoralThreshold2;

    @Value("${strategy.discrepancyDegreeYear-DoctoralScore}")
    private double discrepancyDegreeYearDoctoralScore;

    @Value("${strategy.orgUnitScoringStrategy.organizationalUnitDepartmentMatchingScore}")
    private double organizationalUnitDepartmentMatchingScore;

    @Value("${strategy.orgUnitScoringStrategy.organizationalUnitModifier}")
    private String organizationalUnitModifier;

    @Value("${strategy.orgUnitScoringStrategy.organizationalUnitModifierScore}")
    private double organizationalUnitModifierScore;

    @Value("${strategy.orgUnitScoringStrategy.organizationalUnitProgramMatchingScore}")
    private double organizationalUnitProgramMatchingScore;
    
    @Value("${strategy.orgUnitScoringStrategy.organizationalUnitSynonym}")
    private String organizationalUnitSynonym;

    @Value("${strategy.articleCountScoringStrategy.articleCountThresholdScore}")
    private double articleCountThresholdScore;

    @Value("${strategy.articleCountScoringStrategy.articleCountWeight}")
    private double articleCountWeight;

    @Value("${strategy.personTypeScoringStrategy.personTypeScore-academic-faculty-weillfulltime}")
    private double personTypeScoreAcademicFacultyWeillfulltime;

    @Value("${strategy.personTypeScoringStrategy.personTypeScore-student-md-new-york}")
    private double personTypeScoreStudentMdNewyork;

    @Value("${strategy.acceptedRejectedScoringStrategy.feedbackScore-accepted}")
    private double acceptedArticleScore;

    @Value("${strategy.acceptedRejectedScoringStrategy.feedbackScore-rejected}")
    private double rejectedArticleScore;

    @Value("${strategy.acceptedRejectedScoringStrategy.feedbackScore-null}")
    private double feedbackScoreNullScore;

    @Value("${strategy.averageClusteringScoringStrategy.clusterScore-Factor}")
    private double clusterScoreFactor;
    
    @Value("${strategy.averageClusteringScoringStrategy.clusterReliabilityScoreFactor}")
    private double clusterReliabilityScoreFactor;

    @Value("${standardizedScoreMapping}")
    private String standardizedScoreMapping;

    @Value("${totalArticleScore-standardized-default}")
    private double totalArticleScoreStandardizedDefault;

    @Value("${strategy.authorAffiliationScoringStrategy.targetAuthor-institutionalAffiliation-matchType-positiveMatch-individual-score}")
    private double targetAuthorInstAfflMatchTypePositiveIndividualScore;

    @Value("${strategy.authorAffiliationScoringStrategy.targetAuthor-institutionalAffiliation-matchType-positiveMatch-institution-score}")
    private double targetAuthorInstAfflMatchTypePositiveInstitutionScore;

    @Value("${strategy.authorAffiliationScoringStrategy.targetAuthor-institutionalAffiliation-matchType-null-score}")
    private double targetAuthorInstAfflMatchTypeNullScore;

    @Value("${strategy.authorAffiliationScoringStrategy.targetAuthor-institutionalAffiliation-matchType-noMatch-score}")
    private double targetAuthorInstAfflMatchTypeNoMatchScore;

    @Value("${strategy.authorAffiliationScoringStrategy.nonTargetAuthor-institutionalAffiliation-matchType-positiveMatch-individual-score}")
    private double nonTargetAuthorInstAfflMatchTypePositiveIndividualScore;

    @Value("${strategy.authorAffiliationScoringStrategy.nonTargetAuthor-institutionalAffiliation-matchType-positiveMatch-institution-score}")
    private double nonTargetAuthorInstAfflMatchTypePositiveInstitutionScore;

    @Value("${strategy.authorAffiliationScoringStrategy.nonTargetAuthor-institutionalAffiliation-matchType-null-score}")
    private double nonTargetAuthorInstAfflMatchTypeNullScore;

    @Value("${strategy.authorAffiliationScoringStrategy.nonTargetAuthor-institutionalAffiliation-matchType-noMatch-score}")
    private double nonTargetAuthorInstAfflMatchTypeNoMatchScore;

    @Value("${strategy.authorAffiliationScoringStrategy.nonTargetAuthor-institutionalAffiliation-weight}")
    private double nonTargetAuthorInstAfflMatchTypeWeight;

    @Value("${strategy.authorAffiliationScoringStrategy.nonTargetAuthor-institutionalAffiliation-maxScore}")
    private double nonTargetAuthorInstAfflMatchTypeMaxScore;

    @Value("${strategy.authorAffiliationScoringStrategy.homeInstitution-keywords}")
    private String instAfflHomeInstKeywords;

    @Value("${strategy.authorAffiliationScoringStrategy.homeInstitution-label}")
    private String instAfflInstLabel;

    @Value("${strategy.authorAffiliationScoringStrategy.homeInstitution-scopusInstitutionIDs}")
    private String instAfflHomeInstScopusInstIDs;

    @Value("${strategy.authorAffiliationScoringStrategy.collaboratingInstitutions-scopusInstitutionIDs}")
    private String instAfflCollaboratingInstScopusInstIDs;

    @Value("${strategy.authorAffiliationScoringStrategy.collaboratingInstitutions-keywords}")
    private String instAfflCollaboratingInstKeywords;

    @Value("${strategy.authorAffiliationScoringStrategy.institutionStopwords}")
    private String instAfflInstitutionStopwords;
    
    @Value("${searchStrategy-leninent-threshold}")
    private double searchStrategyStrictThreshold;
    
    @Value("${searchStrategy-strict-threshold}")
    private double searchStrategyLeninentThreshold;
    
    @Value("${strategy.journalCategoryScore.journalSubfieldScore}")
    private double journalSubfieldScore;
    
    @Value("${strategy.journalCategoryScore.journalSubfieldFactorScore}")
    private double journalSubfieldFactorScore;
}
