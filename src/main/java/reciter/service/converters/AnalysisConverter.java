package reciter.service.converters;

import java.util.ArrayList;
import java.util.List;

import database.model.Analysis;
import reciter.erroranalysis.AnalysisObject;

public class AnalysisConverter {

	public static List<Analysis> convertToAnalysisList(List<AnalysisObject> analysisObjectList) {
		List<Analysis> analysisList = new ArrayList<Analysis>();
		for (AnalysisObject analysisObject : analysisObjectList) {
			analysisList.add(convertToAnalysis(analysisObject));
		}
		return analysisList;
	}
	
	public static Analysis convertToAnalysis(AnalysisObject analysisObject) {
		Analysis analysis = new Analysis();
		analysis.setCwid(analysisObject.getCwid());
		analysis.setAnalysisStatus(analysisObject.getStatus().name());
		analysis.setTargetName(analysisObject.getTargetName());
		analysis.setPubmedSearchQuery(analysisObject.getPubmedSearchQuery());
		analysis.setPmid(analysisObject.getPmid());
		analysis.setArticleTitle(analysisObject.getArticleTitle());
		analysis.setFullJournalTitle(analysisObject.getFullJournalTitle());
		analysis.setPublicationYear(analysisObject.getPublicationYear());
		analysis.setScopusTargetAuthorAffiliation(analysisObject.getScopusTargetAuthorAffiliation());
		analysis.setScopusCoauthorAffiliation(analysisObject.getScopusCoAuthorAffiliation());
		analysis.setPubmedTargetAuthorAffiliation(analysisObject.getPubmedTargetAuthorAffiliation());
		analysis.setPubmedCoauthorAffiliation(analysisObject.getPubmedCoAuthorAffiliation());
		analysis.setArticleKeywords(analysisObject.getArticleKeywords());
		analysis.setNameMatchingScore(analysisObject.getNameMatchingScore());
		analysis.setClusterOriginator(analysisObject.isClusterOriginator());
		analysis.setJournalSimilarityPhaseOne(analysisObject.getJournalSimilarityPhaseOne());
		analysis.setCoauthorAffiliationScore(analysisObject.getCoauthorAffiliationScore());
		analysis.setTargetAuthorAffiliationScore(analysisObject.getTargetAuthorAffiliationScore());
		analysis.setKnownCoinvestigatorScore(analysisObject.getKnownCoinvestigatorScore());
		analysis.setFundingStatementScore(analysisObject.getFundingStatementScore());
		analysis.setTerminalDegreeScore(analysisObject.getTerminalDegreeScore());
		analysis.setDefaultDepartmentJournalSimilarityScore(analysisObject.getDefaultDepartmentJournalSimilarityScore());
		analysis.setDepartmentOfAffiliationScore(analysisObject.getDepartmentStrategyScore());
		analysis.setDepartmentOfAffiliationScore(analysisObject.getDepartmentStrategyScore());
		analysis.setKeywordMatchingScore(analysisObject.getKeywordMatchingScore());
		analysis.setPhaseTwoSimilarityThreshold(analysisObject.getPhaseTwoSimilarityThreshold());
		analysis.setClusterArticleAssignedTo(analysisObject.getClusterArticleAssignedTo());
		analysis.setCountArticlesInAssignedClsuter(analysisObject.getCountArticlesInAssignedCluster());
//		analysis.setClusterSelectedInPhaseTwoMatching(analysisObject);
		analysis.setPhaseTwoAffiliationSimilarity(analysisObject.getAffiliationScore());
		analysis.setPhaseTwoKeywordSimilarity(analysisObject.getKeywordSimilarity());
		analysis.setPhaseTwoJournalSimilarity(analysisObject.getJournalSimilarityPhaseTwo());
		return analysis;
	}
}
