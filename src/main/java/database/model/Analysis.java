package database.model;

public class Analysis {
	private String cwid;
	private String analysisStatus;
	private int goldStandard;
	private String targetName;
	private String pubmedSearchQuery;
	private String pmid;
	private String articleTitle;
	private String fullJournalTitle;
	private String publicationYear;
	private String scopusTargetAuthorAffiliation;
	private String scopusCoauthorAffiliation;
	private String pubmedTargetAuthorAffiliation;
	private String pubmedCoauthorAffiliation;
	private String articleKeywords;
	private boolean clusterOriginator;
	private int clusterArticleAssignedTo;
	private int countArticlesInAssignedClsuter;
	private int clusterSelectedInPhaseTwoMatching;

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
	public String getCwid() {
		return cwid;
	}
	public void setCwid(String cwid) {
		this.cwid = cwid;
	}
	public String getAnalysisStatus() {
		return analysisStatus;
	}
	public void setAnalysisStatus(String analysisStatus) {
		this.analysisStatus = analysisStatus;
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
	public void setScopusTargetAuthorAffiliation(String scopusTargetAuthorAffiliation) {
		this.scopusTargetAuthorAffiliation = scopusTargetAuthorAffiliation;
	}
	public String getScopusCoauthorAffiliation() {
		return scopusCoauthorAffiliation;
	}
	public void setScopusCoauthorAffiliation(String scopusCoauthorAffiliation) {
		this.scopusCoauthorAffiliation = scopusCoauthorAffiliation;
	}
	public String getPubmedTargetAuthorAffiliation() {
		return pubmedTargetAuthorAffiliation;
	}
	public void setPubmedTargetAuthorAffiliation(String pubmedTargetAuthorAffiliation) {
		this.pubmedTargetAuthorAffiliation = pubmedTargetAuthorAffiliation;
	}
	public String getPubmedCoauthorAffiliation() {
		return pubmedCoauthorAffiliation;
	}
	public void setPubmedCoauthorAffiliation(String pubmedCoauthorAffiliation) {
		this.pubmedCoauthorAffiliation = pubmedCoauthorAffiliation;
	}
	public String getArticleKeywords() {
		return articleKeywords;
	}
	public void setArticleKeywords(String articleKeywords) {
		this.articleKeywords = articleKeywords;
	}
	public boolean isClusterOriginator() {
		return clusterOriginator;
	}
	public void setClusterOriginator(boolean clusterOriginator) {
		this.clusterOriginator = clusterOriginator;
	}
	public double getKnownCoinvestigatorScore() {
		return knownCoinvestigatorScore;
	}
	public void setKnownCoinvestigatorScore(double knownCoinvestigatorScore) {
		this.knownCoinvestigatorScore = knownCoinvestigatorScore;
	}

	public int getClusterArticleAssignedTo() {
		return clusterArticleAssignedTo;
	}
	public void setClusterArticleAssignedTo(int clusterArticleAssignedTo) {
		this.clusterArticleAssignedTo = clusterArticleAssignedTo;
	}
	public int getCountArticlesInAssignedClsuter() {
		return countArticlesInAssignedClsuter;
	}
	public void setCountArticlesInAssignedClsuter(int countArticlesInAssignedClsuter) {
		this.countArticlesInAssignedClsuter = countArticlesInAssignedClsuter;
	}
	public int getClusterSelectedInPhaseTwoMatching() {
		return clusterSelectedInPhaseTwoMatching;
	}
	public void setClusterSelectedInPhaseTwoMatching(int clusterSelectedInPhaseTwoMatching) {
		this.clusterSelectedInPhaseTwoMatching = clusterSelectedInPhaseTwoMatching;
	}
	public int getGoldStandard() {
		return goldStandard;
	}
	public void setGoldStandard(int goldStandard) {
		this.goldStandard = goldStandard;
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
