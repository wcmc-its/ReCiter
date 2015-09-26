package reciter.algorithm.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import database.dao.CountryDao;
import database.dao.IdentityCitizenshipDao;
import database.dao.IdentityDao;
import database.dao.IdentityDegreeDao;
import database.dao.impl.CountryDaoImpl;
import database.dao.impl.IdentityCitizenshipDaoImpl;
import database.dao.impl.IdentityDaoImpl;
import database.dao.impl.IdentityDegreeDaoImpl;
import database.dao.impl.MatchingDepartmentsJournalsDao;
import database.model.Identity;
import database.model.IdentityDegree;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.tfidf.Document;
import reciter.algorithm.tfidf.TfIdf;
import reciter.erroranalysis.Analysis;
import reciter.erroranalysis.AnalysisReCiterCluster;
import reciter.model.article.ReCiterArticle;
import reciter.model.author.AuthorAffiliation;
import reciter.model.author.AuthorDegree;
import reciter.model.author.AuthorEducation;
import reciter.model.author.AuthorName;
import reciter.model.author.ReCiterAuthor;
import reciter.model.author.TargetAuthor;
import reciter.service.converters.IdentityDegreeConverter;
import reciter.utils.reader.YearDiscrepacyReader;
import xmlparser.scopus.model.Affiliation;
import xmlparser.scopus.model.Author;
import xmlparser.scopus.model.ScopusArticle;

