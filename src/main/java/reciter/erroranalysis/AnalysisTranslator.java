package reciter.erroranalysis;

import reciter.model.article.ReCiterArticle;
import reciter.model.author.TargetAuthor;

public class AnalysisTranslator {

	public static AnalysisObject translate(
			ReCiterArticle reCiterArticle,
			StatusEnum status,
			String cwid,
			TargetAuthor targetAuthor,
			boolean isClusterOriginator) {
		AnalysisObject analysisObject = new AnalysisObject();
		
		analysisObject.setStatus(status);
		analysisObject.setCwid(cwid);
		analysisObject.setTargetName(
				targetAuthor.getAuthorName().getFirstName() + 
				targetAuthor.getAuthorName().getMiddleName() + 
				targetAuthor.getAuthorName().getLastName());
		analysisObject.setPubmedSearchQuery("");
		analysisObject.setPmid(Integer.toString(reCiterArticle.getArticleId()));
		analysisObject.setArticleTitle(reCiterArticle.getArticleTitle());
		analysisObject.setFullJournalTitle(reCiterArticle.getJournal().getJournalTitle());
		analysisObject.setPublicationYear(Integer.toString(reCiterArticle.getJournal().getJournalIssuePubDateYear()));
		
		analysisObject.setScopusTargetAuthorAffiliation("");
		analysisObject.setScopusCoAuthorAffiliation("");
		analysisObject.setPubmedTargetAuthorAffiliation("");
		analysisObject.setPubmedCoAuthorAffiliation("");
		
		analysisObject.setArticleKeywords(reCiterArticle.getArticleKeywords().toString());
		analysisObject.setNameMatchingScore(0);
		
		analysisObject.setClusterOriginator(isClusterOriginator);
		analysisObject.setJournalSimilarityPhaseOne(0);
		analysisObject.setCoauthorAffiliationScore(0);
		analysisObject.setTargetAuthorAffiliationScore(0);
		analysisObject.setKnownCoinvestigatorScore(0);
		analysisObject.setFundingStatementScore(0);
		analysisObject.setTerminalDegreeScore(0);
		analysisObject.setDefaultDepartmentJournalSimilarityScore(0);
		analysisObject.setDepartmentOfAffiliationScore(0);
		analysisObject.setKeywordMatchingScore(0);
		analysisObject.setPhaseTwoSimilarityThreshold(0);
		analysisObject.setClusterArticleAssignedTo(0);
		analysisObject.setCountArticlesInAssignedCluster(0);
		analysisObject.setClusterSelectedInPhaseTwoMatching(false);
		analysisObject.setAffiliationSimilarity(0);
		analysisObject.setKeywordSimilarity(0);
		analysisObject.setJournalSimilarityPhaseTwo(0);
		
		return analysisObject;
	}
}
