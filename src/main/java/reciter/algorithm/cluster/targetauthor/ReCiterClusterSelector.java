/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package reciter.algorithm.cluster.targetauthor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.evidence.StrategyContext;
import reciter.algorithm.evidence.article.ReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.RemoveReCiterArticleStrategyContext;
import reciter.algorithm.evidence.article.citation.CitationStrategyContext;
import reciter.algorithm.evidence.article.citation.strategy.CitationStrategy;
import reciter.algorithm.evidence.article.citation.strategy.InverseCoCitationStrategy;
import reciter.algorithm.evidence.article.coauthor.CoauthorStrategyContext;
import reciter.algorithm.evidence.article.coauthor.strategy.CoauthorStrategy;
import reciter.algorithm.evidence.article.journal.JournalStrategyContext;
import reciter.algorithm.evidence.article.journal.strategy.JournalStrategy;
import reciter.algorithm.evidence.cluster.RemoveClusterStrategyContext;
import reciter.algorithm.evidence.cluster.clustersize.ClusterSizeStrategyContext;
import reciter.algorithm.evidence.cluster.clustersize.strategy.ClusterSizeStrategy;
import reciter.algorithm.evidence.targetauthor.TargetAuthorStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.AffiliationStrategyContext;
import reciter.algorithm.evidence.targetauthor.affiliation.strategy.CommonAffiliationStrategy;
import reciter.algorithm.evidence.targetauthor.articlesize.ArticleSizeStrategyContext;
import reciter.algorithm.evidence.targetauthor.articlesize.strategy.ArticleSizeStrategy;
import reciter.algorithm.evidence.targetauthor.citizenship.CitizenshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.citizenship.strategy.CitizenshipStrategy;
import reciter.algorithm.evidence.targetauthor.degree.DegreeStrategyContext;
import reciter.algorithm.evidence.targetauthor.degree.strategy.DegreeType;
import reciter.algorithm.evidence.targetauthor.degree.strategy.YearDiscrepancyStrategy;
import reciter.algorithm.evidence.targetauthor.department.DepartmentStrategyContext;
import reciter.algorithm.evidence.targetauthor.department.strategy.DepartmentStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.education.EducationStrategyContext;
import reciter.algorithm.evidence.targetauthor.education.strategy.EducationStrategy;
import reciter.algorithm.evidence.targetauthor.email.EmailStrategyContext;
import reciter.algorithm.evidence.targetauthor.email.strategy.EmailStringMatchStrategy;
import reciter.algorithm.evidence.targetauthor.grant.GrantStrategyContext;
import reciter.algorithm.evidence.targetauthor.grant.strategy.GrantStrategy;
import reciter.algorithm.evidence.targetauthor.knownrelationship.KnownRelationshipStrategyContext;
import reciter.algorithm.evidence.targetauthor.knownrelationship.strategy.KnownRelationshipStrategy;
import reciter.algorithm.evidence.targetauthor.name.RemoveByNameStrategyContext;
import reciter.algorithm.evidence.targetauthor.name.strategy.RemoveByNameStrategy;
import reciter.algorithm.evidence.targetauthor.scopus.ScopusStrategyContext;
import reciter.algorithm.evidence.targetauthor.scopus.strategy.ScopusCommonAffiliation;
import reciter.engine.StrategyParameters;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class ReCiterClusterSelector extends AbstractClusterSelector {

	private static final Logger slf4jLogger = LoggerFactory.getLogger(ReCiterClusterSelector.class);

	/** Cluster selection strategy contexts. */

	/**
	 * Email Strategy.
	 */
	private StrategyContext emailStrategyContext;

	/**
	 * Department Strategy.
	 */
	private StrategyContext departmentStringMatchStrategyContext;

	/**
	 * Known co-investigator strategy context.
	 */
	private StrategyContext knownRelationshipsStrategyContext;

	/**
	 * Affiliation strategy context.
	 */
	private StrategyContext affiliationStrategyContext;

	/** Individual article selection strategy contexts. */
	/**
	 * Scopus strategy context.
	 */
	private StrategyContext scopusCommonAffiliationStrategyContext;

	/**
	 * Coauthor strategy context.
	 */
	private StrategyContext coauthorStrategyContext;

	/**
	 * Journal strategy context.
	 */
	private StrategyContext journalStrategyContext;

	/**
	 * Citizenship strategy context.
	 */
	private StrategyContext citizenshipStrategyContext;

	/**
	 * Year Discrepancy (Bachelors).
	 */
	private StrategyContext bachelorsYearDiscrepancyStrategyContext;

	/**
	 * Year Discrepancy (Doctoral).
	 */
	private StrategyContext doctoralYearDiscrepancyStrategyContext;

	/**
	 * Discounts Articles not in English.
	 */
	private StrategyContext articleTitleInEnglishStrategyContext;

	/**
	 * Education.
	 */
	private StrategyContext educationStrategyContext;

	/**
	 * Remove article if the full first name doesn't match.
	 */
	private StrategyContext removeByNameStrategyContext;

	/**
	 * Article size.
	 */
	private StrategyContext articleSizeStrategyContext;

	/**
	 * Remove clusters based on cluster information.
	 */
	private StrategyContext clusterSizeStrategyContext;

	//	private StrategyContext boardCertificationStrategyContext;
	//
	//	private StrategyContext degreeStrategyContext;
	
	private StrategyContext grantStrategyContext;
	
	private StrategyContext citationStrategyContext;
	
	private StrategyContext coCitationStrategyContext;
	
	private List<StrategyContext> strategyContexts;

	private Set<Long> selectedClusterIds; // List of currently selected cluster ids.
	
	private StrategyParameters strategyParameters;

	public ReCiterClusterSelector(Map<Long, ReCiterCluster> clusters, Identity identity, StrategyParameters strategyParameters) {

		this.strategyParameters = strategyParameters;
		
		// Strategies that select clusters that are similar to the target author.
		emailStrategyContext = new EmailStrategyContext(new EmailStringMatchStrategy());
		departmentStringMatchStrategyContext = new DepartmentStrategyContext(new DepartmentStringMatchStrategy());
		knownRelationshipsStrategyContext = new KnownRelationshipStrategyContext(new KnownRelationshipStrategy());
		affiliationStrategyContext = new AffiliationStrategyContext(new CommonAffiliationStrategy());

		// Using the following strategy contexts in sequence to reassign individual articles
		// to selected clusters.
		scopusCommonAffiliationStrategyContext = new ScopusStrategyContext(new ScopusCommonAffiliation());
		coauthorStrategyContext = new CoauthorStrategyContext(new CoauthorStrategy(identity));
		journalStrategyContext = new JournalStrategyContext(new JournalStrategy(identity));
		citizenshipStrategyContext = new CitizenshipStrategyContext(new CitizenshipStrategy());
		educationStrategyContext = new EducationStrategyContext(new EducationStrategy()); // check this one.
		grantStrategyContext = new GrantStrategyContext(new GrantStrategy());
		citationStrategyContext = new CitationStrategyContext(new CitationStrategy());
		coCitationStrategyContext = new CitationStrategyContext(new InverseCoCitationStrategy());
		
		int numArticles = 0;
		for (ReCiterCluster reCiterCluster : clusters.values()) {
			numArticles += reCiterCluster.getArticleCluster().size();
		}
		articleSizeStrategyContext = new ArticleSizeStrategyContext(new ArticleSizeStrategy(numArticles));

		// TODO: getBoardCertificationScore(map);

		bachelorsYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.BACHELORS));
		doctoralYearDiscrepancyStrategyContext = new DegreeStrategyContext(new YearDiscrepancyStrategy(DegreeType.DOCTORAL));
		// articleTitleInEnglishStrategyContext = new ArticleTitleStrategyContext(new ArticleTitleInEnglish());
		removeByNameStrategyContext = new RemoveByNameStrategyContext(new RemoveByNameStrategy());

		clusterSizeStrategyContext = new ClusterSizeStrategyContext(new ClusterSizeStrategy());

		strategyContexts = new ArrayList<StrategyContext>();
		
		if (strategyParameters.isScopusCommonAffiliation()) {
			strategyContexts.add(scopusCommonAffiliationStrategyContext);
		}
		
		if (strategyParameters.isCoauthor()) {
			strategyContexts.add(coauthorStrategyContext);
		}
		
		if (strategyParameters.isJournal()) {
			strategyContexts.add(journalStrategyContext);
		}
		
		if (strategyParameters.isCitizenship()) {
			strategyContexts.add(citizenshipStrategyContext);
		}
		
		if (strategyParameters.isEducation()) {
			strategyContexts.add(educationStrategyContext);
		}	
		
		if (strategyParameters.isGrant()) {
			strategyContexts.add(grantStrategyContext);
		}
		
		if (strategyParameters.isCitation()) {
			strategyContexts.add(citationStrategyContext);
		}
		
		if (strategyParameters.isCoCitation()) {
			strategyContexts.add(coCitationStrategyContext);
		}
		
		if (strategyParameters.isArticleSize()) {
			strategyContexts.add(articleSizeStrategyContext);
		}

		if (strategyParameters.isBachelorsYearDiscrepancy()) {
			strategyContexts.add(bachelorsYearDiscrepancyStrategyContext);
		}
		
		if (strategyParameters.isDoctoralYearDiscrepancy()) {
			strategyContexts.add(doctoralYearDiscrepancyStrategyContext);
		}
		
		//		strategyContexts.add(articleTitleInEnglishStrategyContext);
		
		if (strategyParameters.isRemoveByName()) {
			strategyContexts.add(removeByNameStrategyContext);
		}

		// Re-run these evidence types (could have been removed or not processed in sequence).
		strategyContexts.add(emailStrategyContext);

		// https://github.com/wcmc-its/ReCiter/issues/136
		if (strategyParameters.isClusterSize()) {
			strategyContexts.add(clusterSizeStrategyContext);
		}
	}

	public void runStrategy(StrategyContext strategyContext, List<ReCiterArticle> reCiterArticles, Identity identity) {
		for (ReCiterArticle reCiterArticle : reCiterArticles) {
			if (strategyContext instanceof TargetAuthorStrategyContext) {
				((TargetAuthorStrategyContext) strategyContext).executeStrategy(reCiterArticle, identity);
			}
		}
	}

	@Override
	public void runSelectionStrategy(Map<Long, ReCiterCluster> clusters, Identity identity) {
		// Select clusters that are similar to the target author.
		selectClusters(clusters, identity);

		// If no cluster ids are selected, select the cluster with the first name and middle name matches.
		selectClustersFallBack(clusters, identity);

		// Reassign individual article that are similar to the target author. 
		reAssignArticles(strategyContexts, clusters, identity);
	}

	/**
	 * Selecting clusters based on evidence types.
	 * 
	 * @param clusters Clusters formed in Phase I clustering.
	 * @param identity Target author.
	 * 
	 * @return A set of cluster ids that are selected because of the cluster's
	 * similarity to target author.
	 */
	public void selectClusters(Map<Long, ReCiterCluster> clusters, Identity identity) {

		Set<Long> selectedClusterIds = new HashSet<Long>();
		for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
			long clusterId = entry.getKey();
			List<ReCiterArticle> reCiterArticles = entry.getValue().getArticleCluster();

			if (strategyParameters.isEmail()) {
				double emailStrategyScore = ((TargetAuthorStrategyContext) emailStrategyContext).executeStrategy(reCiterArticles, identity);
				if (emailStrategyScore > 0) {
					selectedClusterIds.add(clusterId);
				}
			}

			if (strategyParameters.isDepartment()) {
				double departmentStrategyScore = ((TargetAuthorStrategyContext) departmentStringMatchStrategyContext).executeStrategy(reCiterArticles, identity);
				if (departmentStrategyScore > 0) {
					selectedClusterIds.add(clusterId);
				}
			}

			if (strategyParameters.isKnownRelationship()) {
				double knownRelationshipScore = ((TargetAuthorStrategyContext) knownRelationshipsStrategyContext).executeStrategy(reCiterArticles, identity);
				if (knownRelationshipScore > 0) {
					selectedClusterIds.add(clusterId);
				}
			}

			if (strategyParameters.isAffiliation()) {
				double affiliationScore = ((TargetAuthorStrategyContext)affiliationStrategyContext).executeStrategy(reCiterArticles, identity);
				if (affiliationScore > 0) {
					selectedClusterIds.add(clusterId);
				}
			}
		}
		this.selectedClusterIds = selectedClusterIds;
	}

	public void selectClustersFallBack(Map<Long, ReCiterCluster> clusters, Identity identity) {
		if (selectedClusterIds.size() == 0) {
			for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
				for (ReCiterArticle reCiterArticle : entry.getValue().getArticleCluster()) {
					for (ReCiterAuthor reCiterAuthor : reCiterArticle.getArticleCoAuthors().getAuthors()) {

						boolean isMiddleNameMatch = StringUtils.equalsIgnoreCase(
								reCiterAuthor.getAuthorName().getMiddleInitial(), identity.getPrimaryName().getMiddleInitial());

						boolean isFirstNameMatch = StringUtils.equalsIgnoreCase(
								reCiterAuthor.getAuthorName().getFirstName(), identity.getPrimaryName().getFirstName());

						if (isMiddleNameMatch && isFirstNameMatch) {
							selectedClusterIds.add(entry.getKey());
						}
					}
				}
			}
		}
	}

	/**
	 * Reassign individual articles that are similar to the target author based
	 * on a given instance of strategy context.
	 * @param strategyContexts list of strategy context that are going to be used to reassign the article.
	 * @param clusters current state of the clusters.
	 * @param identity target author.
	 */
	public void reAssignArticles(List<StrategyContext> strategyContexts, Map<Long, ReCiterCluster> clusters, Identity identity) {
		for (StrategyContext strategyContext : strategyContexts) {
			handleStrategyContext(strategyContext, clusters, identity);
		}
	}

	/**
	 * Handler for target author specific strategy context.
	 * @param targetAuthorStrategyContext
	 * @param clusters
	 * @param identity
	 */
	public void handleTargetAuthorStrategyContext(
			TargetAuthorStrategyContext targetAuthorStrategyContext, 
			Map<Long, ReCiterCluster> clusters, 
			Identity identity) {

		// Map of cluster ids to ReCiterArticle objects. Keep tracks of the new cluster ids that these
		// ReCiterArticle objects will be placed at the end of the below loop.
		Map<Long, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Long, List<ReCiterArticle>>();

		for (long clusterId : selectedClusterIds) {
			for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.contains(entry.getKey())) {

					// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
					Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
					while (iterator.hasNext()) {
						ReCiterArticle otherReCiterArticle = iterator.next();

						if (targetAuthorStrategyContext.executeStrategy(otherReCiterArticle, identity) > 0) {
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
		// Now move the selected article to its cluster where the score returns greater than 0
		// using clusterIdToReCiterArticleList map.
		for (Entry<Long, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				clusters.get(entry.getKey()).add(article);
			}
		}
	}

	/**
	 * Handler for ReCiterArticle specific strategy context.
	 * @param reCiterArticleStrategyContext
	 * @param clusters
	 * @param identity
	 */
	public void handleReCiterArticleStrategyContext(
			ReCiterArticleStrategyContext reCiterArticleStrategyContext,
			Map<Long, ReCiterCluster> clusters, 
			Identity identity) {

		Map<Long, List<ReCiterArticle>> clusterIdToReCiterArticleList = new HashMap<Long, List<ReCiterArticle>>();

		for (long clusterId : selectedClusterIds) {
			for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
				// Do not iterate through the selected cluster ids's articles.
				if (!selectedClusterIds.contains(entry.getKey())) {
					for (ReCiterArticle reCiterArticle : clusters.get(clusterId).getArticleCluster()) {

						// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
						Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
						while (iterator.hasNext()) {
							ReCiterArticle otherReCiterArticle = iterator.next();

							if (reCiterArticleStrategyContext.executeStrategy(reCiterArticle, otherReCiterArticle) > 0) {
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

		// Add article to existing cluster.
		for (Entry<Long, List<ReCiterArticle>> entry : clusterIdToReCiterArticleList.entrySet()) {
			for (ReCiterArticle article : entry.getValue()) {
				clusters.get(entry.getKey()).add(article);
			}
		}
	}

	/**
	 * Handle removal of articles from selected clusters.
	 * @param removeReCiterArticleStrategyContext
	 * @param clusters
	 * @param identity
	 */
	public void handleRemoveReCiterArticleStrategyContext(
			RemoveReCiterArticleStrategyContext removeReCiterArticleStrategyContext,
			Map<Long, ReCiterCluster> clusters,
			Identity identity) {

		ReCiterCluster clusterOfRemovedArticles = new ReCiterCluster();

		for (long clusterId : selectedClusterIds) {
			Iterator<ReCiterArticle> iterator = clusters.get(clusterId).getArticleCluster().iterator();
			while (iterator.hasNext()) {
				ReCiterArticle reCiterArticle = iterator.next();
				double score = removeReCiterArticleStrategyContext.executeStrategy(reCiterArticle, identity);
				if (score > 0) {
					clusterOfRemovedArticles.add(reCiterArticle);
					iterator.remove();
				}
			}
		}

		clusters.put(clusterOfRemovedArticles.getClusterID(), clusterOfRemovedArticles);
	}

	/**
	 * Handle removal of a cluster from selected clusters.
	 * @param removeClusterStrategyContext
	 * @param clusters
	 * @param identity
	 */
	public void handleRemoveClusterStrategyContext(
			RemoveClusterStrategyContext removeClusterStrategyContext,
			Map<Long, ReCiterCluster> clusters,
			Identity identity) {

		Iterator<Long> iterator = selectedClusterIds.iterator();
		while (iterator.hasNext()) {
			long clusterId = iterator.next();
			ReCiterCluster cluster = clusters.get(clusterId);
			double score = removeClusterStrategyContext.executeStrategy(cluster, identity);
			if (score > 0) {
				iterator.remove();
			}
		}
	}


	@Override
	public void handleNonSelectedClusters(
			TargetAuthorStrategyContext strategyContext, 
			Map<Long, ReCiterCluster> clusters, 
			Identity identity) {

		// Map of cluster ids to ReCiterarticle objects. A new cluster that contains previously not selected articles.
		List<ReCiterArticle> movedArticles = new ArrayList<ReCiterArticle>();
		for (Entry<Long, ReCiterCluster> entry : clusters.entrySet()) {
			// Do not iterate through the selected cluster ids's articles.
			if (!selectedClusterIds.contains(entry.getKey())) {

				// Iterate through the remaining final cluster that are not selected in selectedClusterIds.
				Iterator<ReCiterArticle> iterator = entry.getValue().getArticleCluster().iterator();
				while (iterator.hasNext()) {
					ReCiterArticle article = iterator.next();

					if (strategyContext.executeStrategy(article, identity) > 0) {
						movedArticles.add(article);
						iterator.remove();
					}
				}
			}
		}

		slf4jLogger.info("size=" + movedArticles.size());

		// Create a new cluster containing these articles and put its cluster id into the selectedClusterIds.
		if (movedArticles.size() > 0) {
			ReCiterCluster newCluster = new ReCiterCluster();
			newCluster.setClusterOriginator(movedArticles.get(0).getArticleId()); // select a cluster originator.
			newCluster.setArticleCluster(movedArticles);
			clusters.put(newCluster.getClusterID(), newCluster);
			selectedClusterIds.add(newCluster.getClusterID());
			slf4jLogger.info("new cluster=" + newCluster.getClusterID());
		}
	}


	/**
	 * Handler for a generic StrategyContext object.
	 * @param strategyContext
	 * @param clusters
	 * @param identity
	 */
	public void handleStrategyContext(StrategyContext strategyContext, Map<Long, ReCiterCluster> clusters, Identity identity) {
		if (strategyContext instanceof TargetAuthorStrategyContext) {
			handleTargetAuthorStrategyContext((TargetAuthorStrategyContext) strategyContext, clusters, identity);
		} else if (strategyContext instanceof ReCiterArticleStrategyContext) {
			handleReCiterArticleStrategyContext((ReCiterArticleStrategyContext) strategyContext, clusters, identity);
		} else if (strategyContext instanceof RemoveReCiterArticleStrategyContext) {
			handleRemoveReCiterArticleStrategyContext((RemoveReCiterArticleStrategyContext) strategyContext, clusters, identity);
		} else if (strategyContext instanceof RemoveClusterStrategyContext) {
			handleRemoveClusterStrategyContext((RemoveClusterStrategyContext) strategyContext, clusters, identity);
		}
	}

	@Override
	public Set<Long> getSelectedClusterIds() {
		return selectedClusterIds;
	}

	public void setSelectedClusterIds(Set<Long> selectedClusterIds) {
		this.selectedClusterIds = selectedClusterIds;
	}

	public List<StrategyContext> getStrategyContexts() {
		return strategyContexts;
	}

	public StrategyContext getArticleTitleInEnglishStrategyContext() {
		return articleTitleInEnglishStrategyContext;
	}

	public void setArticleTitleInEnglishStrategyContext(StrategyContext articleTitleInEnglishStrategyContext) {
		this.articleTitleInEnglishStrategyContext = articleTitleInEnglishStrategyContext;
	}
}
