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
package reciter.model.article;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;
import reciter.engine.analysis.evidence.AffiliationEvidence;
import reciter.engine.analysis.evidence.ClusteringEvidence;
import reciter.engine.analysis.evidence.EducationYearEvidence;
import reciter.engine.analysis.evidence.GrantEvidence;
import reciter.engine.analysis.evidence.RelationshipEvidence;
import reciter.model.article.completeness.ArticleCompleteness;
import reciter.model.article.completeness.ReCiterCompleteness;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.identity.KnownRelationship;
import reciter.model.scopus.ScopusArticle;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReCiterArticle implements Comparable<ReCiterArticle> {

    /**
     * Article id: (usually PMID).
     */
    private final long articleId;

    /**
     * Article title.
     */
    private String articleTitle;

    /**
     * Co-authors of this article.
     */
    private ReCiterArticleAuthors articleCoAuthors;

    /**
     * Journal that this article belongs to.
     */
    private ReCiterJournal journal;

    /**
     * Keywords associated with this article.
     */
    private ReCiterArticleKeywords articleKeywords;

    /**
     * How "complete" this article is. (Please refer to the ReCiter paper).
     */
    private double completenessScore;

    /**
     * Complete score strategy.
     */
    @Transient
    private ArticleCompleteness articleCompleteness;

    /**
     * Scopus Article.
     */
    @Transient
    private ScopusArticle scopusArticle;

    /**
     * Grant List.

     */
    private List<ReCiterArticleGrant> grantList;
    private List<ReCiterArticleGrant> matchingGrantList = new ArrayList<>(0);

    private boolean shouldRemoveValue;
    private boolean foundAuthorWithSameFirstNameValue;
    private boolean foundMatchingAuthorValue;

    /**
     * Text containing how it's clustered.
     */
    @Transient
    private String clusterInfo = "";

    private double emailStrategyScore;
    private List<String> matchingEmails = new ArrayList<>(0);
    private double departmentStrategyScore;
    private String matchingDepartment;

    private double knownCoinvestigatorScore;
    private List<KnownRelationship> knownRelationship = new ArrayList<>(0);
    private double affiliationScore;
    private double scopusStrategyScore;
    private double coauthorStrategyScore;
    private double journalStrategyScore;
    private double citizenshipStrategyScore;

    private double nameStrategyScore;
    private double boardCertificationStrategyScore;
    private double internshipAndResidenceStrategyScore;
    private double bachelorsYearDiscrepancyScore;
    private double doctoralYearDiscrepancyScore;
    private double bachelorsYearDiscrepancy;
    private String publishedPriorAcademicDegreeBachelors;
    private double doctoralYearDiscrepancy;
    private String publishedPriorAcademicDegreeDoctoral;
    private boolean isArticleTitleStartWithBracket;
    private double educationStrategyScore;
    private double meshMajorStrategyScore;
    private Set<String> overlappingMeSHMajorNegativeArticles = new HashSet<>();

    private int goldStandard;
    private Set<Long> commentsCorrectionsPmids = new HashSet<>();
    private List<ReCiterArticleMeshHeading> meshHeadings = new ArrayList<>();

    private List<ReCiterAuthor> knownRelationships = new ArrayList<>();
    private List<String> frequentInstitutionalCollaborators = new ArrayList<>();

    private List<Long> citations = new ArrayList<>();
    private AuthorName matchingName;

    private List<CoCitation> coCitation = new ArrayList<>();
    private ReCiterAuthor correctAuthor;
    private int correctAuthorRank;
    private int numAuthors;

    private StringBuffer meshMajorInfo;
    private StringBuffer citesInfo;
    private StringBuffer citedByInfo;
    private StringBuffer coCitationInfo;
    private StringBuffer journalTitleInfo;

    private Date pubDate;

    private AffiliationEvidence affiliationEvidence;
    private GrantEvidence grantEvidence;

    private List<RelationshipEvidence> relationshipEvidences;

    private String volume;
    private String issue;

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public List<RelationshipEvidence> getRelationshipEvidences() {
        return relationshipEvidences;
    }

    public void setRelationshipEvidences(List<RelationshipEvidence> relationshipEvidences) {
        this.relationshipEvidences = relationshipEvidences;
    }

    public ClusteringEvidence getClusteringEvidence() {
        return clusteringEvidence;
    }

    public void setClusteringEvidence(ClusteringEvidence clusteringEvidence) {
        this.clusteringEvidence = clusteringEvidence;
    }

    private ClusteringEvidence clusteringEvidence = new ClusteringEvidence();

    public EducationYearEvidence getEducationYearEvidence() {
        return educationYearEvidence;
    }

    public void setEducationYearEvidence(EducationYearEvidence educationYearEvidence) {
        this.educationYearEvidence = educationYearEvidence;
    }

    private EducationYearEvidence educationYearEvidence;

    public List<RelationshipEvidence> getRelationshipEvidence() {
        return relationshipEvidences;
    }

    public void setRelationshipEvidence(List<RelationshipEvidence> relationshipEvidences) {
        this.relationshipEvidences = relationshipEvidences;
    }

    public GrantEvidence getGrantEvidence() {
        return grantEvidence;
    }

    public void setGrantEvidence(GrantEvidence grantEvidence) {
        this.grantEvidence = grantEvidence;
    }


    public AffiliationEvidence getAffiliationEvidence() {
        return affiliationEvidence;
    }

    public void setAffiliationEvidence(AffiliationEvidence affiliationEvidence) {
        this.affiliationEvidence = affiliationEvidence;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public StringBuffer getMeshMajorInfo() {
        if (meshMajorInfo == null) {
            meshMajorInfo = new StringBuffer();
        }
        return meshMajorInfo;
    }

    public void setMeshMajorInfo(StringBuffer meshMajorInfo) {
        this.meshMajorInfo = meshMajorInfo;
    }

    public StringBuffer getCitesInfo() {
        if (citesInfo == null) {
            citesInfo = new StringBuffer();
        }
        return citesInfo;
    }

    public void setCitesInfo(StringBuffer citesInfo) {
        this.citesInfo = citesInfo;
    }

    public StringBuffer getCitedByInfo() {
        if (citedByInfo == null) {
            citedByInfo = new StringBuffer();
        }
        return citedByInfo;
    }

    public void setCitedByInfo(StringBuffer citedByInfo) {
        this.citedByInfo = citedByInfo;
    }

    public StringBuffer getCoCitationInfo() {
        if (coCitationInfo == null) {
            coCitationInfo = new StringBuffer();
        }
        return coCitationInfo;
    }

    public void setCoCitationInfo(StringBuffer coCitationInfo) {
        this.coCitationInfo = coCitationInfo;
    }

    public StringBuffer getJournalTitleInfo() {
        if (journalTitleInfo == null) {
            journalTitleInfo = new StringBuffer();
        }
        return journalTitleInfo;
    }

    public void setJournalTitleInfo(StringBuffer journalTitleInfo) {
        this.journalTitleInfo = journalTitleInfo;
    }

    public void setEducationStrategyScore(double educationStrategyScore) {
        this.educationStrategyScore = educationStrategyScore;
    }

    public static class CoCitation {
        private long pmid;
        private List<Long> pmids;

        public long getPmid() {
            return pmid;
        }

        public void setPmid(long pmid) {
            this.pmid = pmid;
        }

        public List<Long> getPmids() {
            return pmids;
        }

        public void setPmids(List<Long> pmids) {
            this.pmids = pmids;
        }
    }

    /**
     * Default Completeness Score Calculation: ReCiterCompleteness
     *
     * @param articleID
     */
    public ReCiterArticle(long articleId) {
        this.articleId = articleId;
        this.setArticleCompleteness(new ReCiterCompleteness());
    }

    public void setCorrectAuthor(Identity identity) {
        int rank = 0;
        String targetAuthorLastName = identity.getPrimaryName().getLastName();
        ReCiterArticleAuthors authors = getArticleCoAuthors();
        if (authors != null) {
            for (ReCiterAuthor author : authors.getAuthors()) {
                rank++;
                String lastName = author.getAuthorName().getLastName();
                if (StringUtils.equalsIgnoreCase(targetAuthorLastName, lastName)) {
                    String firstInitial = author.getAuthorName().getFirstInitial();
                    if (StringUtils.equalsIgnoreCase(firstInitial, identity.getPrimaryName().getFirstInitial())) {
                        this.correctAuthor = author;
                        break;
                    }
                }
            }
        }
        this.correctAuthorRank = rank;
        this.numAuthors = authors.getNumberOfAuthors();
    }

    @Override
    public int compareTo(ReCiterArticle otherArticle) {
        double x = this.getCompletenessScore();
        double y = otherArticle.getCompletenessScore();
        if (x > y) {
            return -1;
        } else if (x < y) {
            return 1;
        } else {
            return 0;
        }
    }

    public long getArticleId() {
        return articleId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public ReCiterArticleAuthors getArticleCoAuthors() {
        return articleCoAuthors;
    }

    public void setArticleCoAuthors(ReCiterArticleAuthors articleCoAuthors) {
        this.articleCoAuthors = articleCoAuthors;
    }

    public ReCiterJournal getJournal() {
        return journal;
    }

    public void setJournal(ReCiterJournal journal) {
        this.journal = journal;
    }

    public ReCiterArticleKeywords getArticleKeywords() {
        return articleKeywords;
    }

    public void setArticleKeywords(ReCiterArticleKeywords articleKeywords) {
        this.articleKeywords = articleKeywords;
    }

    public double getCompletenessScore() {
        return completenessScore;
    }

    public void setCompletenessScore(double completenessScore) {
        this.completenessScore = completenessScore;
    }

    public ArticleCompleteness getArticleCompleteness() {
        return articleCompleteness;
    }

    public void setArticleCompleteness(ArticleCompleteness articleCompleteness) {
        this.articleCompleteness = articleCompleteness;
    }

    public ScopusArticle getScopusArticle() {
        return scopusArticle;
    }

    public void setScopusArticle(ScopusArticle scopusArticle) {
        this.scopusArticle = scopusArticle;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (articleId ^ (articleId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReCiterArticle other = (ReCiterArticle) obj;
        if (articleId != other.articleId)
            return false;
        return true;
    }

    public String getClusterInfo() {
        return clusterInfo;
    }

    public void setClusterInfo(String clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public void appendClusterInfo(String clusterInfo) {
        this.clusterInfo += " | " + clusterInfo;
    }

    public double getEmailStrategyScore() {
        return emailStrategyScore;
    }

    public void setEmailStrategyScore(double emailStrategyScore) {
        this.emailStrategyScore = emailStrategyScore;
    }

    public double getDepartmentStrategyScore() {
        return departmentStrategyScore;
    }

    public void setDepartmentStrategyScore(double departmentStrategyScore) {
        this.departmentStrategyScore = departmentStrategyScore;
    }

    public double getKnownCoinvestigatorScore() {
        return knownCoinvestigatorScore;
    }

    public void setKnownCoinvestigatorScore(double knownCoinvestigatorScore) {
        this.knownCoinvestigatorScore = knownCoinvestigatorScore;
    }

    public double getAffiliationScore() {
        return affiliationScore;
    }

    public void setAffiliationScore(double affiliationScore) {
        this.affiliationScore = affiliationScore;
    }

    public double getScopusStrategyScore() {
        return scopusStrategyScore;
    }

    public void setScopusStrategyScore(double scopusStrategyScore) {
        this.scopusStrategyScore = scopusStrategyScore;
    }

    public double getCoauthorStrategyScore() {
        return coauthorStrategyScore;
    }

    public void setCoauthorStrategyScore(double coauthorStrategyScore) {
        this.coauthorStrategyScore = coauthorStrategyScore;
    }

    public double getJournalStrategyScore() {
        return journalStrategyScore;
    }

    public void setJournalStrategyScore(double journalStrategyScore) {
        this.journalStrategyScore = journalStrategyScore;
    }

    public double getCitizenshipStrategyScore() {
        return citizenshipStrategyScore;
    }

    public void setCitizenshipStrategyScore(double citizenshipStrategyScore) {
        this.citizenshipStrategyScore = citizenshipStrategyScore;
    }

    public double getNameStrategyScore() {
        return nameStrategyScore;
    }

    public void setNameStrategyScore(double nameStrategyScore) {
        this.nameStrategyScore = nameStrategyScore;
    }

    public double getBoardCertificationStrategyScore() {
        return boardCertificationStrategyScore;
    }

    public void setBoardCertificationStrategyScore(double boardCertificationStrategyScore) {
        this.boardCertificationStrategyScore = boardCertificationStrategyScore;
    }

    public double getInternshipAndResidenceStrategyScore() {
        return internshipAndResidenceStrategyScore;
    }

    public void setInternshipAndResidenceStrategyScore(double internshipAndResidenceStrategyScore) {
        this.internshipAndResidenceStrategyScore = internshipAndResidenceStrategyScore;
    }

    public int getGoldStandard() {
        return goldStandard;
    }

    public void setGoldStandard(int goldStandard) {
        this.goldStandard = goldStandard;
    }

    public List<ReCiterArticleGrant> getGrantList() {
        return grantList;
    }

    public void setGrantList(List<ReCiterArticleGrant> grantList) {
        this.grantList = grantList;
    }

    public double getBachelorsYearDiscrepancyScore() {
        return bachelorsYearDiscrepancyScore;
    }

    public void setBachelorsYearDiscrepancyScore(double bachelorsYearDiscrepancyScore) {
        this.bachelorsYearDiscrepancyScore = bachelorsYearDiscrepancyScore;
    }

    public double getDoctoralYearDiscrepancyScore() {
        return doctoralYearDiscrepancyScore;
    }

    public void setDoctoralYearDiscrepancyScore(double doctoralYearDiscrepancyScore) {
        this.doctoralYearDiscrepancyScore = doctoralYearDiscrepancyScore;
    }

    public boolean isArticleTitleStartWithBracket() {
        return isArticleTitleStartWithBracket;
    }

    public void setArticleTitleStartWithBracket(boolean isArticleTitleStartWithBracket) {
        this.isArticleTitleStartWithBracket = isArticleTitleStartWithBracket;
    }

    public double getEducationStrategyScore() {
        return educationStrategyScore;
    }

    public void setEducationScore(double educationStrategyScore) {
        this.educationStrategyScore = educationStrategyScore;
    }

    public Set<Long> getCommentsCorrectionsPmids() {
        return commentsCorrectionsPmids;
    }

    public void setCommentsCorrectionsPmids(Set<Long> commentsCorrectionsPmids) {
        this.commentsCorrectionsPmids = commentsCorrectionsPmids;
    }

    public List<ReCiterArticleMeshHeading> getMeshHeadings() {
        return meshHeadings;
    }

    public void setMeshHeadings(List<ReCiterArticleMeshHeading> meshHeadings) {
        this.meshHeadings = meshHeadings;
    }

    public double getBachelorsYearDiscrepancy() {
        return bachelorsYearDiscrepancy;
    }

    public void setBachelorsYearDiscrepancy(double bachelorsYearDiscrepancy) {
        this.bachelorsYearDiscrepancy = bachelorsYearDiscrepancy;
    }

    public double getDoctoralYearDiscrepancy() {
        return doctoralYearDiscrepancy;
    }

    public void setDoctoralYearDiscrepancy(double doctoralYearDiscrepancy) {
        this.doctoralYearDiscrepancy = doctoralYearDiscrepancy;
    }

    public boolean isShouldRemoveValue() {
        return shouldRemoveValue;
    }

    public void setShouldRemoveValue(boolean shouldRemoveValue) {
        this.shouldRemoveValue = shouldRemoveValue;
    }

    public boolean isFoundAuthorWithSameFirstNameValue() {
        return foundAuthorWithSameFirstNameValue;
    }

    public void setFoundAuthorWithSameFirstNameValue(boolean foundAuthorWithSameFirstNameValue) {
        this.foundAuthorWithSameFirstNameValue = foundAuthorWithSameFirstNameValue;
    }

    public boolean isFoundMatchingAuthorValue() {
        return foundMatchingAuthorValue;
    }

    public void setFoundMatchingAuthorValue(boolean foundMatchingAuthorValue) {
        this.foundMatchingAuthorValue = foundMatchingAuthorValue;
    }

    public List<ReCiterAuthor> getKnownRelationships() {
        return knownRelationships;
    }

    public void setKnownRelationships(List<ReCiterAuthor> knownRelationships) {
        this.knownRelationships = knownRelationships;
    }

    public List<String> getFrequentInstitutionalCollaborators() {
        return frequentInstitutionalCollaborators;
    }

    public void setFrequentInstitutionalCollaborators(List<String> frequentInstitutionalCollaborators) {
        this.frequentInstitutionalCollaborators = frequentInstitutionalCollaborators;
    }

    public double getMeshMajorStrategyScore() {
        return meshMajorStrategyScore;
    }

    public void setMeshMajorStrategyScore(double meshMajorStrategyScore) {
        this.meshMajorStrategyScore = meshMajorStrategyScore;
    }

    public Set<String> getOverlappingMeSHMajorNegativeArticles() {
        return overlappingMeSHMajorNegativeArticles;
    }

    public void setOverlappingMeSHMajorNegativeArticles(Set<String> overlappingMeSHMajorNegativeArticles) {
        this.overlappingMeSHMajorNegativeArticles = overlappingMeSHMajorNegativeArticles;
    }

    public AuthorName getMatchingName() {
        return matchingName;
    }

    public void setMatchingName(AuthorName matchingName) {
        this.matchingName = matchingName;
    }

    public List<Long> getCitations() {
        return citations;
    }

    public void setCitations(List<Long> citations) {
        this.citations = citations;
    }

    public List<CoCitation> getCoCitation() {
        return coCitation;
    }

    public void setCoCitation(List<CoCitation> coCitation) {
        this.coCitation = coCitation;
    }

    public ReCiterAuthor getCorrectAuthor() {
        return correctAuthor;
    }

    public void setCorrectAuthor(ReCiterAuthor correctAuthor) {
        this.correctAuthor = correctAuthor;
    }

    public int getCorrectAuthorRank() {
        return correctAuthorRank;
    }

    public void setCorrectAuthorRank(int correctAuthorRank) {
        this.correctAuthorRank = correctAuthorRank;
    }

    public int getNumAuthors() {
        return numAuthors;
    }

    public void setNumAuthors(int numAuthors) {
        this.numAuthors = numAuthors;
    }

    public String getMatchingDepartment() {
        return matchingDepartment;
    }

    public void setMatchingDepartment(String matchingDepartment) {
        this.matchingDepartment = matchingDepartment;
    }

    public List<KnownRelationship> getKnownRelationship() {
        return knownRelationship;
    }

    public void setKnownRelationship(List<KnownRelationship> knownRelationship) {
        this.knownRelationship = knownRelationship;
    }

    public List<ReCiterArticleGrant> getMatchingGrantList() {
        return matchingGrantList;
    }

    public void setMatchingGrantList(List<ReCiterArticleGrant> matchingGrantList) {
        this.matchingGrantList = matchingGrantList;
    }

    public List<String> getMatchingEmails() {
        return matchingEmails;
    }

    public void setMatchingEmails(List<String> matchingEmails) {
        this.matchingEmails = matchingEmails;
    }

    public String getPublishedPriorAcademicDegreeBachelors() {
        return publishedPriorAcademicDegreeBachelors;
    }

    public void setPublishedPriorAcademicDegreeBachelors(String publishedPriorAcademicDegreeBachelors) {
        this.publishedPriorAcademicDegreeBachelors = publishedPriorAcademicDegreeBachelors;
    }

    public String getPublishedPriorAcademicDegreeDoctoral() {
        return publishedPriorAcademicDegreeDoctoral;
    }

    public void setPublishedPriorAcademicDegreeDoctoral(String publishedPriorAcademicDegreeDoctoral) {
        this.publishedPriorAcademicDegreeDoctoral = publishedPriorAcademicDegreeDoctoral;
    }
}
