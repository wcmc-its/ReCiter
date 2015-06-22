package reciter.erroranalysis;

import reciter.model.article.ReCiterArticle;

public class AnalysisTranslator {

	public static AnalysisObject translate(
			ReCiterArticle article,
			StatusEnum status,
			String cwid,
			String targetName,
			String pubmedSearchQuery,
			double nameMatchingScore,
			double journalSimilarityPhaseOne,
			double coAuthorAffiliationScore,
			double targetAuthorAffiliationScore,
			double knownCoinvestigatorScore,
			double fundingStatementScore,
			double terminalDegreeScore,
			double defaultDepartmentJournalSimilarityScore,
			double departmentOfAffiliationScore,
			double keywordMatchingScore) {
		
		AnalysisObject analysisObject = new AnalysisObject();
		analysisObject.setStatus(status);
		analysisObject.setCwid(cwid);
		analysisObject.setTargetName(targetName);
		analysisObject.setPubmedSearchQuery(pubmedSearchQuery);
		analysisObject.setPmid(String.valueOf(article.getArticleID()));
		analysisObject.setArticleTitle(article.getArticleTitle().getTitle());
		analysisObject.setFullJournalTitle(article.getJournal().getJournalTitle());
		analysisObject.setPublicationYear("-1");
		analysisObject.setScopusTargetAuthorAffiliation(null);
		analysisObject.setScopusCoAuthorAffiliation(null);
		analysisObject.setPubmedTargetAuthorAffiliation(null);
		analysisObject.setPubmedCoAuthorAffiliation(null);
		analysisObject.setArticleKeywords(null);
		analysisObject.setNameMatchingScore(nameMatchingScore);
		analysisObject.setClusterOriginator(false);
		analysisObject.setJournalSimilarityPhaseOne(-1);
		analysisObject.setCoauthorAffiliationScore(-1);
		analysisObject.setTargetAuthorAffiliationScore(-1);
		analysisObject.setKnownCoinvestigatorScore(-1);
		analysisObject.setFundingStatementScore(-1);
		analysisObject.setTerminalDegreeScore(-1);
		analysisObject.setDefaultDepartmentJournalSimilarityScore(-1);
		analysisObject.setDepartmentOfAffiliationScore(-1);
		analysisObject.setKeywordMatchingScore(-1);
		analysisObject.setPhaseTwoSimilarityThreshold(-1);
		analysisObject.setClusterArticleAssignedTo(-1);
		analysisObject.setCountArticlesInAssignedCluster(-1);
		analysisObject.setClusterSelectedInPhaseTwoMatching(false);
		analysisObject.setAffiliationSimilarity(-1);
		analysisObject.setKeywordSimilarity(-1);
		analysisObject.setJournalSimilarityPhaseTwo(-1);
		return analysisObject;
	}
}
