package reciter.model.article;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import reciter.model.article.completeness.ArticleCompleteness;
import reciter.model.article.completeness.ReCiterCompleteness;
import reciter.model.identity.AuthorName;
import reciter.model.identity.Identity;
import reciter.model.scopus.ScopusArticle;

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
	
	private boolean shouldRemoveValue;
	private boolean foundAuthorWithSameFirstNameValue;
	private boolean foundMatchingAuthorValue;
	
	/**
	 * Text containing how it's clustered.
	 */
	@Transient
	private String clusterInfo = "";
	
	private double emailStrategyScore;
	private double departmentStrategyScore;
	private double knownCoinvestigatorScore;
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
	private double doctoralYearDiscrepancy;
	private boolean isArticleTitleStartWithBracket;
	private double educationStrategyScore;
	private double meshMajorStrategyScore;
	private Set<String> overlappingMeSHMajorNegativeArticles = new HashSet<String>();
	
	private int goldStandard;
	private Set<Long> commentsCorrectionsPmids = new HashSet<>();
	private List<ReCiterArticleMeshHeading> meshHeadings = new ArrayList<ReCiterArticleMeshHeading>();

	private List<ReCiterAuthor> knownRelationships = new ArrayList<ReCiterAuthor>();
	private List<String> frequentInstitutionalCollaborators = new ArrayList<String>();
	
	private List<Long> citations = new ArrayList<>();
	private AuthorName matchingName;
	
	private List<CoCitation> coCitation = new ArrayList<>();
	private ReCiterAuthor correctAuthor;
	
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
	 * @param articleID
	 */
	public ReCiterArticle(long articleId) {
		this.articleId = articleId;
		this.setArticleCompleteness(new ReCiterCompleteness());
	}
	
	public void setCorrectAuthor(Identity identity) {
		String targetAuthorLastName = identity.getPrimaryName().getLastName();
		ReCiterArticleAuthors authors = getArticleCoAuthors();
		if (authors != null) {
			for (ReCiterAuthor author : authors.getAuthors()) {
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
}
