package reciter.algorithm.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.tfidf.Document;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorEducation;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.model.boardcertifications.ReadBoardCertifications;
import reciter.service.TargetAuthorService;
import reciter.service.impl.TargetAuthorServiceImpl;
import reciter.utils.reader.YearDiscrepacyReader;
import xmlparser.pubmed.model.MedlineCitationMeshHeadingDescriptorName;
import xmlparser.scopus.model.Author;
import xmlparser.scopus.model.ScopusArticle;
import database.dao.IdentityDao;
import database.dao.impl.IdentityCitizenshipDao;
import database.dao.impl.IdentityDaoImpl;
import database.dao.impl.IdentityEducationDaoImpl;
import database.dao.impl.MatchingDepartmentsJournalsDao;
import database.model.Identity;

public class ReCiterClusterer implements Clusterer {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);	
	private Map<Integer, ReCiterCluster> finalCluster = new HashMap<Integer, ReCiterCluster>();
	private boolean selectingTarget = false;
	private int selectedReCiterClusterId = -1;
	private double similarityThreshold = 0.3;
	private TargetAuthor targetAuthor;

	public ReCiterClusterer() {
		ReCiterCluster.getClusterIDCounter().set(0); // reset counter on cluster id.
	}

	public ReCiterClusterer(String cwid) {
		ReCiterCluster.getClusterIDCounter().set(0); // reset counter on cluster id.
		TargetAuthorService targetAuthorService= new TargetAuthorServiceImpl();
		setTargetAuthor(targetAuthorService.getTargetAuthor(cwid));
	}

	public Map<Integer, ReCiterCluster> getFinalCluster() {
		return finalCluster;
	}

	public int assignTargetToCluster(ReCiterArticle article) {
		selectingTarget = true;
		selectedReCiterClusterId = selectCandidateCluster(article);
		return selectedReCiterClusterId;
	}

	public double similarity(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {

		return 0;
	}

	public void cluster(List<ReCiterArticle> reCiterArticleList, TargetAuthor targetAuthor) {

		double maxSimilarityScore = -1;
		for (ReCiterArticle reCiterArticle : reCiterArticleList) {
			// compute similarity between reCiterArticle and targetAuthor.

			double similarityScore = similarity(reCiterArticle, targetAuthor);
			if (similarityScore > maxSimilarityScore) {
				maxSimilarityScore = similarityScore;
			}
		}
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

		ReCiterCluster firstCluster = new ReCiterCluster();
		firstCluster.setClusterOriginator(first.getArticleId());
		firstCluster.add(first);
		finalCluster.put(firstCluster.getClusterID(), firstCluster);

		for (int i = 1; i < reciterArticleList.size(); i++) {
			ReCiterArticle article = reciterArticleList.get(i);
			int selection = selectCandidateCluster(article);
			if (selection == -1) {
				// create its own cluster.
				ReCiterCluster newCluster = new ReCiterCluster();
				newCluster.setClusterOriginator(article.getArticleId());
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
	 * Computes the MeSH major overlap between a cluster and an article.
	 * 
	 * For example, here are the MeSH major terms for this article (21421525) has these MeSH terms:
	 * 
	 * Brachytherapy
	 * Breast Neoplasms
	 * Carcinoma, Ductal, Breast
	 * Scleroderma
	 * 
	 * Drop the subheadings, e.g., "methods" in "Humans/methods"
	 * 
	 * Compute the percent overlap between candidate article and cluster
	 * For example, suppose Cluster A has 50 MeSH major terms and Article B has 4 MeSH major terms. 
	 * They have one term in common. Then the overlap is 25%.
	 * 
	 * @param cluster Cluster of articles.
	 * @param article Article being compared.
	 * @return Percentage of overlap between a cluster and an article.
	 */
	public double computeMeshMajorOverlap(List<ReCiterArticle> cluster, ReCiterArticle article) {

		Set<String> clusterMeshTerms = new HashSet<String>();
		for (ReCiterArticle reCiterArticle : cluster) {
			// Collect the MeSH major terms from each article.
			clusterMeshTerms.addAll(reCiterArticle.getMeshList());
		}

		// Compute MeSH major overlap.
		Set<String> intersection = new HashSet<String>(clusterMeshTerms);
		intersection.retainAll(article.getMeshList());

		return intersection.size() / article.getMeshList().size();
	}

	/**
	 * If a candidate article is published in a journal and the cluster contains that journal, increase the score for a match.
	 * (https://github.com/wcmc-its/ReCiter/issues/83).
	 * 
	 * @param cluster
	 * @param article
	 * @return the number of matching journal titles between the articles in the cluster and the article.
	 * 
	 */
	public int computeNumberMatchingJournals(List<ReCiterArticle> cluster, ReCiterArticle article) {
		int numberMatchingJournals = 0;
		for (ReCiterArticle reCiterArticle : cluster) {
			if (reCiterArticle.getJournal().getJournalTitle().equalsIgnoreCase(article.getJournal().getJournalTitle())) {
				numberMatchingJournals++;
			}
		}
		return numberMatchingJournals;
	}

	public double computeYearDiscrepancy(List<ReCiterArticle> cluster, ReCiterArticle article) {
		int yearDiff = Integer.MAX_VALUE; // Compute difference in year between candidate article and closest year in article cluster.
		for (ReCiterArticle reCiterArticle : cluster) {
			int currentYearDiff = Math.abs(reCiterArticle.getJournal().getJournalIssuePubDateYear() - article.getJournal().getJournalIssuePubDateYear());
			if (currentYearDiff < yearDiff) {
				yearDiff = currentYearDiff;
			}
		}
		if (yearDiff > 40) {
			return 0.001526;
		} else {
			return YearDiscrepacyReader.getYearDiscrepancyMap().get(yearDiff);
		}
	}

	//	public boolean computeStringMatchingByKeyword(List<ReCiterArticle> cluster, ReCiterArticle article, String keyword) {
	//		for (ReCiterArticle reCiterArticle : cluster) {
	//			if (StringUtils.contains(StringUtils.lowerCase(article.getAffiliationConcatenated()), keyword) &&
	//					StringUtils.contains(StringUtils.lowerCase(reCiterArticle.getAffiliationConcatenated()), keyword)) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

	/**
	 * Create a map of list of documents for each ReCiterArticle.
	 * @param reCiterArticleList
	 * @param targetAuthor
	 * @return
	 */
	public Map<Integer, List<Document>> createDocuments(List<ReCiterArticle> reCiterArticleList, TargetAuthor targetAuthor) {
		Map<Integer, List<Document>> map = new HashMap<Integer, List<Document>>();
		for (ReCiterArticle reCiterArticle : reCiterArticleList) {
			ScopusArticle scopusArticle = reCiterArticle.getScopusArticle();
			// Get Author affiliation from Scopus by matching author names.
			List<Document> affiliationList = new ArrayList<Document>();

			for (Entry<Long, Author> entry : scopusArticle.getAuthors().entrySet()) {
				Author author = entry.getValue();
				if (StringUtils.equalsIgnoreCase(author.getSurname(), targetAuthor.getAuthorName().getLastName())) {
					Set<Integer> afidSet = author.getAfidSet();

					for (int afid : afidSet) {
						// an author can have multiple affiliations from Scopus, which one to pick?
						// current solution: adding all the documents and selecting one with the high similarity.
						Document document = new Document(scopusArticle.getAffiliationMap().get(afid).getAffilname());
						document.setId(reCiterArticle.getArticleId());
						affiliationList.add(document);
					}
				}
			}
			map.put(reCiterArticle.getArticleId(), affiliationList);
		}
		return map;
	}

	/**
	 * Use citizenship and educational background to improve recall.
	 * (https://github.com/wcmc-its/ReCiter/issues/78).
	 */
	public double computeArticleAffiliationToAuthurEducation(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {

		ScopusArticle scopusArticle = reCiterArticle.getScopusArticle();
		if (scopusArticle != null) {
			// Get scopus affiliation.
			String scopusAffiliation = "";
			AuthorEducation targetAuthorEducation = targetAuthor.getEducation();

			// Compute Similarity.

		}
		return 0;
	}

	public double computeClusterToTargetSimilarity(List<ReCiterArticle> cluster, ReCiterArticle article) {
		return 0;
	}

	/**
	 * Extract Department information from string of the form "Department of *,".
	 * 
	 * @param department Department string
	 * @return Department name.
	 */
	private String extractDepartment(String department) {
		final Pattern pattern = Pattern.compile("Department of (.+?),");
		final Matcher matcher = pattern.matcher(department);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return "";
		}
	}

	/**
	 * Leverage departmental affiliation string matching for phase two matching.
	 * 
	 * If reCiterAuthor has department information, extract the "department of ***" string and use string comparison
	 * to match to target author's primary department and other department fields. If both party's department match,
	 * return true, else return false.
	 * 
	 * (Github issue: https://github.com/wcmc-its/ReCiter/issues/79)
	 * @return True if the department of the ReCiterAuthor and TargetAuthor match.
	 */
	public boolean departmentMatch(ReCiterAuthor reCiterAuthor, TargetAuthor targetAuthor) {

		if (reCiterAuthor.getAffiliation() != null) {
			String affiliation = reCiterAuthor.getAffiliation().getAffiliationName();
			String extractedDept = extractDepartment(affiliation);
			String targetAuthorDept = targetAuthor.getDepartment();
			String targetAuthorOtherDept = targetAuthor.getOtherDeparment();
			if (extractedDept.equalsIgnoreCase(targetAuthorDept) || extractedDept.equalsIgnoreCase(targetAuthorOtherDept)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * 
	 * @param journalIsoAbbr
	 * @param targetAuthorDeptName
	 * @return
	 */
	private double getDepartmentJournalSimilarityScore(String journalIsoAbbr, String targetAuthorDeptName) {
		MatchingDepartmentsJournalsDao matchingDepartmentsJournalsDao = new MatchingDepartmentsJournalsDao();
		return matchingDepartmentsJournalsDao.getScoreByJournalAndDepartment(journalIsoAbbr, targetAuthorDeptName);
	}
	
	/**
	 * Gets the pre-calculated value of article journal to target author's department for Phase II matching.
	 * 
	 * (Github issue: https://github.com/wcmc-its/ReCiter/issues/60).
	 * @return
	 */
	public double getDepartmentJournalSimilarityScore(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		
		if (reCiterArticle.getJournal() != null) {
			String journalIsoAbbr = reCiterArticle.getJournal().getIsoAbbreviation();
			String targetAuthorDeptName = targetAuthor.getDepartment();
			return getDepartmentJournalSimilarityScore(journalIsoAbbr, targetAuthorDeptName);
		}
		return 0;
	}
	
	/**
	 * Leverage data on board certifications to improve Phase II matching.
	 * 
	 * (Github issue: https://github.com/wcmc-its/ReCiter/issues/45).
	 */
	public double getBoardCertificationScore() {
		return 0;
	}
	
	/**
	 * (Phase I clustering).
	 * If a candidate article is published in a journal and another article contains that journal, return true. False
	 * otherwise.
	 * 
	 * Github issue: https://github.com/wcmc-its/ReCiter/issues/83
	 */
	public boolean isJournalMatch(ReCiterArticle article, ReCiterArticle articleInCluster) {
		
		if (article.getJournal() != null && articleInCluster.getJournal() != null) {
			return article.getJournal().getJournalTitle().equalsIgnoreCase(articleInCluster.getJournal().getJournalTitle());
		}
		return false;
	}
	
	/**
	 * 
	 */
	
	// https://github.com/wcmc-its/ReCiter/issues/59
				// Context: first name is a valuable indication if a person is author for an article. 
				// It is always tracked in the rc_identity table. And it is sometimes, though not always available in the 
				// article. First names tend to change especially in cases where it becomes Westernized.

				// https://github.com/wcmc-its/ReCiter/issues/58
				// middle initial is a valuable indication if a person has the identity of author for an article. 
				// It is, sometimes, though not always, tracked in the rc_identity table. 
				// And it is sometimes, though not always available in the article.
				if (!selectingTarget) {
					for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
						String targetAuthorMiddleInitial = TargetAuthor.getInstance().getAuthorName().getMiddleInitial();
						String firstName = TargetAuthor.getInstance().getAuthorName().getFirstName();

						// First Name from rc_identity.
						if (firstName != null) {
							// For cases where first name is present in rc_identity.
							for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
								if (author.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
									if (firstName.equalsIgnoreCase((author.getAuthorName().getFirstName()))) {
										for (ReCiterAuthor currentArticleAuthor : currentArticle.getArticleCoAuthors().getAuthors()) {
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
							for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
								if (author.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
									if (targetAuthorMiddleInitial.equalsIgnoreCase((author.getAuthorName().getMiddleInitial()))) {
										for (ReCiterAuthor currentArticleAuthor : currentArticle.getArticleCoAuthors().getAuthors()) {
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
							for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
								if (author.getAuthorName().firstInitialLastNameMatch(TargetAuthor.getInstance().getAuthorName())) {
									for (ReCiterAuthor currentArticleAuthor : currentArticle.getArticleCoAuthors().getAuthors()) {
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
	
	/**
	 * 
	 * @param clusterIdList
	 * @param currentArticle
	 * @return
	 */
	private int getIdWithMostContentSimilarity(Set<Integer> clusterIdList, ReCiterArticle currentArticle) {
		double currentMax = -1;
		int currentMaxId = -1;
		String cwid = targetAuthor.getCwid();
		for (int id : clusterIdList) {

			double sim = finalCluster.get(id).contentSimilarity(currentArticle); // cosine similarity score.

			// Github issue: https://github.com/wcmc-its/ReCiter/issues/78 (Phase 2 clustering)
			// We have two sources for knowing whether someone lived or worked outside of the United States: 
			// rc_identity_citizenship and rc_identity_education (foreign countries are in parentheses there).
			if (selectingTarget) {				
				IdentityCitizenshipDao identityCitizenshipDao = new IdentityCitizenshipDao();
				IdentityEducationDaoImpl identityEducationDao = new IdentityEducationDaoImpl();
				List<String> citizenship = identityCitizenshipDao.getIdentityCitizenshipCountry(cwid);
				List<String> education = identityEducationDao.getIdentityCitizenshipEducation(cwid);
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					for (ReCiterAuthor coauthor : article.getArticleCoAuthors().getAuthors()) {
						// please skip the coauthor which is the targetAuthor by comparing the first name,
						// middle name, and last name.
						// if (coauthor.getAffiliation().equals(citizenship) || coauthor.getAffiliation().equals(education)) {
						if(coauthor.getAffiliation()!=null){
							String coAuthorAffiliation = coauthor.getAffiliation().getAffiliationName();
							if(citizenship.contains(coAuthorAffiliation) || education.contains(coAuthorAffiliation)){						
								// increase sim score.
								sim = sim + 1; 
							}
						}
					}
				}

			}

			/* Improve score in cases where MeSH major terms match between cluster and target article #82 */ 
			/* https://github.com/wcmc-its/ReCiter/issues/l */ 

			if (!selectingTarget) {
				MedlineCitationMeshHeadingDescriptorName meshName = new MedlineCitationMeshHeadingDescriptorName();
				String meshTermValue = meshName.getDescriptorNameString();
				//System.out.println(meshTermValue);
				if(meshTermValue!=null){
					String[] meshTerms = meshTermValue.split(" "); 
					String getTargetAuthorTitle = targetAuthor.getTargetAuthorArticleIndexed().getArticleTitle().getTitle();
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


			//
			//			// Github issue: https://github.com/wcmc-its/ReCiter/issues/49
			//			// Leverage known co-investigators on grants to improve phase two matching.
			if (selectingTarget) {
				IdentityDao identityDao = new IdentityDaoImpl(); 
				List<Identity> identityList = identityDao.getAssosiatedGrantIdentityList(TargetAuthor.getInstance().getCwid());

				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {			
					for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
						for(Identity identity: identityList){
							for (ReCiterAuthor currentArticleCoAuthor : currentArticle.getArticleCoAuthors().getAuthors()) {
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
				sb.append(a.getArticleId());
				sb.append(", ");
			}
			sb.append("}\n");
		}
		return sb.toString();
	}

	public int getSelectedReCiterClusterId() {
		return selectedReCiterClusterId;
	}

	public void setSelectedReCiterClusterId(int selectedReCiterClusterId) {
		this.selectedReCiterClusterId = selectedReCiterClusterId;
	}

	public TargetAuthor getTargetAuthor() {
		return targetAuthor;
	}

	public void setTargetAuthor(TargetAuthor targetAuthor) {
		this.targetAuthor = targetAuthor;
	}


}
