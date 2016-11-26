package reciter.engine.erroranalysis;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import reciter.database.mongo.model.Identity;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.scopus.Affiliation;
import reciter.model.scopus.Author;
import reciter.model.scopus.ScopusArticle;

public class AnalysisTranslator {

	public static AnalysisObject translate(
			ReCiterArticle reCiterArticle,
			StatusEnum status,
			String cwid,
			Identity identity,
			boolean isClusterOriginator,
			long clusterId,
			int countOfArticleInCluster,
			boolean isClusterSelected) {

		AnalysisObject analysisObject = new AnalysisObject();
		analysisObject.setStatus(status);
		analysisObject.setCwid(cwid);
		analysisObject.setTargetName(
				identity.getAuthorName().getFirstName() + " " + 
						identity.getAuthorName().getMiddleName() + " " + 
						identity.getAuthorName().getLastName());
		
//		analysisObject.setPubmedSearchQuery(identity.getPubmedSearchQuery());
		analysisObject.setPmid(reCiterArticle.getArticleId());
		analysisObject.setArticleTitle(reCiterArticle.getArticleTitle());
		analysisObject.setFullJournalTitle(reCiterArticle.getJournal().getJournalTitle());
		analysisObject.setPublicationYear(Integer.toString(reCiterArticle.getJournal().getJournalIssuePubDateYear()));

		StringBuilder scopusTargetAuthorAffiliation = new StringBuilder();
		StringBuilder scopusCoAuthorAffiliation = new StringBuilder();
		StringBuilder pubmedTargetAuthorAffiliation = new StringBuilder();
		StringBuilder pubmedCoAuthorAffiliation = new StringBuilder();

		// TODO Create a separate function to get the details from Scopus.
		ScopusArticle scopusArticle = reCiterArticle.getScopusArticle();
		if (scopusArticle != null) {
			for (Author scopusAuthor : scopusArticle.getAuthors()) {
				String scopusAuthorFirstName = scopusAuthor.getGivenName();
				String scopusAuthorLastName = scopusAuthor.getSurname();
				String targetAuthorLastName = identity.getAuthorName().getLastName();
				if (StringUtils.equalsIgnoreCase(scopusAuthorLastName, targetAuthorLastName)) {
					String targetAuthorFirstInitial = identity.getAuthorName().getFirstInitial();
					if (scopusAuthorFirstName != null && scopusAuthorFirstName.length() > 1) {
						if (scopusAuthorFirstName.substring(0, 1).equals(targetAuthorFirstInitial)) {

							Set<Integer> afidSet = scopusAuthor.getAfidSet();
							for (int afid : afidSet) {
								for (Affiliation affiliation : scopusArticle.getAffiliations()) {
									if (affiliation.getAfid() == afid) {
										scopusTargetAuthorAffiliation.append("[" + 
												scopusAuthor.getGivenName() + " " + scopusAuthor.getSurname() + "=" +
												affiliation.getAffilname() + " " + 
												affiliation.getAffiliationCity() + " " +
												affiliation.getAffiliationCountry() + "]");
										break;
									}
								}
//								Affiliation affiliation = scopusArticle.getAffiliationMap().get(afid);
//								if (affiliation != null) {
//									scopusTargetAuthorAffiliation.append("[" + 
//											scopusAuthor.getGivenName() + " " + scopusAuthor.getSurname() + "=" +
//											affiliation.getAffilname() + " " + 
//											affiliation.getAffiliationCity() + " " +
//											affiliation.getAffiliationCountry() + "]");
//								}
							}
						}
					}
				} else {
					Set<Integer> afidSet = scopusAuthor.getAfidSet();
					for (int afid : afidSet) {
						for (Affiliation affiliation : scopusArticle.getAffiliations()) {
							if (affiliation.getAfid() == afid) {
								scopusTargetAuthorAffiliation.append("[" + 
										scopusAuthor.getGivenName() + " " + scopusAuthor.getSurname() + "=" +
										affiliation.getAffilname() + " " + 
										affiliation.getAffiliationCity() + " " +
										affiliation.getAffiliationCountry() + "]");
								break;
							}
						}
//						Affiliation affiliation = scopusArticle.getAffiliationMap().get(afid);
//						if (affiliation != null) {
//							scopusCoAuthorAffiliation.append("[" + 
//									scopusAuthor.getGivenName() + " " + scopusAuthor.getSurname() + "=" +
//									affiliation.getAffilname() + " " + 
//									affiliation.getAffiliationCity() + " " +
//									affiliation.getAffiliationCountry() + "], ");
//						}
					}
				}
			}
		}

		for (ReCiterAuthor reCiterAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (reCiterAuthor.getAuthorName().getLastName().equalsIgnoreCase(identity.getAuthorName().getLastName())) {
				if (reCiterAuthor.getAffiliation() != null) {
					pubmedTargetAuthorAffiliation.append(reCiterAuthor.getAffiliation().getAffiliationName());
				}
			} else {
				if (reCiterAuthor.getAffiliation() != null) {
					pubmedCoAuthorAffiliation.append("[" +
							reCiterAuthor.getAuthorName().getFirstName() + " " +
							reCiterAuthor.getAuthorName().getMiddleName() + " " +
							reCiterAuthor.getAuthorName().getLastName() + "=" +
							reCiterAuthor.getAffiliation().getAffiliationName() + "]"
							);
				} else {
					pubmedCoAuthorAffiliation.append("[" +
							reCiterAuthor.getAuthorName().getFirstName() + " " +
							reCiterAuthor.getAuthorName().getMiddleName() + " " +
							reCiterAuthor.getAuthorName().getLastName() + "=" +
							"N/A]"
							);
				}
			}
		}

		analysisObject.setScopusTargetAuthorAffiliation(scopusTargetAuthorAffiliation.toString());
		analysisObject.setScopusCoAuthorAffiliation(scopusCoAuthorAffiliation.toString());
		analysisObject.setPubmedTargetAuthorAffiliation(pubmedTargetAuthorAffiliation.toString());
		analysisObject.setPubmedCoAuthorAffiliation(pubmedCoAuthorAffiliation.toString());

		analysisObject.setArticleKeywords(reCiterArticle.getArticleKeywords().toString());

		analysisObject.setClusterOriginator(isClusterOriginator);
		analysisObject.setClusterArticleAssignedTo(clusterId);
		analysisObject.setCountArticlesInAssignedCluster(countOfArticleInCluster);
		analysisObject.setClusterSelectedInPhaseTwoMatching(isClusterSelected);

		analysisObject.setEmailStrategyScore(reCiterArticle.getEmailStrategyScore());
		analysisObject.setDepartmentStrategyScore(reCiterArticle.getDepartmentStrategyScore());
		analysisObject.setKnownCoinvestigatorScore(reCiterArticle.getKnownCoinvestigatorScore());
		analysisObject.setAffiliationScore(reCiterArticle.getAffiliationScore());
		analysisObject.setScopusStrategyScore(reCiterArticle.getScopusStrategyScore());
		analysisObject.setCoauthorStrategyScore(reCiterArticle.getCoauthorStrategyScore());
		analysisObject.setJournalStrategyScore(reCiterArticle.getJournalStrategyScore());
		analysisObject.setCitizenshipStrategyScore(reCiterArticle.getCitizenshipStrategyScore());
		analysisObject.setBachelorsYearDiscrepancyScore(reCiterArticle.getBachelorsYearDiscrepancyScore());
		analysisObject.setDoctoralYearDiscrepancyScore(reCiterArticle.getDoctoralYearDiscrepancyScore());
		analysisObject.setArticleTitleStartWithBracket(reCiterArticle.isArticleTitleStartWithBracket());
		analysisObject.setEducationScore(reCiterArticle.getEducationStrategyScore());

		analysisObject.setDateInitialRun(identity.getDateInitialRun());
		analysisObject.setDateLastRun(identity.getDateLastRun());
		
		analysisObject.setTargetAuthorYearBachelorsDegree(identity.getBachelor().getDegreeYear());
		analysisObject.setTargetAuthorYearTerminalDegree(identity.getDoctoral().getDegreeYear());
		analysisObject.setDepartments(identity.getDepartments());
		analysisObject.setTargetAuthorKnownEmails(identity.getEmails());
		analysisObject.setTargetAuthorKnownNameAliases(identity.getAliases());
		analysisObject.setTargetAuthorKnownAffiliations(identity.getAffiliations());
		analysisObject.setBachelorsYearDiscrepancy(reCiterArticle.getBachelorsYearDiscrepancy());
		analysisObject.setDoctoralYearDiscrepancy(reCiterArticle.getDoctoralYearDiscrepancy());
		
		analysisObject.setFrequentInstitutionalCollaborators(reCiterArticle.getFrequentInstitutionalCollaborators());
		analysisObject.setKnownRelationships(reCiterArticle.getKnownRelationships());
		
		analysisObject.setClusterId(clusterId);
		analysisObject.setMeshMajorStrategyScore(reCiterArticle.getMeshMajorStrategyScore());
		analysisObject.setOverlappingMeSHMajorNegativeArticles(reCiterArticle.getOverlappingMeSHMajorNegativeArticles());
		return analysisObject;
	}
}
