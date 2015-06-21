package reciter.erroranalysis;

public class AnalysisObject {

	private StatusEnum status;
	private String cwid;
	private String targetName;
	private String pubmedSearchQuery;
	private String pmid;
	private String articleTitle;
	private String fullJournalTitle;
	private String publicationYear;
	private String scopusTargetAuthorAffiliation;
	private String scopusCoAuthorAffiliation;
	private String pubmedTargetAuthorAffiliation;
	private String pubmedCoAuthorAffiliation;
	private String articleKeywords;
	private double nameMatchingScore;
	private boolean isClusterOriginator;
	private double journalSimilarityPhaseOne;
	private double coauthorAffiliationScore;
	private double targetAuthorAffiliationScore;
	private double knownCoinvestigatorScore;
	private double fundingStatementScore;
	private double terminalDegreeScore;
	private double defaultDepartmentJournalSimilarityScore;
	private double departmentOfAffiliationScore;
	private double keywordMatchingScore;
	private double phaseTwoSimilarityThreshold;
	private int clusterArticleAssignedTo;
	private int countArticlesInAssignedCluster;
	private boolean isClusterSelectedInPhaseTwoMatching;
	private double affiliationSimilarity;
	private double keywordSimilarity;
	private double journalSimilarityPhaseTwo;
	
