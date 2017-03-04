package reciter.engine.erroranalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.Citation;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.Citation.Journal;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.PositiveEvidence;
import reciter.engine.erroranalysis.ReCiterAnalysis.ReCiterAnalysisArticle.PositiveEvidence.ClusteredWithOtherMatchingArticle;
import reciter.model.article.ReCiterArticle;

public class ReCiterAnalysisTranslator {

	public static ReCiterAnalysis convert(String uid, List<Long> goldStandard, Analysis analysis, List<ReCiterCluster> reCiterClusters) {
		ReCiterAnalysis reCiterAnalysis = new ReCiterAnalysis();
		reCiterAnalysis.setCwid(uid);
		List<ReCiterAnalysisArticle> reCiterAnalysisArticles = new ArrayList<>();
		reCiterAnalysis.setReCiterAnalysisArticles(reCiterAnalysisArticles);
		Set<Long> goldStandardNotRetrieved = new HashSet<>(goldStandard);
		for (ReCiterCluster cluster : reCiterClusters) {
			for (ReCiterArticle reCiterArticle : cluster.getArticleCluster()) {
				if (goldStandardNotRetrieved.contains(reCiterArticle.getArticleId())) {
					goldStandardNotRetrieved.remove(reCiterArticle.getArticleId());
				}
				ReCiterAnalysisArticle article = new ReCiterAnalysisArticle();
				reCiterAnalysisArticles.add(article);
				article.setPmid(reCiterArticle.getArticleId());
				Citation citation = new Citation();
				article.setCitation(citation);
				citation.setPubDate(reCiterArticle.getJournal().getJournalIssuePubDateYear());
				citation.setAuthorList(reCiterArticle.getArticleCoAuthors().getAuthors());
				citation.setVolume(reCiterArticle.getJournal().getJournalTitle());
				Journal journal = new Journal();
				citation.setJournal(journal);
				journal.setVerbose(reCiterArticle.getJournal().getIsoAbbreviation());
				journal.setMedlineTA(reCiterArticle.getJournal().getJournalTitle());
				
				if (reCiterArticle.getScopusArticle() != null && reCiterArticle.getScopusArticle().getDoi() != null) {
					citation.setDoi(reCiterArticle.getScopusArticle().getDoi());
				}
				
				article.setUserAssertion(null);
				
				PositiveEvidence positiveEvidence = new PositiveEvidence();
				article.setPositiveEvidence(positiveEvidence);
				
				positiveEvidence.setMatchingNameVariant(reCiterArticle.getCorrectAuthor());
				positiveEvidence.setMatchingDepartment(reCiterArticle.getMatchingDepartment());
				positiveEvidence.setMatchingRelationships(reCiterArticle.getKnownRelationship());
				positiveEvidence.setMatchingInstitutionTargetAuthors(reCiterArticle.getFrequentInstitutionalCollaborators());
				// TODO matchingInstitutionFrequentCollaborator
				positiveEvidence.setMatchingGrantIDs(reCiterArticle.getMatchingGrantList());
				positiveEvidence.setMatchingEmails(reCiterArticle.getMatchingEmails());
				positiveEvidence.setPublishedPriorAcademicDegreeBachelors(reCiterArticle.getPublishedPriorAcademicDegreeBachelors());
				positiveEvidence.setPublishedPriorAcademicDegreeDoctoral(reCiterArticle.getPublishedPriorAcademicDegreeDoctoral());
				
				ClusteredWithOtherMatchingArticle clusteredWithOtherMatchingArticle = new ClusteredWithOtherMatchingArticle();
				positiveEvidence.setClusteredWithOtherMatchingArticle(clusteredWithOtherMatchingArticle);
				
				clusteredWithOtherMatchingArticle.setMeshMajor(reCiterArticle.getMeshMajorInfo().toString());
				clusteredWithOtherMatchingArticle.setCites(reCiterArticle.getCitesInfo().toString());
				clusteredWithOtherMatchingArticle.setCitedBy(reCiterArticle.getCitedByInfo().toString());
				clusteredWithOtherMatchingArticle.setCoCitation(reCiterArticle.getCoCitationInfo().toString());
				clusteredWithOtherMatchingArticle.setJournalTitle(reCiterArticle.getJournalTitleInfo().toString());
			}
		}
		reCiterAnalysis.setNotRetrievedGoldStandards(new ArrayList<>(goldStandardNotRetrieved));
		return reCiterAnalysis;
	}
}
