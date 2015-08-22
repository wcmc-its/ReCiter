package reciter.algorithm.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.model.boardcertifications.ReadBoardCertifications;
import reciter.utils.reader.YearDiscrepacyReader;
import xmlparser.pubmed.PubmedXmlFetcher;
import xmlparser.pubmed.model.MedlineCitationMeshHeadingDescriptorName;
import xmlparser.pubmed.model.PubmedArticle;
import xmlparser.scopus.ScopusXmlFetcher;
import xmlparser.scopus.model.ScopusArticle;
import database.dao.CoauthorAffiliationsDao;
import database.dao.IdentityCitizenshipDao;
import database.dao.IdentityDao;
import database.dao.IdentityEducationDao;
import database.dao.MatchingDepartmentsJournalsDao;
import database.model.CoauthorAffiliations;
import database.model.Identity;
import database.model.IdentityDirectory;

public class ReCiterClusterer implements Clusterer {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);	
	private Map<Integer, ReCiterCluster> finalCluster = new HashMap<Integer, ReCiterCluster>();
	private boolean selectingTarget = false;
	private int selectedReCiterClusterId = -1;
	private double similarityThreshold = 0.3;
	
	
	public ReCiterClusterer() {
		ReCiterCluster.getClusterIDCounter().set(0); // reset counter on cluster id.
	}

	public Map<Integer, ReCiterCluster> getFinalCluster() {
		return finalCluster;
	}

	public int assignTargetToCluster(ReCiterArticle article) {
		selectingTarget = true;
		selectedReCiterClusterId = selectCandidateCluster(article);
		return selectedReCiterClusterId;
	}

	/**
	 * 
	 * @param articleList
	 * @param targetAuthor
	 */
	@Override
	public void cluster(List<ReCiterArticle> reciterArticleList) {

		slf4jLogger.info("Number of articles to be clustered: " + reciterArticleList.size());

		ReCiterArticle first = null;
		if (reciterArticleList != null && reciterArticleList.size() > 0) {
			first = reciterArticleList.get(0);
		} else {
			return;
		}

		first.setClusterOriginator(true); // first article is the cluster starter.

		ReCiterCluster firstCluster = new ReCiterCluster();
		firstCluster.add(first);
		finalCluster.put(firstCluster.getClusterID(), firstCluster);

		for (int i = 1; i < reciterArticleList.size(); i++) {

			ReCiterArticle article = reciterArticleList.get(i);
			//			slf4jLogger.info("Assigning " + i + ": " + article.getArticleID());
			int selection = selectCandidateCluster(article);
			if (selection == -1) {
				article.setClusterOriginator(true);
				// create its own cluster.
				ReCiterCluster newCluster = new ReCiterCluster();
				newCluster.add(article);
				finalCluster.put(newCluster.getClusterID(), newCluster);
			} else {
				finalCluster.get(selection).add(article);
			}
		}
	}

	/**
	 * Select the candidate cluster.
	 * @param currentArticle
	 * @param targetAuthor
	 * @return
	 */
	public int selectCandidateCluster(ReCiterArticle currentArticle) {

		// Get cluster ids with max number of coauthor matches.
		Set<Integer> clusterIdSet = getKeysWithMaxVal(computeCoauthorMatch(currentArticle));
		//		slf4jLogger.info("PMID: " + currentArticle.getArticleID() + " " + clusterIdSet);

		// If groups have matching co-authors, the program selects the group that has the most matching names.
		if (clusterIdSet.size() == 1) {
			for (int id : clusterIdSet) {
				return id;
			}
		}

		// If two or more of these have the same number of coauthors, the one with the highest matching score is selected.
		if (clusterIdSet.size() > 1) {
			return getIdWithMostContentSimilarity(clusterIdSet, currentArticle);
		}

		// If groups have no matching co-authors, the group with the highest 
		// matching (cosine) score is selected, provided that the score
		// exceeds a given threshold.
		Set<Integer> allClusterIdSet = new HashSet<Integer>();
		for (ReCiterCluster c : finalCluster.values()) {
			allClusterIdSet.add(c.getClusterID());
		}
		return getIdWithMostContentSimilarity(allClusterIdSet, currentArticle);
	}

	
	
	/**
	 * Computes the similarity between an article and a cluster of articles.
	 * @param cluster Cluster of articles.
	 * @param article Article being compared.
	 * @return Similarity score between an article and a cluster of articles.
	 */
	private double computeClusterToArticleSimilarity(List<ReCiterArticle> cluster, ReCiterArticle article) {
		
		for (ReCiterArticle reCiterArticle : cluster) {
			
		}
		return 0;
	}
	
	/**
	 * 
	 * @param clusterIdList
	 * @param currentArticle
	 * @return
	 */
	private int getIdWithMostContentSimilarity(Set<Integer> clusterIdList, ReCiterArticle currentArticle) {
		double currentMax = -1;
		int currentMaxId = -1;
		String cwid = TargetAuthor.getInstance().getCwid();
		for (int id : clusterIdList) {

			double sim = finalCluster.get(id).contentSimilarity(currentArticle); // cosine similarity score.

			// Github issue: https://github.com/wcmc-its/ReCiter/issues/78 (Phase 2 clustering)
			// We have two sources for knowing whether someone lived or worked outside of the United States: 
			// rc_identity_citizenship and rc_identity_education (foreign countries are in parentheses there).
			if (selectingTarget) {				
				IdentityCitizenshipDao identityCitizenshipDao = new IdentityCitizenshipDao();
				IdentityEducationDao identityEducationDao = new IdentityEducationDao();
				List<String> citizenship = identityCitizenshipDao.getIdentityCitizenshipCountry(cwid);
				List<String> education = identityEducationDao.getIdentityCitizenshipEducation(cwid);
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					for (ReCiterAuthor coauthor : article.getArticleCoAuthors().getCoAuthors()) {
						// please skip the coauthor which is the targetAuthor by comparing the first name,
						// middle name, and last name.
						// if (coauthor.getAffiliation().equals(citizenship) || coauthor.getAffiliation().equals(education)) {
						if(coauthor.getAffiliation()!=null){
							String coAuthorAffiliation = coauthor.getAffiliation().getAffiliation();
							if(citizenship.contains(coAuthorAffiliation) || education.contains(coAuthorAffiliation)){						
								// increase sim score.
								sim = sim + 1; 
							}
						}
					}
				}
				
			}
			
			//  Assign Phase Two score to reflect the extent to which candidate articles have authors with affiliations that occur frequently with WCMC authors #74 
			if(selectingTarget){
				double score = 0;
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					List<ReCiterAuthor> coAuthors = article.getArticleCoAuthors().getCoAuthors();
					CoauthorAffiliationsDao dao = new CoauthorAffiliationsDao();
					List<String> coAuthorAffiliations = new ArrayList<String>();
					for(ReCiterAuthor coAuthor: coAuthors){	
						if(coAuthor!=null && coAuthor.getAffiliation()!=null && coAuthor.getAffiliation().getAffiliation()!=null)
						coAuthorAffiliations.add(coAuthor.getAffiliation().getAffiliation());
					}
					if(coAuthorAffiliations.size()>0){
						List<CoauthorAffiliations> coAuthorAffiliationList = dao.getCoathorAffiliationsByAffiliationLabel(coAuthorAffiliations);
						for(CoauthorAffiliations coaf: coAuthorAffiliationList){
							score=score+coaf.getScore();
						}
					}
				}
			}
			
			/* Use citizenship and educational background to improve precision #97 */  
			if (selectingTarget) {
				IdentityCitizenshipDao identityCitizenshipDao = new IdentityCitizenshipDao();
				IdentityEducationDao identityEducationDao = new IdentityEducationDao();
				
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					for (ReCiterAuthor coauthor : article.getArticleCoAuthors().getCoAuthors()) {
							
						/* skip the coauthor which is the targetAuthor by comparing the first name, middle name, and last name. */
						if (coauthor.getAuthorName().getFirstName().equals(TargetAuthor.getInstance().getAuthorName().getFirstName()) || 
							coauthor.getAuthorName().getLastName().equals(TargetAuthor.getInstance().getAuthorName().getLastName()) || 
							coauthor.getAuthorName().getMiddleName().equals(TargetAuthor.getInstance().getAuthorName().getMiddleName())) {
							continue; 
						}						
						// if (coauthor.getAffiliation().equals(identityCitizenshipDao.getIdentityCitizenshipCountry(cwid))) {
						if (identityCitizenshipDao.getIdentityCitizenshipCountry(cwid).equals(identityEducationDao.getIdentityCitizenshipEducation(cwid))) {
							// increase sim score.
							sim = sim + 1; 
						}
					}
				}
			}
			
			/* Look up email separately in Scopus and PubMed at a formative state to find name variants #73 */ 
			
			if (selectingTarget) {
				String firstName = TargetAuthor.getInstance().getAuthorName().getFirstName();
				String lastName = TargetAuthor.getInstance().getAuthorName().getLastName();
				IdentityDao dao = new IdentityDao();
				Identity identity = dao.getIdentityByCwid(cwid);
				String emailId = identity!=null && identity.getEmail()!=null?identity.getEmail():"";
				String emailOther = identity!=null && identity.getEmailOther()!=null?identity.getEmailOther():"";
				
				PubmedXmlFetcher pubmedXmlFetcher = new PubmedXmlFetcher();
				if(currentArticle.getAliasesList()!=null && currentArticle.getAliasesList().size()>0){
					List<IdentityDirectory> aliases = currentArticle.getAliasesList();
					for(IdentityDirectory dir:aliases){
						if(cwid.equals(dir.getCwid()))
						pubmedXmlFetcher.preparePubMedQueries(dir.getSurname(), dir.getGivenName(), dir.getMiddleName());
					}
				}
				List<PubmedArticle> pubmedArticleList = pubmedXmlFetcher.getPubmedArticle(lastName, firstName,TargetAuthor.getInstance().getAuthorName().getMiddleName(), cwid);

				// Retrieve the scopus affiliation information for this cwid if the affiliations have not been retrieve yet.
				ScopusXmlFetcher scopusXmlFetcher = new ScopusXmlFetcher();
				
				for (PubmedArticle pubmedArticle : pubmedArticleList) {
					String pmid = pubmedArticle.getMedlineCitation().getPmid().getPmidString();
					ScopusArticle scopusArticle = scopusXmlFetcher.getScopusXml(cwid, pmid);
					
					if (lastName!= null && firstName != null ) { 
					   // TBD 
					}				
				}
			}
			
			// Github issue: https://github.com/wcmc-its/ReCiter/issues/79
			// Leverage departmental affiliation string matching for phase two matching.
			if (selectingTarget) {
				// Grab columns "primary_department" and "other_departments" from table "rc_identity".
				/*
				 * Please be sure to translate "and" into different ways it's represented. "Pathology and Laboratory Medicine" should become:
					1. Pathology and Laboratory Medicine
					2. Pathology/Laboratory Medicine
					3. Pathology & Laboratory Medicine
				 */
				/*
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					// compare the above departments information with the article's affiliation information.
					// increase the sim score if departments match.
				}
				 */
				MatchingDepartmentsJournalsDao departmentJournalsDao = new MatchingDepartmentsJournalsDao();
				List<String> departmentList = departmentJournalsDao.getTranslatedDepartmentList();
			    for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
			    	for ( String matchDepartment: departmentList)
						if (StringUtils.contains(StringUtils.lowerCase(article.getAffiliationConcatenated()), matchDepartment)) {
							sim = sim + 1;
						}
			    }
			}
			
			/* Improve score in cases where MeSH major terms match between cluster and target article #82 */ 
			/* https://github.com/wcmc-its/ReCiter/issues/82 */ 
			
			if (!selectingTarget) {
				MedlineCitationMeshHeadingDescriptorName meshName = new MedlineCitationMeshHeadingDescriptorName();
				String meshTermValue = meshName.getDescriptorNameString();
				//System.out.println(meshTermValue);
				if(meshTermValue!=null){
					String[] meshTerms = meshTermValue.split(" "); 
					String getTargetAuthorTitle = TargetAuthor.getInstance().getTargetAuthorArticleIndexed().getArticleTitle().getTitle();
					/* Not clear about calculation of the MeshTerms Score  */ 
					for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {	
						for (String meshTerm : meshTerms) { 
							if (meshTerm==null || meshTerm.equals("and") || meshTerm.equals("or") || meshTerm.equals("of") || meshTerm.equals("for") || meshTerm.equals(" ")) { continue;  }
							if (article.getArticleTitle().getTitle().contains(meshTerm) && getTargetAuthorTitle.contains(meshTerm)){ 
								sim = sim + 1;
							}
						}
					}
				}
			}
			
			// Github issue: https://github.com/wcmc-its/ReCiter/issues/60
			// For individuals with no/few papers, use default departmental-journal similarity score.
			if (selectingTarget) {
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					MatchingDepartmentsJournalsDao matchingDepartmentsJournalsDao = 
							new MatchingDepartmentsJournalsDao();
					double score = matchingDepartmentsJournalsDao.getScoreByJournalAndDepartment(
							article.getJournal().getIsoAbbreviation(), TargetAuthor.getInstance().getDepartment());
					sim *= (1 + score);
				}
			}
		
			// Github issue: https://github.com/wcmc-its/ReCiter/issues/45
			// Leverage data on board certifications to improve phase two matching.
			if (selectingTarget){// && currentArticle!=null && currentArticle.getArticleTitle().getTitle()!=null) {
				ReadBoardCertifications efr=new ReadBoardCertifications();
				
				sim= sim+efr.getBoardCertifications(cwid,finalCluster.get(id).getArticleCluster());
				//slf4jLogger.info("Board Certifications Sim Score for CWID("+cwid+" => " + sim);
				//for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
				//					// TODO if TargetAuthor.getInstance()'s board certification matches the `article` object. Increase `sim`.
					//if(articles.contains(article))sim=sim+1;
				//}
			}
			//
			//			// Github issue: https://github.com/wcmc-its/ReCiter/issues/49
			//			// Leverage known co-investigators on grants to improve phase two matching.
			if (selectingTarget) {
				IdentityDao identityDao = new IdentityDao(); 
				List<Identity> identityList = identityDao.getAssosiatedGrantIdentityList(TargetAuthor.getInstance().getCwid());
				
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {			
					for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
						for(Identity identity: identityList){
							for (ReCiterAuthor currentArticleCoAuthor : currentArticle.getArticleCoAuthors().getCoAuthors()) {
								if (currentArticleCoAuthor.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
									// First Name 
									if (author.getAuthorName().getFirstName().equalsIgnoreCase(identity.getFirstName()) && currentArticleCoAuthor.getAuthorName().getFirstName().equalsIgnoreCase(identity.getFirstName())){
										sim = sim + 1;
									}
									// Last Name
									if (author.getAuthorName().getLastName().equalsIgnoreCase(identity.getLastName()) && currentArticleCoAuthor.getAuthorName().getLastName().equalsIgnoreCase(identity.getLastName()) ){
										sim = sim + 1;
									}
								}
							}						
						}							
					}
				}
			}
			
			
			
			//