	public StatusEnum getStatus() {
		return status;
	}
	public void setStatus(StatusEnum status) {
		this.status = status;
	}
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getTargetName() {
		return targetName;
	}
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
	public String getPubmedSearchQuery() {
		return pubmedSearchQuery;
	}
	public void setPubmedSearchQuery(String pubmedSearchQuery) {
		this.pubmedSearchQuery = pubmedSearchQuery;
	}
	public String getPmid() {
		return pmid;
	}
	public void setPmid(String pmid) {
		this.pmid = pmid;
	}
	public String getArticleTitle() {
		return articleTitle;
	}
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	public String getFullJournalTitle() {
		return fullJournalTitle;
	}
	public void setFullJournalTitle(String fullJournalTitle) {
		this.fullJournalTitle = fullJournalTitle;
	}
	public String getPublicationYear() {
		return publicationYear;
	}
	public void setPublicationYear(String publicationYear) {
		this.publicationYear = publicationYear;
	}
	public String getScopusTargetAuthorAffiliation() {
		return scopusTargetAuthorAffiliation;
	}
	public void setScopusTargetAuthorAffiliation(
			String scopusTargetAuthorAffiliation) {
		this.scopusTargetAuthorAffiliation = scopusTargetAuthorAffiliation;
	}
	public String getScopusCoAuthorAffiliation() {
		return scopusCoAuthorAffiliation;
	}
	public void setScopusCoAuthorAffiliation(String scopusCoAuthorAffiliation) {
		this.scopusCoAuthorAffiliation = scopusCoAuthorAffiliation;
	}
	public String getPubmedTargetAuthorAffiliation() {
		return pubmedTargetAuthorAffiliation;
	}
	public void setPubmedTargetAuthorAffiliation(
			String pubmedTargetAuthorAffiliation) {
		this.pubmedTargetAuthorAffiliation = pubmedTargetAuthorAffiliation;
	}
	public String getPubmedCoAuthorAffiliation() {
		return pubmedCoAuthorAffiliation;
	}
	public void setPubmedCoAuthorAffiliation(String pubmedCoAuthorAffiliation) {
		this.pubmedCoAuthorAffiliation = pubmedCoAuthorAffiliation;
	}
	public String getArticleKeywords() {
		return articleKeywords;
	}
	public void setArticleKeywords(String articleKeywords) {
		this.articleKeywords = articleKeywords;
	}
	public double getNameMatchingScore() {
		return nameMatchingScore;
	}
	public void setNameMatchingScore(double nameMatchingScore) {
		this.nameMatchingScore = nameMatchingScore;
	}
	public boolean isClusterOriginator() {
		return isClusterOriginator;
	}
	public void setClusterOriginator(boolean isClusterOriginator) {
		this.isClusterOriginator = isClusterOriginator;
	}
	public double getJournalSimilarityPhaseOne() {
		return journalSimilarityPhaseOne;
	}
	public void setJournalSimilarityPhaseOne(double journalSimilarityPhaseOne) {
		this.journalSimilarityPhaseOne = journalSimilarityPhaseOne;
	}
	public double getCoauthorAffiliationScore() {
		return coauthorAffiliationScore;
	}
	public void setCoauthorAffiliationScore(double coauthorAffiliationScore) {
		this.coauthorAffiliationScore = coauthorAffiliationScore;
	}
	public double getTargetAuthorAffiliationScore() {
		return targetAuthorAffiliationScore;
	}
	public void setTargetAuthorAffiliationScore(double targetAuthorAffiliationScore) {
		this.targetAuthorAffiliationScore = targetAuthorAffiliationScore;
	}
	public double getKnownCoinvestigatorScore() {
		return knownCoinvestigatorScore;
	}
	public void setKnownCoinvestigatorScore(double knownCoinvestigatorScore) {
		this.knownCoinvestigatorScore = knownCoinvestigatorScore;
	}
	public double getFundingStatementScore() {
		return fundingStatementScore;
	}
	public void setFundingStatementScore(double fundingStatementScore) {
		this.fundingStatementScore = fundingStatementScore;
	}
	public double getTerminalDegreeScore() {
		return terminalDegreeScore;
	}
	public void setTerminalDegreeScore(double terminalDegreeScore) {
		this.terminalDegreeScore = terminalDegreeScore;
	}
	public double getDefaultDepartmentJournalSimilarityScore() {
		return defaultDepartmentJournalSimilarityScore;
	}
	public void setDefaultDepartmentJournalSimilarityScore(
			double defaultDepartmentJournalSimilarityScore) {
		this.defaultDepartmentJournalSimilarityScore = defaultDepartmentJournalSimilarityScore;
	}
	public double getDepartmentOfAffiliationScore() {
		return departmentOfAffiliationScore;
	}
	public void setDepartmentOfAffiliationScore(double departmentOfAffiliationScore) {
		this.departmentOfAffiliationScore = departmentOfAffiliationScore;
	}
	public double getKeywordMatchingScore() {
		return keywordMatchingScore;
	}
	public void setKeywordMatchingScore(double keywordMatchingScore) {
		this.keywordMatchingScore = keywordMatchingScore;
	}
	public double getPhaseTwoSimilarityThreshold() {
		return phaseTwoSimilarityThreshold;
	}
	public void setPhaseTwoSimilarityThreshold(double phaseTwoSimilarityThreshold) {
		this.phaseTwoSimilarityThreshold = phaseTwoSimilarityThreshold;
	}
	public int getClusterArticleAssignedTo() {
		return clusterArticleAssignedTo;
	}
	public void setClusterArticleAssignedTo(int clusterArticleAssignedTo) {
		this.clusterArticleAssignedTo = clusterArticleAssignedTo;
	}
	public int getCountArticlesInAssignedCluster() {
		return countArticlesInAssignedCluster;
	}
	public void setCountArticlesInAssignedCluster(int countArticlesInAssignedCluster) {
		this.countArticlesInAssignedCluster = countArticlesInAssignedCluster;
	}
	public boolean isClusterSelectedInPhaseTwoMatching() {
		return isClusterSelectedInPhaseTwoMatching;
	}
	public void setClusterSelectedInPhaseTwoMatching(
			boolean isClusterSelectedInPhaseTwoMatching) {
		this.isClusterSelectedInPhaseTwoMatching = isClusterSelectedInPhaseTwoMatching;
	}
	public double getAffiliationSimilarity() {
		return affiliationSimilarity;
	}
	public void setAffiliationSimilarity(double affiliationSimilarity) {
		this.affiliationSimilarity = affiliationSimilarity;
	}
	public double getKeywordSimilarity() {
		return keywordSimilarity;
	}
	public void setKeywordSimilarity(double keywordSimilarity) {
		this.keywordSimilarity = keywordSimilarity;
	}
	public double getJournalSimilarityPhaseTwo() {
		return journalSimilarityPhaseTwo;
	}
	public void setJournalSimilarityPhaseTwo(double journalSimilarityPhaseTwo) {
		this.journalSimilarityPhaseTwo = journalSimilarityPhaseTwo;
	}
}
