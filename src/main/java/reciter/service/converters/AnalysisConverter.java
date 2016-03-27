package reciter.service.converters;

import java.util.ArrayList;
import java.util.List;

import reciter.database.model.Analysis;
import reciter.erroranalysis.AnalysisObject;
import reciter.erroranalysis.StatusEnum;

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
		if (analysisObject.getStatus().equals(StatusEnum.TRUE_POSITIVE) || 
			analysisObject.getStatus().equals(StatusEnum.FALSE_NEGATIVE)) {
			analysis.setGoldStandard(1);
		} else {
			analysis.setGoldStandard(0);
		}
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
		analysis.setClusterOriginator(analysisObject.isClusterOriginator());
		analysis.setClusterArticleAssignedTo(analysisObject.getClusterArticleAssignedTo());
		analysis.setCountArticlesInAssignedClsuter(analysisObject.getCountArticlesInAssignedCluster());
		
		analysis.setEmailStrategyScore(analysisObject.getEmailStrategyScore());
		analysis.setDepartmentStrategyScore(analysisObject.getDepartmentStrategyScore());
		analysis.setKnownCoinvestigatorScore(analysisObject.getKnownCoinvestigatorScore());
		analysis.setAffiliationScore(analysisObject.getAffiliationScore());
		analysis.setScopusStrategyScore(analysisObject.getScopusStrategyScore());
		analysis.setCoauthorStrategyScore(analysisObject.getCoauthorStrategyScore());
		analysis.setJournalStrategyScore(analysisObject.getJournalStrategyScore());
		analysis.setCitizenshipStrategyScore(analysisObject.getCitizenshipStrategyScore());
		analysis.setBachelorsYearDiscrepancyScore(analysisObject.getBachelorsYearDiscrepancyScore());
		analysis.setDoctoralYearDiscrepancyScore(analysisObject.getDoctoralYearDiscrepancyScore());
		analysis.setArticleTitleStartWithBracket(analysisObject.isArticleTitleStartWithBracket());
		analysis.setEducationScore(analysisObject.getEducationScore());
		
		return analysis;
	}
}
