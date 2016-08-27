package reciter.engine.erroranalysis;

public class AnalysisObject {

	// Information regarding the article and target author.
	private StatusEnum status;
	private String cwid;
	private String targetName;
	private String pubmedSearchQuery;
	private long pmid;
	private String articleTitle;
	private String fullJournalTitle;
	private String publicationYear;
	private String scopusTargetAuthorAffiliation;
	private String scopusCoAuthorAffiliation;
	private String pubmedTargetAuthorAffiliation;
	private String pubmedCoAuthorAffiliation;
	private String articleKeywords;
	
	private boolean isClusterOriginator;
	private long clusterArticleAssignedTo;
	private int countArticlesInAssignedCluster;
	private boolean isClusterSelectedInPhaseTwoMatching;
	
	// Scores.
	private double emailStrategyScore;
	private double departmentStrategyScore;
	private double knownCoinvestigatorScore;
	private double affiliationScore;
	private double scopusStrategyScore;
	private double coauthorStrategyScore;
	private double journalStrategyScore;
	private double citizenshipStrategyScore;
	private double bachelorsYearDiscrepancyScore;
	private double doctoralYearDiscrepancyScore;
	private boolean isArticleTitleStartWithBracket;
	private double educationScore;
	
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
	public long getPmid() {
		return pmid;
	}
	public void setPmid(long pmid) {
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
	public boolean isClusterOriginator() {
		return isClusterOriginator;
	}
	public void setClusterOriginator(boolean isClusterOriginator) {
		this.isClusterOriginator = isClusterOriginator;
	}
	public long getClusterArticleAssignedTo() {
		return clusterArticleAssignedTo;
	}
	public void setClusterArticleAssignedTo(long clusterArticleAssignedTo) {
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
	public double getEducationScore() {
		return educationScore;
	}
	public void setEducationScore(double educationScore) {
		this.educationScore = educationScore;
	}
}