//			// Github issue: https://github.com/wcmc-its/ReCiter/issues/83
//			// If a candidate article is published in a journal and the cluster contains that journal, increase the score for a match.
			if (!selectingTarget) {
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					if(article.getJournal().getJournalTitle().equalsIgnoreCase(currentArticle.getJournal().getJournalTitle())){
						sim = sim + 1;
					}
//					// TODO If `article`'s journal title matches (by direct string matching or journal similarity) `currentArticle`'s
//					// journal tit//le, increase `sim` score.
				}
			}

			// Grab CWID from rc_identity table. Combine with "@med.cornell.edu" and match against candidate records. 
			// When email is found in affiliation string, during phase two clustering, automatically assign the matching identity.
			if (selectingTarget) {
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					if (article.getAffiliationConcatenated() != null) {
						if (article.getAffiliationConcatenated().contains(TargetAuthor.getInstance().getCwid() + "@med.cornell.edu")) {
							//							sim *= 1.3; // a matching email should dramatically increase the score of some results but not decrease the score of others
							return id;
						}
					}
				}
			}


			// Increase similarity if the affiliation information "Weill Cornell Medical College" appears in affiliation.
			if (!selectingTarget) {
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					if (StringUtils.contains(StringUtils.lowerCase(article.getAffiliationConcatenated()), "weill cornell") &&
							StringUtils.contains(StringUtils.lowerCase(currentArticle.getAffiliationConcatenated()), "weill cornell")) {
						sim *= 3;
					}
				}
			}

			// Adjust cosine similarity score with year discrepancy.
			if (!selectingTarget) {
				// Update the similarity score with year discrepancy.
				int yearDiff = Integer.MAX_VALUE; // Compute difference in year between candidate article and closest year in article cluster.
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					int currentYearDiff = Math.abs(currentArticle.getJournal().getJournalIssuePubDateYear() - article.getJournal().getJournalIssuePubDateYear());
					if (currentYearDiff < yearDiff) {
						yearDiff = currentYearDiff;
					}
				}
				if (yearDiff > 40) {
					sim *= 0.001526;
				} else {
					sim = sim * YearDiscrepacyReader.getYearDiscrepancyMap().get(yearDiff);
				}
			}
			
			//// https://github.com/wcmc-its/ReCiter/issues/48
			//In phase two matching ReCiter determines which, if any, clusters are to be assigned to the target author.
			//The goal of this improvement is to leverage an author’s aliases to improve this selection process.
			//Match cluster originator against first, middle, and last name to the target author’s aliases.
			//Based on match between the target’s aliases and the names in the candidate cluster, apply scores as in the above table.
			if (!selectingTarget) {
				if(currentArticle.getAliasesList()!=null && currentArticle.getAliasesList().size()>0){
					List<IdentityDirectory> aliases = currentArticle.getAliasesList();
					List<String> firstNameList = new ArrayList<String>();
					List<String> lastNameList = new ArrayList<String>();
					List<String> middleNameList = new ArrayList<String>();
					for(IdentityDirectory dir:aliases){
						if(dir.getGivenName()!=null)firstNameList.add(dir.getGivenName().toLowerCase());
						if(dir.getMiddleName()!=null)middleNameList.add(dir.getMiddleName().toLowerCase());
						if(dir.getSurname()!=null)lastNameList.add(dir.getSurname().toLowerCase());
					}
					String fName = TargetAuthor.getInstance().getAuthorName().getFirstName();
					String mName = TargetAuthor.getInstance().getAuthorName().getMiddleName();
					String lName = TargetAuthor.getInstance().getAuthorName().getLastName();
					//if(fName!=null && firstNameList.contains(fName.toLowerCase()))sim=sim+1;
					//if(lName!=null && lastNameList.contains(lName.toLowerCase()))sim=sim+1;
					//if(mName!=null	&& middleNameList.contains(mName.toLowerCase()))sim=sim+1;
					
					for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
						for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
							if(fName!=null && author.getAuthorName().getFirstName().equalsIgnoreCase(fName))sim=sim+1;
							else sim=sim+0.2;
							if(lName!=null && author.getAuthorName().getLastName().equalsIgnoreCase(lName))sim=sim+1;							
							if(mName!=null && author.getAuthorName().getMiddleName()!=null && author.getAuthorName().getMiddleName().equalsIgnoreCase(mName))sim=sim+1;
							if(author.getAuthorName().getMiddleName()==null)sim=sim+0.5;
							if(mName!=null && author.getAuthorName().getMiddleName()!=null && !author.getAuthorName().getMiddleName().equalsIgnoreCase(mName))sim=sim+0.3;
							
							if(author.getAuthorName().getFirstName()!=null && firstNameList.contains(author.getAuthorName().getFirstName().toLowerCase()))sim=sim+1;
							else sim=sim+0.2;
							if(author.getAuthorName().getLastName()!=null && lastNameList.contains(author.getAuthorName().getLastName().toLowerCase()))sim=sim+1;							
							if(author.getAuthorName().getMiddleName()!=null && middleNameList.contains(author.getAuthorName().getMiddleName().toLowerCase()))sim=sim+1;
							if(author.getAuthorName().getMiddleName()!=null && !middleNameList.contains(author.getAuthorName().getMiddleName().toLowerCase()))sim=sim+0.3;
						}
					}
				}
			}

			// https://github.com/wcmc-its/ReCiter/issues/59
			// Context: first name is a valuable indication if a person is author for an article. 
			// It is always tracked in the rc_identity table. And it is sometimes, though not always available in the 
			// article. First names tend to change especially in cases where it becomes Westernized.

			// https://github.com/wcmc-its/ReCiter/issues/58
			// middle initial is a valuable indication if a person has the identity of author for an article. 
			// It is, sometimes, though not always, tracked in the rc_identity table. 
			// And it is sometimes, though not always available in the article.
			if (!selectingTarget) {
				if(currentArticle.getAliasesList()!=null && currentArticle.getAliasesList().size()>0){
					List<IdentityDirectory> aliases = currentArticle.getAliasesList();
					List<String> firstNameList = new ArrayList<String>();
					List<String> lastNameList = new ArrayList<String>();
					List<String> middleNameList = new ArrayList<String>();
					for(IdentityDirectory dir:aliases){
						if(dir.getGivenName()!=null)firstNameList.add(dir.getGivenName().toLowerCase());
						if(dir.getMiddleName()!=null)middleNameList.add(dir.getMiddleName().toLowerCase());
						if(dir.getSurname()!=null)lastNameList.add(dir.getSurname().toLowerCase());
					}
					String fName = TargetAuthor.getInstance().getAuthorName().getFirstName();
					String mName = TargetAuthor.getInstance().getAuthorName().getMiddleName();
					String lName = TargetAuthor.getInstance().getAuthorName().getLastName();
					//if(fName!=null && firstNameList.contains(fName.toLowerCase()))sim=sim+1;
					//if(lName!=null && lastNameList.contains(lName.toLowerCase()))sim=sim+1;
					//if(mName!=null	&& middleNameList.contains(mName.toLowerCase()))sim=sim+1;
					
					for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
						for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
							if(fName!=null && author.getAuthorName().getFirstName().equalsIgnoreCase(fName))sim=sim+1;
							else sim=sim+0.2;
							if(lName!=null && author.getAuthorName().getLastName().equalsIgnoreCase(lName))sim=sim+1;							
							if(mName!=null && author.getAuthorName().getMiddleName()!=null && author.getAuthorName().getMiddleName().equalsIgnoreCase(mName))sim=sim+1;
							if(mName==null || author.getAuthorName().getMiddleName()==null)sim=sim+0.5;
							if(mName!=null && author.getAuthorName().getMiddleName()!=null && !author.getAuthorName().getMiddleName().equalsIgnoreCase(mName))sim=sim+0.3;
							
						}
					}
				}
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					String targetAuthorMiddleInitial = TargetAuthor.getInstance().getAuthorName().getMiddleInitial();
					String firstName = TargetAuthor.getInstance().getAuthorName().getFirstName();

					// First Name from rc_identity.
					if (firstName != null) {
						// For cases where first name is present in rc_identity.
						for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
							if (author.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
								if (firstName.equalsIgnoreCase((author.getAuthorName().getFirstName()))) {
									for (ReCiterAuthor currentArticleAuthor : currentArticle.getArticleCoAuthors().getCoAuthors()) {
										if (currentArticleAuthor.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
											if (firstName.equalsIgnoreCase(currentArticleAuthor.getAuthorName().getFirstName())) {
												sim *= 1.3; // (rc_idenity = YES, present in cluster = YES, match = YES.
											} else {
												sim *= 0.4; // (rc_idenity = YES, present in cluster = YES, match = NO.
											}
										}
									}
								}
							}
						}
					}

					// Middle initial from rc_identity.
					if (targetAuthorMiddleInitial != null) {
						// For cases where middle initial is present in rc_identity.
						for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
							if (author.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
								if (targetAuthorMiddleInitial.equalsIgnoreCase((author.getAuthorName().getMiddleInitial()))) {
									for (ReCiterAuthor currentArticleAuthor : currentArticle.getArticleCoAuthors().getCoAuthors()) {
										if (currentArticleAuthor.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
											if (targetAuthorMiddleInitial.equalsIgnoreCase(currentArticleAuthor.getAuthorName().getMiddleInitial())) {
												sim *= 1.3;
											} else {
												sim *= 0.3; // the likelihood that someone wrote an article should plummet when this is the case
											}
										}
									}
								}
							}
						}
					} else {
						// For cases where middle initial is not present in rc_identity.
						for (ReCiterAuthor author : article.getArticleCoAuthors().getCoAuthors()) {
							if (author.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
								for (ReCiterAuthor currentArticleAuthor : currentArticle.getArticleCoAuthors().getCoAuthors()) {
									if (currentArticleAuthor.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
										if (currentArticleAuthor.getAuthorName().getMiddleInitial() == null &&
												author.getAuthorName().getMiddleInitial() != null) {
											// it's rare but not completely improbable that an author would share their 
											// middle initial on a paper but won't supply it on an official CV, or so I would argue
											sim *= 0.7;
										}
									}
								}
							}
						}
					}
				}
			}

			//			if (!selectingTarget) {
			//				JournalDao journalDao = new JournalDao();
			//				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
			//					// Use the cluster starter to compare journal similarity.
			//					if (article.isClusterStarter()) {
			//						if (article.getJournal() != null && currentArticle.getJournal() != null) {
			//							double journalSimScore = journalDao.getJournalSimilarity(
			//									article.getJournal().getIsoAbbreviation(),
			//									currentArticle.getJournal().getIsoAbbreviation());
			//							// Check similarity both ways.
			//							if (journalSimScore == -1.0) {
			//								journalSimScore = journalDao.getJournalSimilarity(
			//										currentArticle.getJournal().getIsoAbbreviation(),
			//										article.getJournal().getIsoAbbreviation());
			//							}
			//							if (journalSimScore != -1.0) {
			//								if (journalSimScore > 0.8) {
			//									sim *= (1 + journalSimScore); // Journal similarity on a sliding scale.
			//								}
			//							}
			//						}
			//					}
			//				}
			//			}

			if (selectingTarget) {

				// Update the similarity score with year discrepancy.
				int yearDiff = Integer.MAX_VALUE; // Compute difference in year between candidate article and closest year in article cluster.
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					int currentYearDiff = article.getJournal().getJournalIssuePubDateYear() - TargetAuthor.getInstance().getTerminalDegreeYear();
					if (currentYearDiff < yearDiff) {
						yearDiff = currentYearDiff;
					}
				}
				// 7 years before terminal degree >> 0.3
				if (yearDiff < -7) {
					sim *= 0.3;
				
				// 0 - 7 years before terminal degree >> 0.75
				} else if (yearDiff <= 0 && yearDiff >= -7) {
					sim *= 0.75;
				}
				
				// after terminal degree >> 1.0 (don't change sim score).
				
			} else if (sim > similarityThreshold && sim > currentMax) {
				currentArticle.setInfo("Max Id: + " + currentMaxId + " sim: " + sim);
				currentMaxId = id;
				currentMax = sim;
				// TODO: what happens if cosine similarity is tied?
			}
		}
		return currentMaxId; // found a cluster.
	}

	/**
	 * 
	 * @param currentArticle
	 * @param targetAuthor
	 * @return
	 */
	// computes coauthor matches of this article with all current clusters.
	private Map<Integer, Integer> computeCoauthorMatch(ReCiterArticle currentArticle) {
		Map<Integer, Integer> coauthorsCount = new HashMap<Integer, Integer>(); // ClusterId to number of coauthors.
		for (ReCiterCluster reCiterCluster : finalCluster.values()) {
			int matchingCoauthors = reCiterCluster.getMatchingCoauthorCount(currentArticle);
			coauthorsCount.put(reCiterCluster.getClusterID(), matchingCoauthors);
		}
		return coauthorsCount;
	}

	/**
	 * 
	 * @param map
	 * @return
	 */
	// helper function to find keys in map data structure with max values.
	private Set<Integer> getKeysWithMaxVal(Map<Integer, Integer> map) {
		Set<Integer> keyList = new HashSet<Integer>();
		int maxValueInMap=(Collections.max(map.values()));  // This will return max value in the Hashmap
		//		System.out.println("Max value: " + maxValueInMap);
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			if (entry.getValue() == maxValueInMap && maxValueInMap != 0) {
				keyList.add(entry.getKey());
			}
		}
		return keyList;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Number of clusters formed: " + getFinalCluster().size() + "\n");

		for (ReCiterCluster r : finalCluster.values()) {
			sb.append("{");
			sb.append(r.getClusterID());
			sb.append(" (size of cluster=");
			sb.append(r.getArticleCluster().size());
			sb.append("): ");
			for (ReCiterArticle a : r.getArticleCluster()) {
				sb.append(a.getArticleID());
				sb.append(", ");
			}
			sb.append("}\n");
		}
		return sb.toString();
	}

	@Override
	public double getArticleToArticleSimilarityThresholdValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getSelectedReCiterClusterId() {
		return selectedReCiterClusterId;
	}

	public void setSelectedReCiterClusterId(int selectedReCiterClusterId) {
		this.selectedReCiterClusterId = selectedReCiterClusterId;
	}


}
