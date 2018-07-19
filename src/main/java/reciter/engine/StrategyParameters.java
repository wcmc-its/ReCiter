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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StrategyParameters {

	@Value("${strategy.email}")
	private boolean isEmail;
	
	@Value("${strategy.department}")
	private boolean isDepartment;
	
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
	
	@Value("${strategy.citizenship}")
	private boolean isCitizenship;
	
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
	
	@Value("${strategy.acceptedrejected}")
	private boolean isAcceptedRejected;
	
	@Value("${strategy.averageclustering}")
	private boolean isAverageClustering;
	
	@Value("${strategy.bachelors.year.discrepancy}")
	private boolean isBachelorsYearDiscrepancy;
	
	@Value("${strategy.doctoral.year.discrepancy}")
	private boolean isDoctoralYearDiscrepancy;
	
	@Value("${strategy.remove.by.name}")
	private boolean isRemoveByName;
	
	@Value("${strategy.cluster.size}")
	private boolean isClusterSize;
	
	@Value("${strategy.mesh.major}")
	private boolean isMeshMajor;
	
	@Value("${use.gold.standard.evidence}")
	private boolean useGoldStandardEvidence;
	
	@Value("${use.rejected.evidence}")
	private boolean useRejectedEvidence;
	
	@Value("${cluster.similarity.threshold.score}")
	private double clusterSimilarityThresholdScore;
	
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
	
	@Value("${standardizedScoreMapping}")
	private String standardizedScoreMapping;
	
	@Value("${totalArticleScore-standardized-default}")
	private double totalArticleScoreStandardizedDefault;
	

	public boolean isEmail() {
		return isEmail;
	}

	public void setEmail(boolean isEmail) {
		this.isEmail = isEmail;
	}

	public boolean isDepartment() {
		return isDepartment;
	}

	public void setDepartment(boolean isDepartment) {
		this.isDepartment = isDepartment;
	}

	public boolean isKnownRelationship() {
		return isKnownRelationship;
	}

	public void setKnownRelationship(boolean isKnownRelationship) {
		this.isKnownRelationship = isKnownRelationship;
	}

	public boolean isAffiliation() {
		return isAffiliation;
	}

	public void setAffiliation(boolean isAffiliation) {
		this.isAffiliation = isAffiliation;
	}

	public boolean isScopusCommonAffiliation() {
		return isScopusCommonAffiliation;
	}

	public void setScopusCommonAffiliation(boolean isScopusCommonAffiliation) {
		this.isScopusCommonAffiliation = isScopusCommonAffiliation;
	}

	public boolean isCoauthor() {
		return isCoauthor;
	}

	public void setCoauthor(boolean isCoauthor) {
		this.isCoauthor = isCoauthor;
	}

	public boolean isJournal() {
		return isJournal;
	}

	public void setJournal(boolean isJournal) {
		this.isJournal = isJournal;
	}

	public boolean isCitizenship() {
		return isCitizenship;
	}

	public void setCitizenship(boolean isCitizenship) {
		this.isCitizenship = isCitizenship;
	}

	public boolean isEducation() {
		return isEducation;
	}

	public void setEducation(boolean isEducation) {
		this.isEducation = isEducation;
	}

	public boolean isGrant() {
		return isGrant;
	}

	public void setGrant(boolean isGrant) {
		this.isGrant = isGrant;
	}

	public boolean isCitation() {
		return isCitation;
	}

	public void setCitation(boolean isCitation) {
		this.isCitation = isCitation;
	}

	public boolean isCoCitation() {
		return isCoCitation;
	}

	public void setCoCitation(boolean isCoCitation) {
		this.isCoCitation = isCoCitation;
	}

	public boolean isArticleSize() {
		return isArticleSize;
	}

	public void setArticleSize(boolean isArticleSize) {
		this.isArticleSize = isArticleSize;
	}

	public boolean isBachelorsYearDiscrepancy() {
		return isBachelorsYearDiscrepancy;
	}

	public void setBachelorsYearDiscrepancy(boolean isBachelorsYearDiscrepancy) {
		this.isBachelorsYearDiscrepancy = isBachelorsYearDiscrepancy;
	}

	public boolean isDoctoralYearDiscrepancy() {
		return isDoctoralYearDiscrepancy;
	}

	public void setDoctoralYearDiscrepancy(boolean isDoctoralYearDiscrepancy) {
		this.isDoctoralYearDiscrepancy = isDoctoralYearDiscrepancy;
	}

	public boolean isRemoveByName() {
		return isRemoveByName;
	}

	public void setRemoveByName(boolean isRemoveByName) {
		this.isRemoveByName = isRemoveByName;
	}

	public boolean isClusterSize() {
		return isClusterSize;
	}

	public void setClusterSize(boolean isClusterSize) {
		this.isClusterSize = isClusterSize;
	}

	public boolean isMeshMajor() {
		return isMeshMajor;
	}

	public void setMeshMajor(boolean isMeshMajor) {
		this.isMeshMajor = isMeshMajor;
	}

	public boolean isUseGoldStandardEvidence() {
		return useGoldStandardEvidence;
	}

	public void setUseGoldStandardEvidence(boolean useGoldStandardEvidence) {
		this.useGoldStandardEvidence = useGoldStandardEvidence;
	}
	
	public boolean isUseRejectedEvidence() {
		return useRejectedEvidence;
	}

	public void setUseRejectedEvidence(boolean useRejectedEvidence) {
		this.useRejectedEvidence = useRejectedEvidence;
	}

	public double getClusterSimilarityThresholdScore() {
		return clusterSimilarityThresholdScore;
	}

	public void setClusterSimilarityThresholdScore(double clusterSimilarityThresholdScore) {
		this.clusterSimilarityThresholdScore = clusterSimilarityThresholdScore;
	}

	public double getNameMatchFirstTypeFullExactScore() {
		return nameMatchFirstTypeFullExactScore;
	}

	public void setNameMatchFirstTypeFullExactScore(double nameMatchFirstTypeFullExactScore) {
		this.nameMatchFirstTypeFullExactScore = nameMatchFirstTypeFullExactScore;
	}

	public double getNameMatchFirstTypeInferredInitialsExactScore() {
		return nameMatchFirstTypeInferredInitialsExactScore;
	}

	public void setNameMatchFirstTypeInferredInitialsExactScore(double nameMatchFirstTypeInferredInitialsExactScore) {
		this.nameMatchFirstTypeInferredInitialsExactScore = nameMatchFirstTypeInferredInitialsExactScore;
	}

	public double getNameMatchFirstTypeFullFuzzyScore() {
		return nameMatchFirstTypeFullFuzzyScore;
	}

	public void setNameMatchFirstTypeFullFuzzyScore(double nameMatchFirstTypeFullFuzzyScore) {
		this.nameMatchFirstTypeFullFuzzyScore = nameMatchFirstTypeFullFuzzyScore;
	}

	public double getNameMatchFirstTypeNoMatchScore() {
		return nameMatchFirstTypeNoMatchScore;
	}

	public void setNameMatchFirstTypeNoMatchScore(double nameMatchFirstTypeNoMatchScore) {
		this.nameMatchFirstTypeNoMatchScore = nameMatchFirstTypeNoMatchScore;
	}

	public double getNameMatchFirstTypeFullConflictingAllButInitialsScore() {
		return nameMatchFirstTypeFullConflictingAllButInitialsScore;
	}

	public void setNameMatchFirstTypeFullConflictingAllButInitialsScore(
			double nameMatchFirstTypeFullConflictingAllButInitialsScore) {
		this.nameMatchFirstTypeFullConflictingAllButInitialsScore = nameMatchFirstTypeFullConflictingAllButInitialsScore;
	}

	public double getNameMatchFirstTypeFullConflictingEntirelyScore() {
		return nameMatchFirstTypeFullConflictingEntirelyScore;
	}

	public void setNameMatchFirstTypeFullConflictingEntirelyScore(double nameMatchFirstTypeFullConflictingEntirelyScore) {
		this.nameMatchFirstTypeFullConflictingEntirelyScore = nameMatchFirstTypeFullConflictingEntirelyScore;
	}

	public double getNameMatchFirstTypeNullTargetAuthorMatchNotAttemptedScore() {
		return nameMatchFirstTypeNullTargetAuthorMatchNotAttemptedScore;
	}

	public void setNameMatchFirstTypeNullTargetAuthorMatchNotAttemptedScore(
			double nameMatchFirstTypeNullTargetAuthorMatchNotAttemptedScore) {
		this.nameMatchFirstTypeNullTargetAuthorMatchNotAttemptedScore = nameMatchFirstTypeNullTargetAuthorMatchNotAttemptedScore;
	}

	public double getNameMatchLastTypeFullExactScore() {
		return nameMatchLastTypeFullExactScore;
	}

	public void setNameMatchLastTypeFullExactScore(double nameMatchLastTypeFullExactScore) {
		this.nameMatchLastTypeFullExactScore = nameMatchLastTypeFullExactScore;
	}

	public double getNameMatchLastTypeFullFuzzyScore() {
		return nameMatchLastTypeFullFuzzyScore;
	}

	public void setNameMatchLastTypeFullFuzzyScore(double nameMatchLastTypeFullFuzzyScore) {
		this.nameMatchLastTypeFullFuzzyScore = nameMatchLastTypeFullFuzzyScore;
	}

	public double getNameMatchLastTypeFullConflictingEntirelyScore() {
		return nameMatchLastTypeFullConflictingEntirelyScore;
	}

	public void setNameMatchLastTypeFullConflictingEntirelyScore(double nameMatchLastTypeFullConflictingEntirelyScore) {
		this.nameMatchLastTypeFullConflictingEntirelyScore = nameMatchLastTypeFullConflictingEntirelyScore;
	}

	public double getNameMatchLastTypeNullTargetAuthorMatchNotAttemptedScore() {
		return nameMatchLastTypeNullTargetAuthorMatchNotAttemptedScore;
	}

	public void setNameMatchLastTypeNullTargetAuthorMatchNotAttemptedScore(
			double nameMatchLastTypeNullTargetAuthorMatchNotAttemptedScore) {
		this.nameMatchLastTypeNullTargetAuthorMatchNotAttemptedScore = nameMatchLastTypeNullTargetAuthorMatchNotAttemptedScore;
	}

	public double getNameMatchMiddleTypeInferredInitialsExactScore() {
		return nameMatchMiddleTypeInferredInitialsExactScore;
	}

	public void setNameMatchMiddleTypeInferredInitialsExactScore(double nameMatchMiddleTypeInferredInitialsExactScore) {
		this.nameMatchMiddleTypeInferredInitialsExactScore = nameMatchMiddleTypeInferredInitialsExactScore;
	}

	public double getNameMatchMiddleTypeFullExactScore() {
		return nameMatchMiddleTypeFullExactScore;
	}

	public void setNameMatchMiddleTypeFullExactScore(double nameMatchMiddleTypeFullExactScore) {
		this.nameMatchMiddleTypeFullExactScore = nameMatchMiddleTypeFullExactScore;
	}

	public double getNameMatchMiddleTypeNoMatchScore() {
		return nameMatchMiddleTypeNoMatchScore;
	}

	public void setNameMatchMiddleTypeNoMatchScore(double nameMatchMiddleTypeNoMatchScore) {
		this.nameMatchMiddleTypeNoMatchScore = nameMatchMiddleTypeNoMatchScore;
	}

	public double getNameMatchMiddleTypeFullFuzzyScore() {
		return nameMatchMiddleTypeFullFuzzyScore;
	}

	public void setNameMatchMiddleTypeFullFuzzyScore(double nameMatchMiddleTypeFullFuzzyScore) {
		this.nameMatchMiddleTypeFullFuzzyScore = nameMatchMiddleTypeFullFuzzyScore;
	}

	public double getNameMatchMiddleTypeFullConflictingEntirelyScore() {
		return nameMatchMiddleTypeFullConflictingEntirelyScore;
	}

	public void setNameMatchMiddleTypeFullConflictingEntirelyScore(double nameMatchMiddleTypeFullConflictingEntirelyScore) {
		this.nameMatchMiddleTypeFullConflictingEntirelyScore = nameMatchMiddleTypeFullConflictingEntirelyScore;
	}

	public double getNameMatchMiddleTypeNullTargetAuthorMatchNotAttemptedScore() {
		return nameMatchMiddleTypeNullTargetAuthorMatchNotAttemptedScore;
	}

	public void setNameMatchMiddleTypeNullTargetAuthorMatchNotAttemptedScore(
			double nameMatchMiddleTypeNullTargetAuthorMatchNotAttemptedScore) {
		this.nameMatchMiddleTypeNullTargetAuthorMatchNotAttemptedScore = nameMatchMiddleTypeNullTargetAuthorMatchNotAttemptedScore;
	}

	public double getNameMatchMiddleTypeIdentityNullMatchNotAttemptedScore() {
		return nameMatchMiddleTypeIdentityNullMatchNotAttemptedScore;
	}

	public void setNameMatchMiddleTypeIdentityNullMatchNotAttemptedScore(
			double nameMatchMiddleTypeIdentityNullMatchNotAttemptedScore) {
		this.nameMatchMiddleTypeIdentityNullMatchNotAttemptedScore = nameMatchMiddleTypeIdentityNullMatchNotAttemptedScore;
	}

	public double getNameMatchModifierIncorrectOrderScore() {
		return nameMatchModifierIncorrectOrderScore;
	}

	public void setNameMatchModifierIncorrectOrderScore(double nameMatchModifierIncorrectOrderScore) {
		this.nameMatchModifierIncorrectOrderScore = nameMatchModifierIncorrectOrderScore;
	}

	public double getNameMatchModifierArticleSubstringOfIdentityLastnameScore() {
		return nameMatchModifierArticleSubstringOfIdentityLastnameScore;
	}

	public void setNameMatchModifierArticleSubstringOfIdentityLastnameScore(
			double nameMatchModifierArticleSubstringOfIdentityLastnameScore) {
		this.nameMatchModifierArticleSubstringOfIdentityLastnameScore = nameMatchModifierArticleSubstringOfIdentityLastnameScore;
	}

	public double getNameMatchModifierArticleSubstringOfIdentityFirstMiddlenameScore() {
		return nameMatchModifierArticleSubstringOfIdentityFirstMiddlenameScore;
	}

	public void setNameMatchModifierArticleSubstringOfIdentityFirstMiddlenameScore(
			double nameMatchModifierArticleSubstringOfIdentityFirstMiddlenameScore) {
		this.nameMatchModifierArticleSubstringOfIdentityFirstMiddlenameScore = nameMatchModifierArticleSubstringOfIdentityFirstMiddlenameScore;
	}

	public double getNameMatchModifierIdentitySubstringOfArticleLastnameScore() {
		return nameMatchModifierIdentitySubstringOfArticleLastnameScore;
	}

	public void setNameMatchModifierIdentitySubstringOfArticleLastnameScore(
			double nameMatchModifierIdentitySubstringOfArticleLastnameScore) {
		this.nameMatchModifierIdentitySubstringOfArticleLastnameScore = nameMatchModifierIdentitySubstringOfArticleLastnameScore;
	}

	public double getNameMatchModifierIdentitySubstringOfArticleFirstnameScore() {
		return nameMatchModifierIdentitySubstringOfArticleFirstnameScore;
	}

	public void setNameMatchModifierIdentitySubstringOfArticleFirstnameScore(
			double nameMatchModifierIdentitySubstringOfArticleFirstnameScore) {
		this.nameMatchModifierIdentitySubstringOfArticleFirstnameScore = nameMatchModifierIdentitySubstringOfArticleFirstnameScore;
	}

	public double getNameMatchModifierIdentitySubstringOfArticleMiddlenameScore() {
		return nameMatchModifierIdentitySubstringOfArticleMiddlenameScore;
	}

	public void setNameMatchModifierIdentitySubstringOfArticleMiddlenameScore(
			double nameMatchModifierIdentitySubstringOfArticleMiddlenameScore) {
		this.nameMatchModifierIdentitySubstringOfArticleMiddlenameScore = nameMatchModifierIdentitySubstringOfArticleMiddlenameScore;
	}

	public double getNameMatchModifierIdentitySubstringOfArticleFirstMiddlenameScore() {
		return nameMatchModifierIdentitySubstringOfArticleFirstMiddlenameScore;
	}

	public void setNameMatchModifierIdentitySubstringOfArticleFirstMiddlenameScore(
			double nameMatchModifierIdentitySubstringOfArticleFirstMiddlenameScore) {
		this.nameMatchModifierIdentitySubstringOfArticleFirstMiddlenameScore = nameMatchModifierIdentitySubstringOfArticleFirstMiddlenameScore;
	}

	public double getNameMatchMiddleTypeExactSingleInitialScore() {
		return nameMatchMiddleTypeExactSingleInitialScore;
	}

	public void setNameMatchMiddleTypeExactSingleInitialScore(double nameMatchMiddleTypeExactSingleInitialScore) {
		this.nameMatchMiddleTypeExactSingleInitialScore = nameMatchMiddleTypeExactSingleInitialScore;
	}

	public double getNameMatchModifierCombinedMiddleNameLastNameScore() {
		return nameMatchModifierCombinedMiddleNameLastNameScore;
	}

	public void setNameMatchModifierCombinedMiddleNameLastNameScore(
			double nameMatchModifierCombinedMiddleNameLastNameScore) {
		this.nameMatchModifierCombinedMiddleNameLastNameScore = nameMatchModifierCombinedMiddleNameLastNameScore;
	}

	public double getEmailMatchScore() {
		return emailMatchScore;
	}

	public void setEmailMatchScore(double emailMatchScore) {
		this.emailMatchScore = emailMatchScore;
	}

	public String getDefaultSuffixes() {
		return defaultSuffixes;
	}

	public void setDefaultSuffixes(String defaultSuffixes) {
		this.defaultSuffixes = defaultSuffixes;
	}

	public double getGrantMatchScore() {
		return grantMatchScore;
	}

	public void setGrantMatchScore(double grantMatchScore) {
		this.grantMatchScore = grantMatchScore;
	}

	public double getRelationshipMatchingScore() {
		return relationshipMatchingScore;
	}

	public void setRelationshipMatchingScore(double relationshipMatchingScore) {
		this.relationshipMatchingScore = relationshipMatchingScore;
	}

	public double getRelationshipVerboseMatchModifier() {
		return relationshipVerboseMatchModifier;
	}

	public void setRelationshipVerboseMatchModifier(double relationshipVerboseMatchModifier) {
		this.relationshipVerboseMatchModifier = relationshipVerboseMatchModifier;
	}

	public double getRelationshipMatchModifierMentor() {
		return relationshipMatchModifierMentor;
	}

	public void setRelationshipMatchModifierMentor(double relationshipMatchModifierMentor) {
		this.relationshipMatchModifierMentor = relationshipMatchModifierMentor;
	}

	public double getRelationshipMatchModifierMentorSeniorAuthor() {
		return relationshipMatchModifierMentorSeniorAuthor;
	}

	public void setRelationshipMatchModifierMentorSeniorAuthor(double relationshipMatchModifierMentorSeniorAuthor) {
		this.relationshipMatchModifierMentorSeniorAuthor = relationshipMatchModifierMentorSeniorAuthor;
	}

	public double getDiscrepancyDegreeYearBachelorThreshold() {
		return discrepancyDegreeYearBachelorThreshold;
	}

	public void setDiscrepancyDegreeYearBachelorThreshold(double discrepancyDegreeYearBachelorThreshold) {
		this.discrepancyDegreeYearBachelorThreshold = discrepancyDegreeYearBachelorThreshold;
	}

	public double getDiscrepancyDegreeYearBachelorScore() {
		return discrepancyDegreeYearBachelorScore;
	}

	public void setDiscrepancyDegreeYearBachelorScore(double discrepancyDegreeYearBachelorScore) {
		this.discrepancyDegreeYearBachelorScore = discrepancyDegreeYearBachelorScore;
	}

	public int getDiscrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers() {
		return discrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers;
	}

	public void setDiscrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers(
			int discrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers) {
		this.discrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers = discrepancyDegreeYearYearWhichPhDStudentsStartedToAuthorMorePapers;
	}

	public double getDiscrepancyDegreeYearDoctoralThreshold1() {
		return discrepancyDegreeYearDoctoralThreshold1;
	}

	public void setDiscrepancyDegreeYearDoctoralThreshold1(double discrepancyDegreeYearDoctoralThreshold1) {
		this.discrepancyDegreeYearDoctoralThreshold1 = discrepancyDegreeYearDoctoralThreshold1;
	}

	public double getDiscrepancyDegreeYearDoctoralThreshold2() {
		return discrepancyDegreeYearDoctoralThreshold2;
	}

	public void setDiscrepancyDegreeYearDoctoralThreshold2(double discrepancyDegreeYearDoctoralThreshold2) {
		this.discrepancyDegreeYearDoctoralThreshold2 = discrepancyDegreeYearDoctoralThreshold2;
	}

	public double getDiscrepancyDegreeYearDoctoralScore() {
		return discrepancyDegreeYearDoctoralScore;
	}

	public void setDiscrepancyDegreeYearDoctoralScore(double discrepancyDegreeYearDoctoralScore) {
		this.discrepancyDegreeYearDoctoralScore = discrepancyDegreeYearDoctoralScore;
	}

	public double getOrganizationalUnitDepartmentMatchingScore() {
		return organizationalUnitDepartmentMatchingScore;
	}

	public void setOrganizationalUnitDepartmentMatchingScore(double organizationalUnitDepartmentMatchingScore) {
		this.organizationalUnitDepartmentMatchingScore = organizationalUnitDepartmentMatchingScore;
	}

	public String getOrganizationalUnitModifier() {
		return organizationalUnitModifier;
	}

	public void setOrganizationalUnitModifier(String organizationalUnitModifier) {
		this.organizationalUnitModifier = organizationalUnitModifier;
	}

	public double getOrganizationalUnitModifierScore() {
		return organizationalUnitModifierScore;
	}

	public void setOrganizationalUnitModifierScore(double organizationalUnitModifierScore) {
		this.organizationalUnitModifierScore = organizationalUnitModifierScore;
	}

	public double getOrganizationalUnitProgramMatchingScore() {
		return organizationalUnitProgramMatchingScore;
	}

	public void setOrganizationalUnitProgramMatchingScore(double organizationalUnitProgramMatchingScore) {
		this.organizationalUnitProgramMatchingScore = organizationalUnitProgramMatchingScore;
	}

	public double getArticleCountThresholdScore() {
		return articleCountThresholdScore;
	}

	public void setArticleCountThresholdScore(double articleCountThresholdScore) {
		this.articleCountThresholdScore = articleCountThresholdScore;
	}

	public double getArticleCountWeight() {
		return articleCountWeight;
	}

	public void setArticleCountWeight(double articleCountWeight) {
		this.articleCountWeight = articleCountWeight;
	}

	public double getPersonTypeScoreAcademicFacultyWeillfulltime() {
		return personTypeScoreAcademicFacultyWeillfulltime;
	}

	public void setPersonTypeScoreAcademicFacultyWeillfulltime(double personTypeScoreAcademicFacultyWeillfulltime) {
		this.personTypeScoreAcademicFacultyWeillfulltime = personTypeScoreAcademicFacultyWeillfulltime;
	}

	public double getPersonTypeScoreStudentMdNewyork() {
		return personTypeScoreStudentMdNewyork;
	}

	public void setPersonTypeScoreStudentMdNewyork(double personTypeScoreStudentMdNewyork) {
		this.personTypeScoreStudentMdNewyork = personTypeScoreStudentMdNewyork;
	}

	public boolean isPersonType() {
		return isPersonType;
	}

	public void setPersonType(boolean isPersonType) {
		this.isPersonType = isPersonType;
	}

	public boolean isAcceptedRejected() {
		return isAcceptedRejected;
	}

	public void setAcceptedRejected(boolean isAcceptedRejected) {
		this.isAcceptedRejected = isAcceptedRejected;
	}

	public double getAcceptedArticleScore() {
		return acceptedArticleScore;
	}

	public void setAcceptedArticleScore(double acceptedArticleScore) {
		this.acceptedArticleScore = acceptedArticleScore;
	}

	public double getRejectedArticleScore() {
		return rejectedArticleScore;
	}

	public void setRejectedArticleScore(double rejectedArticleScore) {
		this.rejectedArticleScore = rejectedArticleScore;
	}

	public double getFeedbackScoreNullScore() {
		return feedbackScoreNullScore;
	}

	public void setFeedbackScoreNullScore(double feedbackScoreNullScore) {
		this.feedbackScoreNullScore = feedbackScoreNullScore;
	}

	public double getClusterScoreFactor() {
		return clusterScoreFactor;
	}

	public void setClusterScoreFactor(double clusterScoreFactor) {
		this.clusterScoreFactor = clusterScoreFactor;
	}

	public boolean isAverageClustering() {
		return isAverageClustering;
	}

	public void setAverageClustering(boolean isAverageClustering) {
		this.isAverageClustering = isAverageClustering;
	}

	public String getStandardizedScoreMapping() {
		return standardizedScoreMapping;
	}

	public void setStandardizedScoreMapping(String standardizedScoreMapping) {
		this.standardizedScoreMapping = standardizedScoreMapping;
	}

	public double getTotalArticleScoreStandardizedDefault() {
		return totalArticleScoreStandardizedDefault;
	}

	public void setTotalArticleScoreStandardizedDefault(double totalArticleScoreStandardizedDefault) {
		this.totalArticleScoreStandardizedDefault = totalArticleScoreStandardizedDefault;
	}
	
	
}
