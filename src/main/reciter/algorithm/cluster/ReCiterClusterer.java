package main.reciter.algorithm.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import main.database.dao.JournalDao;
import main.reciter.algorithm.cluster.model.ReCiterCluster;
import main.reciter.lucene.docsimilarity.AffiliationCosineSimilarity;
import main.reciter.lucene.docsimilarity.DocumentSimilarity;
import main.reciter.lucene.docsimilarity.KeywordCosineSimilarity;
import main.reciter.model.article.ReCiterArticle;
import main.reciter.model.author.TargetAuthor;
import main.reciter.model.author.TargetAuthor.TypeScore;
import main.reciter.utils.Analysis;
import main.reciter.utils.AnalysisObject;
import main.reciter.utils.YearDiscrepacyReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.examples.pubmed.ReCiterExampleTest;

/**
 * 
 * @author jil3004
 *
 */
public class ReCiterClusterer implements Clusterer {

	/**
	 * Debug boolean.
	 */
	private boolean debug = false;

	/**
	 * CSV boolean
	 */
	private boolean debugCSV = false;

	/**
	 * Logger.
	 */
	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterer.class);	

	/**
	 * A map that contains the id to ReCiterCluster object.
	 */
	private Map<Integer, ReCiterCluster> finalCluster;

	/**
	 * List of articles to be clustered.
	 */
	private List<ReCiterArticle> articleList;

	/**
	 * Boolean containing the value of whether it's currently selecting the final target person.
	 */
	boolean selectingTarget = false;

	/**
	 * ReCiterArticle to ReCiterCluster similarity threshold value.
	 */
	public double similarityThreshold = 0.1;

	/**
	 * ReCiterArticle (target person) to ReCiterClusters in the 
	 * finalCluster similarity threshold value.
	 */
	public double targetAuthorSimilarityThreshold = 0.001; // target to cluster similarity threshold value.

	/**
	 * Hierarchical Agglomerative Clustering:
	 * cluster to cluster similarity threshold value.
	 */
	double hacSimilarityThreshold = 0.7; // hierarchical agglomerative clustering threshold similarity.

	/**
	 * Constructor initializing the finalCluster map data structure.
	 */
	public ReCiterClusterer() {
		if (debug) {
			slf4jLogger.info("Initializing ReCiterClusterer...\nParameter Settings:\n"
					+ "Cluster to article similarity threshold: " + similarityThreshold + "\n"
					+ "Final Cluster list to target author similarity threshold: " + targetAuthorSimilarityThreshold);
		}
		finalCluster = new HashMap<Integer, ReCiterCluster>();
	}

	public void cluster(double similarityThreshold, double incrementer, Analysis analysis) {

		this.similarityThreshold = similarityThreshold;

		finalCluster.clear(); // forgot to clear this: now similarity threshold should work.
		ReCiterCluster.getClusterIDCounter().set(0); // reset counter.

		cluster();

		DocumentSimilarity affiliationSimilarity = new AffiliationCosineSimilarity();
		DocumentSimilarity keywordSimilarity = new KeywordCosineSimilarity();

		ReCiterArticle targetAuthorArticle = TargetAuthor.getInstance().getTargetAuthorArticleIndexed();
		// Compute the affiliation similarity and keyword similarity of target author to all the clusters.
		for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
			TargetAuthor.getInstance().getMap().put(entry.getKey(), new ArrayList<TypeScore>());
			double affiliationMax = -1;
			double keywordMax = -1;
			for (ReCiterArticle reCiterArticle : entry.getValue().getArticleCluster()) {
				double currentAffiliationSimScore = affiliationSimilarity.documentSimilarity(reCiterArticle, targetAuthorArticle);
				double currentKeywordSimScore = keywordSimilarity.documentSimilarity(reCiterArticle, targetAuthorArticle);

				if (currentAffiliationSimScore > affiliationMax) {
					affiliationMax = currentAffiliationSimScore;
				}

				if (currentKeywordSimScore > keywordMax) {
					keywordMax = currentKeywordSimScore;
				}
			}
			TargetAuthor.getInstance().getMap().get(entry.getKey()).add(new TypeScore("affiliation", affiliationMax));
			TargetAuthor.getInstance().getMap().get(entry.getKey()).add(new TypeScore("keyword", keywordMax));
		}

		// Assign target author to a cluster in finalCluster.
		int assignedClusterId = assignTargetToCluster(TargetAuthor.getInstance().getTargetAuthorArticleIndexed());

		ReCiterCluster reCiterCluster = finalCluster.get(assignedClusterId);

		if (reCiterCluster != null) {
			Set<Integer> pmidSet = reCiterCluster.getPmidSet();
			analysis.setTruePositiveList(pmidSet);
			analysis.setSizeOfSelected(pmidSet.size());
			double precision = analysis.getPrecision();
			double recall = analysis.getRecall();
			//		double avgPrecRecall = (precision + recall) / 2;

			// Analysis:
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				for (ReCiterArticle reCiterArticle : entry.getValue().getArticleCluster()) {
					AnalysisObject analysisObject = new AnalysisObject();
					analysisObject.setSimilarityMeasure(similarityThreshold);
					analysisObject.setReCiterArticle(reCiterArticle);
					analysisObject.setClusterId(entry.getValue().getClusterID());
					analysisObject.setNumArticlesInCluster(entry.getValue().getArticleCluster().size());

					if (reCiterArticle.getJournal() != null) {
						analysisObject.setYearOfPublication(reCiterArticle.getJournal().getJournalIssuePubDateYear());
					} else {
						analysisObject.setYearOfPublication(-1);
					}
					// If the cluster id is the same, then cluster is selected as the assigned matching cluster.
					if (entry.getKey() == reCiterCluster.getClusterID()) {
						analysisObject.setSelected(true);
						// True Positive.
						if (analysis.getGoldStandard().contains(reCiterArticle.getArticleID())) {
							analysisObject.setStatus("True Positive");
						}
						// False Positive.
						else {
							analysisObject.setStatus("False Positive");
						}
					} else {
						analysisObject.setSelected(false);
						// True Negative.
						if (!analysis.getGoldStandard().contains(reCiterArticle.getArticleID())) {
							analysisObject.setStatus("True Negative");
						}
						// False Negative.
						else {
							analysisObject.setStatus("False Negative");
						}
					}

					AnalysisObject.getAnalysisObjectList().add(analysisObject);	
				}
			}

			slf4jLogger.info("Precision = " + precision);
			slf4jLogger.info("Recall = " + recall);

			ReCiterExampleTest.totalPrecision += precision;
			ReCiterExampleTest.totalRecall += recall;
		} else {
			slf4jLogger.info("No cluster match found.");
		}
	}

	/**
	 * Getter method to retrieve the finalCluster map data structure.
	 * @return
	 */
	public Map<Integer, ReCiterCluster> getFinalCluster() {
		return finalCluster;
	}

	/**
	 * 
	 * @param article
	 * @param targetAuthor
	 * @return
	 */
	public int assignTargetToCluster(ReCiterArticle article) {
		selectingTarget = true;
		return selectCandidateCluster(article);
	}

	/**
	 * Hierarchical Agglomerative Clustering.
	 * TODO place this function in another class.
	 * @param articleList
	 */
	public void hacCluster(List<ReCiterArticle> articleList) {
		for (ReCiterArticle article : articleList) {
			ReCiterCluster r = new ReCiterCluster();
			r.add(article);
			finalCluster.put(r.getClusterID(), r);
		}
		double currentMaxSimilarity = Integer.MAX_VALUE;
		while (currentMaxSimilarity > hacSimilarityThreshold && finalCluster.size() > 0) {
			double currentMaxSim = -1;
			int clusterId1 = -1;
			int clusterId2 = -1;
			for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
				for (Entry<Integer, ReCiterCluster> entryInner : finalCluster.entrySet()) {
					if (entry.getKey() != entryInner.getKey()) {
						double sim = entry.getValue().similar(entryInner.getValue());
						if (sim > currentMaxSim) {
							currentMaxSim = sim;
							clusterId1 = entry.getKey();
							clusterId2 = entryInner.getKey();
						}
					}
				}
			}
			if (clusterId1 != -1 && clusterId2 != -1) {
				finalCluster.get(clusterId1).addAll(finalCluster.get(clusterId2)); // add all articles from cluster 2 to 1.
				finalCluster.remove(clusterId2);
				currentMaxSimilarity = currentMaxSim;
			}
		}
	}

	/**
	 * 
	 * @param articleList
	 * @param targetAuthor
	 */
	public void cluster() {

		ReCiterCluster firstCluster = new ReCiterCluster();
		ReCiterArticle first = articleList.get(0);

		slf4jLogger.info("Number of articles to be clustered: " + articleList.size()); // delete
		if (debug) {
			slf4jLogger.info("Number of articles to be clustered: " + articleList.size());
			slf4jLogger.info("First article: " + first);
		}
		first.setClusterStarter(true); // first article is the cluster starter.
		firstCluster.add(first);

		if (debugCSV) {
			slf4jLogger.info(firstCluster.getClusterID() + ", " + first.toCSV());
		}

		finalCluster.put(firstCluster.getClusterID(), firstCluster);
		for (int i = 1; i < articleList.size(); i++) {
			ReCiterArticle article = articleList.get(i);
			//			slf4jLogger.info(i + ": Assigning article: " + article.getArticleID());
			int selection = selectCandidateCluster(article);
			if (selection == -1) {
				if (debug) {
					article.setInfo("Forming its own cluster");
					slf4jLogger.info(article.getArticleID() + " - forming its own cluster.");
					article.setClusterStarter(true);
				}
				// create its own cluster.
				ReCiterCluster newCluster = new ReCiterCluster();
				newCluster.add(article);
				finalCluster.put(newCluster.getClusterID(), newCluster);
				if (debugCSV) {
					slf4jLogger.info(newCluster.getClusterID() + ", " + article.toCSV());
				}
			} else {
				finalCluster.get(selection).add(article);
				//				slf4jLogger.info("PMID: " + article.getArticleID() + " selection: " + selection);
				if (debugCSV) {
					slf4jLogger.info(selection + ", " + article.toCSV());
				}
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
				if (selectingTarget) {
					if (debug) {
						slf4jLogger.info("(Target to Cluster) Selecting the group with most matching coauthors: " + id);
					}
				}
				if (debug) {
					slf4jLogger.info("Selecting cluster " + id + " as candidate cluster based on most matching names.");
				}
				return id;
			}
		}

		// If two or more of these have the same number of coauthors, the one with the highest matching score is selected.
		if (clusterIdSet.size() > 1) {
			if (selectingTarget) {
				if (debug) {
					slf4jLogger.info("(Target to Cluster) Selecting the group with most content similarity");
				}
			}
			return getIdWithMostContentSimilarity(clusterIdSet, currentArticle);
		}

		// If groups have no matching co-authors, the group with the highest 
		// matching (cosine) score is selected, provided that the score
		// exceeds a given threshold.
		//		System.out.println("Using similarity threshold");
		return getIdWithMostContentSimilarity(finalCluster.keySet(), currentArticle);
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
			// Grab CWID from rc_identity table. Combine with "@med.cornell.edu" and match against candidate records. 
			// When email is found in affiliation string, during phase two clustering, automatically assign the matching identity.
			if (selectingTarget) {
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					if (article.getAffiliationConcatenated() != null) {
						if (article.getAffiliationConcatenated().contains(TargetAuthor.getInstance().getCwid() + "@med.cornell.edu")) {
							return id;
						}
					}
				}
			}

			double sim = finalCluster.get(id).contentSimilarity(currentArticle); // cosine similarity score.

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

			// Adjust cosine similarity score with journal similarity.
			//			if (!selectingTarget) {
			//				JournalDao journalDao = new JournalDao();
			//				double sumJournalSimilarityScore = -1;
			//				int numJournalComparisions = 0;
			//				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
			//					if (article.getJournal() != null && currentArticle.getJournal() != null) {
			//						double journalSimScore = journalDao.getJournalSimilarity(
			//								article.getJournal().getIsoAbbreviation(),
			//								currentArticle.getJournal().getIsoAbbreviation());
			//						// Check similarity both ways.
			//						if (journalSimScore == -1.0) {
			//							journalSimScore = journalDao.getJournalSimilarity(
			//									currentArticle.getJournal().getIsoAbbreviation(),
			//									article.getJournal().getIsoAbbreviation());
			//						}
			//						if (journalSimScore != -1.0) {
			//							sumJournalSimilarityScore += journalSimScore;
			//							numJournalComparisions++;
			//						}
			//					}
			//				}
			//				if (numJournalComparisions != 0) {
			//					double avgJournalSimScore = sumJournalSimilarityScore / numJournalComparisions;
			//					if (avgJournalSimScore > 0.8) {
			//						sim *= 1.5;
			//					}
			//				}
			//			}

			if (!selectingTarget) {
				JournalDao journalDao = new JournalDao();
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					// Use the cluster starter to compare journal similarity.
					if (article.isClusterStarter()) {
						if (article.getJournal() != null && currentArticle.getJournal() != null) {
							double journalSimScore = journalDao.getJournalSimilarity(
									article.getJournal().getIsoAbbreviation(),
									currentArticle.getJournal().getIsoAbbreviation());
							// Check similarity both ways.
							if (journalSimScore == -1.0) {
								journalSimScore = journalDao.getJournalSimilarity(
										currentArticle.getJournal().getIsoAbbreviation(),
										article.getJournal().getIsoAbbreviation());
							}
							if (journalSimScore != -1.0) {
								if (journalSimScore > 0.8) {
									sim *= (1 + journalSimScore);
								}
							}
						}
					}
				}
			}

			if (selectingTarget) {

				//				System.out.println("Cosine similarity between target and cluster " + id + ": score=" + sim);
				if (debug) {
					slf4jLogger.info("Cosine similarity between target and cluster " + id + ": score=" + sim);
				}
				// Update the similarity score with year discrepancy.
				int yearDiff = Integer.MAX_VALUE; // Compute difference in year between candidate article and closest year in article cluster.
				for (ReCiterArticle article : finalCluster.get(id).getArticleCluster()) {
					int currentYearDiff = Math.abs(TargetAuthor.getInstance().getTerminalDegreeYear() - article.getJournal().getJournalIssuePubDateYear());
					if (currentYearDiff < yearDiff) {
						yearDiff = currentYearDiff;
					}
				}
				// Moderately penalize articles that were published slightly before (0 - 7 years) a person's terminal 
				// degree, and strongly penalize articles that were published well before (>7 years) a person's terminal degree.
				if (yearDiff > 40) {
					sim *= 0.001526;
				} else {
					sim = sim * YearDiscrepacyReader.getYearDiscrepancyMap().get(yearDiff);
				}
				if (sim > targetAuthorSimilarityThreshold && sim > currentMax) {
					currentMaxId = id;
					currentMax = sim;
				}
			} else if (sim > similarityThreshold && sim > currentMax) {
				// not selecting target:
				if (debug) {

					slf4jLogger.info("Similarity article " + currentArticle.getArticleID() + " to cluster: " + id + " score: " + sim);
					slf4jLogger.info(finalCluster.get(id).toString());
				}
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
		for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
			int clusterId = entry.getKey();
			ReCiterCluster reCiterCluster = entry.getValue();
			int matchingCoauthors = reCiterCluster.getMatchingCoauthorCount(currentArticle);
			coauthorsCount.put(clusterId, matchingCoauthors);
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


	public List<ReCiterArticle> getArticleList() {
		return articleList;
	}

	public void setArticleList(List<ReCiterArticle> articleList) {
		this.articleList = articleList;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Number of clusters formed: " + getFinalCluster().size() + "\n");

		for (ReCiterCluster r : getFinalCluster().values()) {
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
	public void cluster(List<ReCiterArticle> reciterArticleList) {
		// TODO Auto-generated method stub

	}
}


/**
public void cluster(double similarityThreshold, double incrementer, Analysis analysis) {

double overallHighestPrecision = -1;
double overallHighestRecall = -1;
double overallAvgPrecRecall = -1;
//		double bestSimThreshold = -1;

//		for (; similarityThreshold < 1.0; similarityThreshold += incrementer) {
finalCluster.clear(); // forgot to clear this: now similarity threshold should work.
// reset counter.
ReCiterCluster.getClusterIDCounter().set(0);
slf4jLogger.info("Similarity Threshold: " + similarityThreshold);
this.similarityThreshold = similarityThreshold;

cluster();

double highestPrecision = -1;
double highestRecall = -1;
double highestAvgPrecRecall = -1;

// Assign target author to a cluster in finalCluster.
int assignedClusterId = assignTargetToCluster(TargetAuthor.getInstance().getTargetAuthorArticleIndexed());

ReCiterCluster reCiterCluster = finalCluster.get(assignedClusterId);
//			for (ReCiterCluster reCiterCluster : finalCluster.values()) {

Set<Integer> pmidSet = reCiterCluster.getPmidSet();
analysis.setTruePositiveList(pmidSet);
analysis.setSizeOfSelected(pmidSet.size());
double precision = analysis.getPrecision();
double recall = analysis.getRecall();
double avgPrecRecall = (precision + recall) / 2;
if (precision > highestPrecision) {
	highestPrecision = precision;
}
if (recall > highestRecall) {
	highestRecall = recall;
}
if (avgPrecRecall > highestAvgPrecRecall) {
	highestAvgPrecRecall = avgPrecRecall;
}
if (debug) {
	slf4jLogger.info("Set of pmids: " + pmidSet);
	slf4jLogger.info("Gold standard: " + analysis.getGoldStandard());
	slf4jLogger.info("Precision: " + precision);
	slf4jLogger.info("Recall: " + recall);
}

// Analysis:

for (Entry<Integer, ReCiterCluster> entry : finalCluster.entrySet()) {
	for (ReCiterArticle reCiterArticle : entry.getValue().getArticleCluster()) {
		AnalysisObject analysisObject = new AnalysisObject();
		analysisObject.setSimilarityMeasure(similarityThreshold);
		analysisObject.setReCiterArticle(reCiterArticle);
		analysisObject.setClusterId(entry.getValue().getClusterID());
		analysisObject.setNumArticlesInCluster(entry.getValue().getArticleCluster().size());

		// If the cluster id is the same, then cluster is selected as the assigned matching cluster.
		if (entry.getKey() == reCiterCluster.getClusterID()) {
			analysisObject.setSelected(true);
			// True Positive.
			if (analysis.getGoldStandard().contains(reCiterArticle.getArticleID())) {
				analysisObject.setStatus("True Positive");
			}
			// False Positive.
			else {
				analysisObject.setStatus("False Positive");
			}
		} else {
			analysisObject.setSelected(false);
			// True Negative.
			if (!analysis.getGoldStandard().contains(reCiterArticle.getArticleID())) {
				analysisObject.setStatus("True Negative");
			}
			// False Negative.
			else {
				analysisObject.setStatus("False Negative");
			}
		}

		AnalysisObject.getAnalysisObjectList().add(analysisObject);	
	}
}
//			}

if (highestPrecision > overallHighestPrecision) {
	overallHighestPrecision = highestPrecision;
}
if (highestRecall > overallHighestRecall) {
	overallHighestRecall = highestRecall;
}
if (highestAvgPrecRecall > overallAvgPrecRecall) {
	overallAvgPrecRecall = highestAvgPrecRecall;
	bestSimThreshold = similarityThreshold; // overall best similarity threshold is based on best highest average precision and recall.
}
//			slf4jLogger.info("Highest Precision: " + highestPrecision);
//			slf4jLogger.info("Highest Recall: " + highestRecall);
//			slf4jLogger.info("Highest (Precision + Recall) / 2: " + highestAvgPrecRecall);

// Writing to CSV:


//		}
//		slf4jLogger.info("Note: overall best similarity threshold is based on best highest average precision and recall.");
slf4jLogger.info("Precision = " + overallHighestPrecision);
slf4jLogger.info("Recall = " + overallHighestRecall);
//		slf4jLogger.info("Overall Best (Precision + Recall) / 2: " + overallAvgPrecRecall + " with similarity threshold=" + bestSimThreshold);
}
 */