public class ReCiterClusterer implements Clusterer {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);	
	private Map<Integer, ReCiterCluster> finalCluster = new HashMap<Integer, ReCiterCluster>();
	private Set<Integer> selectedClusterIdSet = new HashSet<Integer>();
	private int selectedReCiterClusterId = -1;
	private double similarityThresholdForPubMedAffiliation = 0.75;
	private TargetAuthor targetAuthor;

	public ReCiterClusterer() {
		ReCiterCluster.getClusterIDCounter().set(0); // reset counter on cluster id.
	}

	public ReCiterClusterer(String cwid) {
		ReCiterCluster.getClusterIDCounter().set(0); // reset counter on cluster id.
		//		TargetAuthorService targetAuthorService= new TargetAuthorServiceImpl();
		//		setTargetAuthor(targetAuthorService.getTargetAuthor(cwid));
		targetAuthor = getTargetAuthor(cwid);
	}

	public Map<Integer, ReCiterCluster> getFinalCluster() {
		return finalCluster;
	}

	private TargetAuthor getAuthorFromIdentity(Identity identity) {

		TargetAuthor targetAuthor = new TargetAuthor(
				new AuthorName(identity.getFirstName(), identity.getMiddleName(), identity.getLastName()),
				new AuthorAffiliation(identity.getPrimaryAffiliation()));

		targetAuthor.setCwid(identity.getCwid());
		targetAuthor.setDepartment(identity.getPrimaryDepartment());
		targetAuthor.setOtherDeparment(identity.getOtherDepartment());

		targetAuthor.setEducation(new AuthorEducation());

		IdentityCitizenshipDao identityCitizenshipDao = new IdentityCitizenshipDaoImpl();
		String countryOfCitizenship = identityCitizenshipDao.getIdentityCitizenshipCountry(identity.getCwid());
		if (countryOfCitizenship != null) {
			targetAuthor.setCitizenship(countryOfCitizenship);
		}
		IdentityDegreeDao identityDegreeDao = new IdentityDegreeDaoImpl();
		IdentityDegree identityDegree = identityDegreeDao.getIdentityDegreeByCwid(identity.getCwid());
		AuthorDegree authorDegree = IdentityDegreeConverter.convert(identityDegree);
		targetAuthor.setDegree(authorDegree);
		return targetAuthor;
	}

	private TargetAuthor getTargetAuthor(String cwid) {
		IdentityDao identityDao = new IdentityDaoImpl();
		Identity identity = identityDao.getIdentityByCwid(cwid);

		return getAuthorFromIdentity(identity);
	}

	/**
	 * 
	 * @param articleList
	 * @param targetAuthor
	 */
	@Override
	public Analysis cluster(List<ReCiterArticle> reciterArticleList) {

		slf4jLogger.info("Number of articles to be clustered: " + reciterArticleList.size());

		ReCiterArticle first = null;
		if (reciterArticleList != null && reciterArticleList.size() > 0) {
			first = reciterArticleList.get(0);
		} else {
			return null;
		}

		ReCiterCluster firstCluster = new ReCiterCluster();
		firstCluster.setClusterOriginator(first.getArticleId());
		firstCluster.add(first);
		finalCluster.put(firstCluster.getClusterID(), firstCluster);

		for (int i = 1; i < reciterArticleList.size(); i++) {
			ReCiterArticle article = reciterArticleList.get(i);
			int selection = selectCandidateCluster(article, targetAuthor);
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

		// Phase 2 matching.
		Map<Integer, Integer> map = computeClusterSelectionForTarget();
		slf4jLogger.debug(map.toString());

		// Perform PubMed affiliation reassignment.
//		reAssignArticlesByPubmedAffiliation(map);

		// Perform Scopus affiliation reassignment.
		reAssignArticlesByScopusAffiliation(map);

		// Perform coauthor reassignment.
		reAssignArticlesByCoauthorMatch(map);

		// Perform journal reassignment.
		reAssignArticlesByJournalMatch(map);

		// Perform citizenship reassignment.
		reAssignArticlesByCitizenshipMatch(map);

		// Perform pubmed cosine similarity.
		reAssignArticlesByPubmedAffiliationCosineSimilarity(map);
		
		// Perform year discrepancy removals.
		removeArticlesBasedOnYearDiscrepancy(map);

		// Set selected cluster.
		setSelectedClusterIdSet(map.keySet());

		AnalysisReCiterCluster analyisReCiterCluster = new AnalysisReCiterCluster();
		Map<String, Integer> authorCount = analyisReCiterCluster.getTargetAuthorNameCounts(reciterArticleList, targetAuthor);
		for (Entry<String, Integer> entry : authorCount.entrySet()) {
			slf4jLogger.info(entry.getKey() + ": " + entry.getValue());
		}
		slf4jLogger.info("Number of different author names: " + authorCount.size());

		// Analysis to get Precision and Recall.
		return Analysis.performAnalysis(this);
	}

	/**
	 * Determine whether two reCiterArticles contain mutual authors.
	 * @param reCiterArticle
	 * @param targetAuthor
	 * @return
	 */
	public boolean containsMutualCoauthors(ReCiterArticle reCiterArticleA, ReCiterArticle reCiterArticleB) {

		for (ReCiterAuthor authorA : reCiterArticleA.getArticleCoAuthors().getAuthors()) {
			for (ReCiterAuthor authorB : reCiterArticleB.getArticleCoAuthors().getAuthors()) {

				// do not match target author's name
				if (!authorA.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName()) &&
						!authorB.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName())) {

					if (authorA.getAuthorName().isFullNameMatch(authorB.getAuthorName())) {
						slf4jLogger.info(authorA.getAuthorName().toString());
						return true;
					}
				}

			}
		}
		return false;
	}


	/**
	 * Reassign articles not selected by matching co-authors.
	 * @param selectedClusterIds
	 */
	public void reAssignArticlesByCoauthorMatch(Map<Integer, Integer> selectedClusterIds) {
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds.keySet()) {
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.keySet().contains(entry.getKey())) {
					for (ReCiterArticle reCiterArticle : finalCluster.get(clusterId).getArticleCluster()) {

						// Iterate through the remaining final cluster that are not selected in selectedClusterIds.

						Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
						while (iterator.hasNext()) {
							ReCiterArticle otherReCiterArticle = iterator.next();

							// contains matching co-authors.
							boolean containsMutualCoauthors = containsMutualCoauthors(reCiterArticle, otherReCiterArticle);
							if (containsMutualCoauthors) {
								if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
									clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
								} else {
									List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
									articleList.add(otherReCiterArticle);
									clusterIdToReCiterArticleList.put(clusterId, articleList);
								}
								// remove from old cluster.
								iterator.remove();
							}
						}
					}
				}
			}
		}

		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				finalCluster.get(entry.getKey()).add(article);
			}
		}
	}

	/**
	 * 				// contains weill cornell. TODO: make sure target author's name matches the one iterated thru.
				boolean containsWeillCornell = containsWeillCornell(reCiterArticle);
				if (containsWeillCornell) {
					slf4jLogger.info("Weill Cornell Match=" + reCiterArticle.getArticleId() + " cluster id=" + entry.getKey());
					if (clusterIds.containsKey(entry.getKey())) {
						int currentCount = clusterIds.get(entry.getKey());
						clusterIds.put(entry.getKey(), ++currentCount);
					} else {
						clusterIds.put(entry.getKey(), 1);
					}
				}
	 * @param selectedClusterIds
	 */

	public void removeArticlesBasedOnYearDiscrepancy(Map<Integer, Integer> selectedClusterIds) {
		// Map of integer (cluster to be added to) and reciterarticles.
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds.keySet()) {
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				// IMPORTANT: Here we actually iterate through the selected cluster ids's articles.
				if (selectedClusterIds.keySet().contains(entry.getKey())) {

					// Iterate through the articles selected in selectedClusterIds.
					Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
					while (iterator.hasNext()) {
						ReCiterArticle otherReCiterArticle = iterator.next();

						// check year discrepancy for both doctoal and bachelors degrees.
						int yearDiscrepancyDoctoral = 
								computeYearDiscrepancyDoctoral(otherReCiterArticle, targetAuthor);

						int yearDiscrepancyBachelors = 
								computeYearDiscrepancyBachelors(otherReCiterArticle, targetAuthor);

						if (yearDiscrepancyDoctoral < -5 || yearDiscrepancyBachelors < 1) {
							if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
								clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
							} else {
								List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
								articleList.add(otherReCiterArticle);
								clusterIdToReCiterArticleList.put(clusterId, articleList);
							}
							// remove from old cluster.
							iterator.remove();
						}
					}
				}
			}
		}

		// Create a new cluster to store the deleted articles.
		ReCiterCluster reCiterCluster = new ReCiterCluster();
		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				reCiterCluster.add(article);	
				slf4jLogger.info("Removed article id=" + article.getArticleId());
			}
		}
		finalCluster.put(reCiterCluster.getClusterID(), reCiterCluster);
	}

	public void reAssignArticlesByScopusAffiliation(Map<Integer, Integer> selectedClusterIds) {

		// Map of integer (cluster to be added to) and reciterarticles.
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds.keySet()) {
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.keySet().contains(entry.getKey())) {

					// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
					Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
					while (iterator.hasNext()) {
						ReCiterArticle otherReCiterArticle = iterator.next();

						// check Scopus affiliation.
						boolean containsWeillCornellFromScopus = 
								containsWeillCornellFromScopus(otherReCiterArticle.getScopusArticle(), targetAuthor);

						if (containsWeillCornellFromScopus) {
							for (ReCiterAuthor reCiterAuthor : otherReCiterArticle.getArticleCoAuthors().getAuthors()) {

								boolean isFirstNameMatch = StringUtils.equalsIgnoreCase(
										reCiterAuthor.getAuthorName().getFirstInitial(), targetAuthor.getAuthorName().getFirstInitial());

								if (isFirstNameMatch) {
									if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
										clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
									} else {
										List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
										articleList.add(otherReCiterArticle);
										clusterIdToReCiterArticleList.put(clusterId, articleList);
									}

									// remove from old cluster.
									iterator.remove();
									break; // break loop iterating over authors.
								}
							}
						}
					}
				}
			}
		}

		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				finalCluster.get(entry.getKey()).add(article);
			}
		}
	}

	public void reAssignArticlesByPubmedAffiliationCosineSimilarity(Map<Integer, Integer> selectedClusterIds) {
		// Map of integer (cluster to be added to) and reciterarticles.
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds.keySet()) {

			for (ReCiterArticle reCiterArticle : finalCluster.get(clusterId).getArticleCluster()) {
				for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
					// Do not iterate through the selected cluster ids's articles.
					if (!selectedClusterIds.keySet().contains(entry.getKey())) {
						// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
						Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
						while (iterator.hasNext()) {
							ReCiterArticle otherReCiterArticle = iterator.next();

							double sim = computeCosineSimilarity(reCiterArticle, otherReCiterArticle);
							if (sim > similarityThresholdForPubMedAffiliation) {
								if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
									clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
								} else {
									List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
									articleList.add(otherReCiterArticle);
									clusterIdToReCiterArticleList.put(clusterId, articleList);
								}

								// remove from old cluster.
								iterator.remove();
								break; // break loop iterating over authors.
							}
						}
					}
				}
			}
		}

		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				finalCluster.get(entry.getKey()).add(article);
			}
		}
	}

	public void reAssignArticlesByPubmedAffiliation(Map<Integer, Integer> selectedClusterIds) {

		// Map of integer (cluster to be added to) and reciterarticles.
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds.keySet()) {
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.keySet().contains(entry.getKey())) {
					// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
					Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
					while (iterator.hasNext()) {
						ReCiterArticle otherReCiterArticle = iterator.next();

						// contains weill cornell. TODO: make sure target author's name matches the one iterated thru.
						boolean containsWeillCornell = containsWeillCornell(otherReCiterArticle);

						if (containsWeillCornell) {
							for (ReCiterAuthor reCiterAuthor : otherReCiterArticle.getArticleCoAuthors().getAuthors()) {

								boolean isFirstNameMatch = StringUtils.equalsIgnoreCase(
										reCiterAuthor.getAuthorName().getFirstInitial(), targetAuthor.getAuthorName().getFirstInitial());

								if (isFirstNameMatch) {
									if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
										clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
									} else {
										List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
										articleList.add(otherReCiterArticle);
										clusterIdToReCiterArticleList.put(clusterId, articleList);
									}

									// remove from old cluster.
									iterator.remove();
									break; // break loop iterating over authors.
								}
							}
						}
					}
				}
			}
		}

		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				finalCluster.get(entry.getKey()).add(article);
			}
		}
	}
	/**
	 * Reassign articles not selected by matching journals.
	 * @param selectedClusterIds
	 */
	public void reAssignArticlesByJournalMatch(Map<Integer, Integer> selectedClusterIds) {

		// Map of integer (cluster to be added to) and reciterarticles.
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds.keySet()) {
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.keySet().contains(entry.getKey())) {
					for (ReCiterArticle reCiterArticle : finalCluster.get(clusterId).getArticleCluster()) {

						// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
						Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
						while (iterator.hasNext()) {
							ReCiterArticle otherReCiterArticle = iterator.next();

							boolean isJournalMatch = isJournalMatch(reCiterArticle, otherReCiterArticle);

							if (isJournalMatch) {
								// contains matching journals.
								for (ReCiterAuthor reCiterAuthor : otherReCiterArticle.getArticleCoAuthors().getAuthors()) {

									boolean isFirstNameMatch = StringUtils.equalsIgnoreCase(
											reCiterAuthor.getAuthorName().getFirstName(), targetAuthor.getAuthorName().getFirstName());

									if (isFirstNameMatch) {
										if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
											clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
										} else {
											List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
											articleList.add(otherReCiterArticle);
											clusterIdToReCiterArticleList.put(clusterId, articleList);
										}

										// remove from old cluster.
										iterator.remove();
										break; // break loop iterating over authors.
									}
								}
							}
						}
					}
				}
			}
		}

		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				finalCluster.get(entry.getKey()).add(article);
			}
		}
	}

	/**
	 * Reassign articles not selected by matching journals.
	 * @param selectedClusterIds
	 */
	public void reAssignArticlesByCitizenshipMatch(Map<Integer, Integer> selectedClusterIds) {

		// Map of integer (cluster to be added to) and reciterarticles.
		Map<Integer, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Integer, List<ReCiterArticle>>();

		for (int clusterId : selectedClusterIds.keySet()) {
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.keySet().contains(entry.getKey())) {
					// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
					Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
					while (iterator.hasNext()) {
						ReCiterArticle otherReCiterArticle = iterator.next();

						boolean containsCitizenshipFromScopus = containsCitizenshipFromScopus(otherReCiterArticle.getScopusArticle(), targetAuthor);
						boolean isCitizenshipMatchFromPubmed = false;
						for (ReCiterAuthor reCiterAuthor : otherReCiterArticle.getArticleCoAuthors().getAuthors()) {

							if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation().getAffiliationName() != null) {
								String affiliation = reCiterAuthor.getAffiliation().getAffiliationName();
								boolean isCitizenshipMatch = containsCitizenship(affiliation, targetAuthor);

								boolean isFirstNameMatch = StringUtils.equalsIgnoreCase(
										reCiterAuthor.getAuthorName().getFirstName(), targetAuthor.getAuthorName().getFirstName());

								if (isCitizenshipMatch && isFirstNameMatch) {
									//									slf4jLogger.info("citizenship match=" + affiliation + " " + otherReCiterArticle.getArticleId());
									//									if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
									//										clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
									//									} else {
									//										List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
									//										articleList.add(otherReCiterArticle);
									//										clusterIdToReCiterArticleList.put(clusterId, articleList);
									//									}

									isCitizenshipMatchFromPubmed = true;
									// remove from old cluster.
									//									iterator.remove();
									break; // break loop iterating over authors.
								}
							}
						}

						if (containsCitizenshipFromScopus || isCitizenshipMatchFromPubmed) {
							if (clusterIdToReCiterArticleList.containsKey(clusterId)) {
								clusterIdToReCiterArticleList.get(clusterId).add(otherReCiterArticle);
							} else {
								List<ReCiterArticle> articleList = new ArrayList<ReCiterArticle>();
								articleList.add(otherReCiterArticle);
								clusterIdToReCiterArticleList.put(clusterId, articleList);
							}
							iterator.remove();
						}
					}
				}
			}
		}

		// Add to new cluster.
		for (Entry<Integer, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				finalCluster.get(entry.getKey()).add(article);
			}
		}
	}

	/**
	 * Compute the most similar cluster to target.
	 * @return
	 */
	public Map<Integer, Integer> computeClusterSelectionForTarget() {

		Map<Integer, Integer> clusterIds = new HashMap<Integer, Integer>();
		for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
			for (ReCiterArticle reCiterArticle : entry.getValue().getArticleCluster()) {

				// Email Match.
				boolean isEmailMatch = isEmailMatch(reCiterArticle, targetAuthor);
				if (isEmailMatch) {
					//					slf4jLogger.info("Email Match=" + reCiterArticle.getArticleId() + " cluster id=" + entry.getKey());
					if (clusterIds.containsKey(entry.getKey())) {
						int currentCount = clusterIds.get(entry.getKey());
						clusterIds.put(entry.getKey(), ++currentCount);
					} else {
						clusterIds.put(entry.getKey(), 1);
					}
					reCiterArticle.appendClusterInfo("Email matched.");
				}

				// Department Match.
				for (ReCiterAuthor reCiterAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {
					boolean isDepartmentMatch = departmentMatch(reCiterAuthor, targetAuthor);
					boolean isFirstMatch = reCiterAuthor.getAuthorName().getFirstInitial().equalsIgnoreCase(targetAuthor.getAuthorName().getFirstInitial());
					if (isDepartmentMatch && isFirstMatch) {
						//						slf4jLogger.info("Department Match=" + reCiterArticle.getArticleId() + " cluster id=" + entry.getKey());
						if (clusterIds.containsKey(entry.getKey())) {
							int currentCount = clusterIds.get(entry.getKey());
							clusterIds.put(entry.getKey(), ++currentCount);
						} else {
							clusterIds.put(entry.getKey(), 1);
						}
					}
					reCiterArticle.appendClusterInfo("Department matched.");
				}

				// containsGrantCoAuthor.
				boolean containsGrantCoAuthor = containsGrantCoAuthor(reCiterArticle, targetAuthor);
				if (containsGrantCoAuthor) {
					//					slf4jLogger.info("Contains Grant CoaAuthor=" + reCiterArticle.getArticleId() + " cluster id=" + entry.getKey());
					//					System.out.println("containsGrantCoAuthor: " + reCiterArticle.getArticleId());
					if (clusterIds.containsKey(entry.getKey())) {
						int currentCount = clusterIds.get(entry.getKey());
						clusterIds.put(entry.getKey(), ++currentCount);
					} else {
						clusterIds.put(entry.getKey(), 1);
					}
					reCiterArticle.appendClusterInfo("contains grant coauthor");
				}
				
				// contains weill cornell. TODO: make sure target author's name matches the one iterated thru.
				boolean containsWeillCornell = containsWeillCornell(reCiterArticle);
				if (containsWeillCornell) {
					if (clusterIds.containsKey(entry.getKey())) {
						int currentCount = clusterIds.get(entry.getKey());
						clusterIds.put(entry.getKey(), ++currentCount);
					} else {
						clusterIds.put(entry.getKey(), 1);
					}
				}
			}
		}
		return clusterIds;
	}

	/**
	 * Select the candidate cluster.
	 * @param currentArticle
	 * @param targetAuthor
	 * @return
	 */
	public int selectCandidateCluster(ReCiterArticle currentArticle, TargetAuthor targetAuthor) {

		// Get cluster ids with max number of coauthor matches.
		//		Set<Integer> clusterIdSet = getKeysWithMaxVal(computeCoauthorMatch(currentArticle, targetAuthor));
		//
		//		// If groups have matching co-authors, the program selects the group that has the most matching names.
		//		if (clusterIdSet.size() == 1) {
		//			for (int id : clusterIdSet) {
		//				return id;
		//			}
		//		}
		//
		//		// If two or more of these have the same number of coauthors, the one with the highest matching score is selected.
		//		if (clusterIdSet.size() > 1) {
		//			return getIdWithMostContentSimilarity(clusterIdSet, currentArticle);
		//		}

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

	/**
	 * <p>
	 * Earliest years:
	 * Identify year of pub
	 * Get year of bachelor degree from rc_identity_degree
	 * Get year of doctoral degree from rc_identity_degree
	 * If discrepancy between pub and doctoral degree < -5, mark as false 
	 * </p>
	 * 
	 * <p>
	 * Example: 
	 * pubyear = 1990, doctoral degree = 1994, difference is -4, -5 < -4, therefore do nothing
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * pubyear = 1998, doctoral degree = 1994, difference is 4, -5 < 4, therefore do nothing.
	 * </p>
	 * 
	 * <p>
	 * If discrepancy between pub and bachelor degree < 1, mark as false
	 * Example:
	 * pubyear = 1998, bachelor degree = 1998, difference is 0, 1 < 0 is not true, therefore mark as false
	 * </p>
	 * 
	 * @param reCiterArticle
	 * @param targetAuthor
	 * @return
	 */
	public int computeYearDiscrepancyDoctoral(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		if (reCiterArticle.getJournal() != null) {
			int currentYearDiff = reCiterArticle.getJournal().getJournalIssuePubDateYear() - 
					targetAuthor.getDegree().getDoctoral();
			return currentYearDiff;
		}
		return 0;
	}

	public int computeYearDiscrepancyBachelors(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {
		if (reCiterArticle.getJournal() != null) {
			int currentYearDiff = reCiterArticle.getJournal().getJournalIssuePubDateYear() - 
					targetAuthor.getDegree().getBachelor();
			return currentYearDiff;
		}
		return 10; // so that it doesn't register as false.
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

	public double computeCosineSimilarity(ReCiterArticle a, ReCiterArticle b) {
		double maxSimilarity = -1;
		for (ReCiterAuthor author : a.getArticleCoAuthors().getAuthors()) {
			if (author.getAffiliation() != null && author.getAffiliation().getAffiliationName() != null) {
				for (ReCiterAuthor authorB : b.getArticleCoAuthors().getAuthors()) {
					if (authorB.getAffiliation() != null && authorB.getAffiliation().getAffiliationName() != null) {
						double sim = computeCosineSimilarity(
								author.getAffiliation().getAffiliationName(), 
								authorB.getAffiliation().getAffiliationName());
						if (sim > maxSimilarity) {
							maxSimilarity = sim;
						}
					}
				}
			}
		}
		return maxSimilarity;
	}

	/**
	 * Computes the cosine similarity distance between two strings.
	 * @param s1
	 * @param s2
	 * @return
	 */
	public double computeCosineSimilarity(String s1, String s2) {
		Document doc1 = new Document(s1);
		doc1.setId(1);
		Document doc2 = new Document(s2);
		doc2.setId(2);
		List<Document> documents = new ArrayList<Document>();
		documents.add(doc1);
		documents.add(doc2);
		TfIdf tfidf = new TfIdf(documents);
		tfidf.computeTfIdf();
		double[] d1 = tfidf.createVector(doc1);
		double[] d2 = tfidf.createVector(doc2);
		return tfidf.cosineSimilarity(d1, d2);
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
	public boolean containsCitizenship(String affiliation, TargetAuthor targetAuthor) {

		if (targetAuthor.getCitizenship() != null) {
			return StringUtils.containsIgnoreCase(affiliation, targetAuthor.getCitizenship());
		}

		return false;
	}

	public boolean containsCitizenshipFromScopus(ScopusArticle scopusArticle, TargetAuthor targetAuthor) {

		if (scopusArticle != null) {
			for (Entry<Long, Author> entry : scopusArticle.getAuthors().entrySet()) {
				boolean isNameMatch = entry.getValue().getSurname().equals(targetAuthor.getAuthorName().getLastName());
				if (isNameMatch) {
//					slf4jLogger.info("name= " + targetAuthor.getAuthorName().getLastName());
					if (scopusArticle.getAffiliationMap() != null && scopusArticle.getAffiliationMap().get(entry.getKey()) != null &&
							scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCountry() != null) {
						String scopusCountry = scopusArticle.getAffiliationMap().get(entry.getKey()).getAffiliationCountry();
						if (StringUtils.containsIgnoreCase(scopusCountry, targetAuthor.getCitizenship())) {
//							slf4jLogger.info("scopusCountry = " + scopusCountry + " author = " + targetAuthor.getCwid());
							return true;
						}
					}
				}
			}
		}
		return false;
	}


	private String extractCountry(String affiliation) {
		CountryDao countryDao = new CountryDaoImpl();
		Set<String> countries = countryDao.getCountryNames();
		for (String country : countries) {
			if (StringUtils.containsIgnoreCase(affiliation, country)) {
				return country;
			}
		}
		return "";
	}

	/**
	 * Extract Department information from string of the form "Department of *," or "Department of *.".
	 * 
	 * @param department Department string
	 * @return Department name.
	 */
	private String extractDepartment(String department) {
		final Pattern pattern = Pattern.compile("Department of (.+?)[\\.,]");
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

		if (reCiterAuthor.getAffiliation() != null && reCiterAuthor.getAffiliation().getAffiliationName() != null) {
			String affiliation = reCiterAuthor.getAffiliation().getAffiliationName();
			//			slf4jLogger.info("Country=" + extractCountry(affiliation));
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

	/** http://rosettacode.org/wiki/Levenshtein_distance#Java */
	public static int distance(String a, String b) {
		a = a.toLowerCase();
		b = b.toLowerCase();
		// i == 0
		int [] costs = new int [b.length() + 1];
		for (int j = 0; j < costs.length; j++)
			costs[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			// j == 0; nw = lev(i - 1, j)
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];
	}

	/**
	 * https://github.com/wcmc-its/ReCiter/issues/59.
	 */
	public boolean calculateNameMatch(ReCiterArticle reCiterArticle, ReCiterArticle articleInCluster) {
		for (ReCiterAuthor reCiterAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			for (ReCiterAuthor clusterAuthor : articleInCluster.getArticleCoAuthors().getAuthors()) {
				if (reCiterAuthor.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName()) &&
						clusterAuthor.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName())) {

					if (reCiterAuthor.getAuthorName().getFirstName().equalsIgnoreCase(clusterAuthor.getAuthorName().getFirstName())
							&&
							reCiterAuthor.getAuthorName().getMiddleInitial().equalsIgnoreCase(clusterAuthor.getAuthorName().getMiddleInitial())) {
						//						slf4jLogger.info(reCiterAuthor.getAuthorName().getFirstName() + " | " + clusterAuthor.getAuthorName().getFirstName());
						return true;
					} 
					//					else {
					//						if (reCiterAuthor.getAuthorName().getFirstInitial().equalsIgnoreCase(clusterAuthor.getAuthorName().getFirstInitial())) {
					//							// calculate the probability of how common the name is.
					//							// calculate the levenshtein distance.
					//							int levenshteinDist = distance(reCiterAuthor.getAuthorName().getFirstName(), clusterAuthor.getAuthorName().getFirstName());
					//							if (levenshteinDist <= 1) {
					//								return true;
					//							}
					//						}
					//					}
				}
			}
		}
		return false;
	}

	private double getYearDiscrepancyScore(int diffYear) {
		return YearDiscrepacyReader.getYearDiscrepancyMap().get(diffYear);
	}

	/**
	 * Year-based clustering and matching.
	 * Phase I. 
	 * https://github.com/wcmc-its/ReCiter/issues/40.
	 */
	public double calculateYearDiscrepancyScore(ReCiterArticle reCiterArticle, ReCiterArticle articleInCluster) {

		if (reCiterArticle.getJournal() != null && articleInCluster.getJournal() != null) {
			int year = reCiterArticle.getJournal().getJournalIssuePubDateYear();
			int otherYear = articleInCluster.getJournal().getJournalIssuePubDateYear();
			int diffYear = Math.abs(year - otherYear);
			return getYearDiscrepancyScore(diffYear);
		}

		return 0;
	}

	/**
	 * Year-based clustering and matching.
	 * Phase II.
	 * @param reCiterArticle
	 * @param targetAuthor
	 * @return
	 */
	public double calculateYearDiscrepancyScore(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {

		if (reCiterArticle.getJournal() != null) {
			int year = reCiterArticle.getJournal().getJournalIssuePubDateYear();
			int authorTerminalDegreeYear = targetAuthor.getEducation().getDegreeYear(); // Note: author might have multiple educations.
			int diffYear = Math.abs(year - authorTerminalDegreeYear);
			return getYearDiscrepancyScore(diffYear);
		}		
		return 0;
	}

	/**
	 * Use email as a conclusive indication of author identity when present.
	 * 
	 * https://github.com/wcmc-its/ReCiter/issues/57
	 */
	public boolean isEmailMatch(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {

		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (author.getAffiliation() != null && author.getAffiliation().getAffiliationName() != null) {
				String affiliation = author.getAffiliation().getAffiliationName();
				String emailCase1 = targetAuthor.getCwid() + "@med.cornell.edu";
				String emailCase2 = targetAuthor.getCwid() + "@mail.med.cornell.edu";
				String emailCase3 = targetAuthor.getCwid() + "@weill.cornell.edu";
				String emailCase4 = targetAuthor.getCwid() + "@nyp.org";

				if (affiliation.contains(emailCase1) ||
						affiliation.contains(emailCase2) ||
						affiliation.contains(emailCase3) ||
						affiliation.contains(emailCase4)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean containsWeillCornell(String affiliation) {
		if (StringUtils.containsIgnoreCase(affiliation, "weill cornell") || 
				StringUtils.containsIgnoreCase(affiliation, "weill-cornell") || 
				StringUtils.containsIgnoreCase(affiliation, "weill medical") || 
				StringUtils.containsIgnoreCase(affiliation, "cornell medical center") || 
				StringUtils.containsIgnoreCase(affiliation, "Memorial Sloan-Kettering Cancer Center")) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Check if the ReCiterArticle's affiliation information contains the phrase 
	 * "weill cornell", "weill-cornell", "weill medical" using case-insensitive
	 * string matching.
	 * 
	 * @param reCiterArticle
	 * @return
	 */
	public boolean containsWeillCornell(ReCiterArticle reCiterArticle) {
		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			if (author.getAffiliation() != null) {
				String affiliation = author.getAffiliation().getAffiliationName();
				if (containsWeillCornell(affiliation)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check affiliation exists in Scopus Article.
	 * @param scopusArticle
	 * @param targetAuthor
	 * @return
	 */
	public boolean containsWeillCornellFromScopus(ScopusArticle scopusArticle, TargetAuthor targetAuthor) {
		if (scopusArticle != null) {
			for (Author scopusAuthor : scopusArticle.getAuthors().values()) {
				if (StringUtils.equalsIgnoreCase(scopusAuthor.getSurname(), targetAuthor.getAuthorName().getLastName())) {
					Set<Integer> afidSet = scopusAuthor.getAfidSet();
					for (int afid : afidSet) {
						Affiliation scopusAffialition = scopusArticle.getAffiliationMap().get(afid);
						if (scopusAffialition != null) {
							String affilName = scopusAffialition.getAffilname();
							if (containsWeillCornell(affilName)) {
								slf4jLogger.info(scopusArticle.getPubmedId() + ": " + affilName);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	private List<Identity> getGrantCoAuthors(TargetAuthor targetAuthor) {
		IdentityDao identityDao = new IdentityDaoImpl();
		return identityDao.getAssosiatedGrantIdentityList(targetAuthor.getCwid());
	}

	private List<AuthorName> getAuthorNames(List<Identity> identityList) {
		List<AuthorName> authorNames = new ArrayList<AuthorName>();
		for (Identity identity : identityList) {
			AuthorName authorName = 
					new AuthorName(identity.getFirstName(), identity.getMiddleName(), identity.getLastName());
			authorNames.add(authorName);
		}
		return authorNames;
	}

	private List<AuthorName> getGrantCoAuthorsOf(TargetAuthor targetAuthor) {
		return getAuthorNames(getGrantCoAuthors(targetAuthor));
	}

	/**
	 * Leverage known co-investigators on grants to improve phase two matching.
	 * 
	 * If an article includes a name that matches that of a person who has previously served as a 
	 * co-author with the target author on a grant, this should increase the likelihood that the articles in the 
	 * given pile were in fact written by the target author.
	 * 
	 * https://github.com/wcmc-its/ReCiter/issues/49
	 */
	public boolean containsGrantCoAuthor(ReCiterArticle reCiterArticle, TargetAuthor targetAuthor) {

		List<AuthorName> authorNames = getGrantCoAuthorsOf(targetAuthor);

		for (ReCiterAuthor author : reCiterArticle.getArticleCoAuthors().getAuthors()) {
			// do not match target author's name
			if (!author.getAuthorName().firstInitialLastNameMatch(targetAuthor.getAuthorName())) {
				for (AuthorName authorName : authorNames) {
					if (authorName.isFullNameMatch(author.getAuthorName())) {
						return true;
					}
				}
			}
		}
		return false;
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
		for (int id : clusterIdList) {
			ReCiterCluster reCiterCluster = finalCluster.get(id);
			for (ReCiterArticle reCiterArticle : reCiterCluster.getArticleCluster()) {

				/**
				 * First Name match.
				 */
				boolean isFirstNameMatch = calculateNameMatch(currentArticle, reCiterArticle);
				if (isFirstNameMatch) {
					return id;
				}

				/**
				 * Journal Match.
				 */
				//				boolean isJournalNameMatch = isJournalMatch(currentArticle, reCiterArticle);
				//				if (isJournalNameMatch) {
				//					return id;
				//				}
			}
		}
		return currentMaxId; // found a cluster.
	}

	/**
	 * computes coauthor matches of this article with all current clusters.
	 * @param currentArticle
	 * @param targetAuthor
	 * @return
	 */
	private Map<Integer, Integer> computeCoauthorMatch(ReCiterArticle currentArticle, TargetAuthor targetAuthor) {
		Map<Integer, Integer> coauthorsCount = new HashMap<Integer, Integer>(); // ClusterId to number of coauthors.
		for (ReCiterCluster reCiterCluster : finalCluster.values()) {
			int matchingCoauthors = reCiterCluster.getMatchingCoauthorCount(currentArticle, targetAuthor);
			coauthorsCount.put(reCiterCluster.getClusterID(), matchingCoauthors);
		}
		if (currentArticle.getArticleId() == 19805687) {
			slf4jLogger.debug("count=" + coauthorsCount);
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
		if (!map.isEmpty()) {
			int maxValueInMap=(Collections.max(map.values()));  // This will return max value in the Hashmap
			//		System.out.println("Max value: " + maxValueInMap);
			for (Entry<Integer, Integer> entry : map.entrySet()) {
				if (entry.getValue() == maxValueInMap && maxValueInMap != 0) {
					keyList.add(entry.getKey());
				}
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

	public String getClusterInfo() {
		StringBuilder sb = new StringBuilder();

		for (Entry<Integer, ReCiterCluster> cluster : finalCluster.entrySet()) {
			sb.append("\nCluster id: " + cluster.getKey() + "= ");
			for (ReCiterArticle reCiterArticle : cluster.getValue().getArticleCluster()) {
				sb.append(reCiterArticle.getArticleId() + ", ");
			}
		}
		return sb.toString();
	}

	public Set<Integer> getSelectedClusterIdSet() {
		return selectedClusterIdSet;
	}

	public void setSelectedClusterIdSet(Set<Integer> selectedClusterIdSet) {
		this.selectedClusterIdSet = selectedClusterIdSet;
	}
}
