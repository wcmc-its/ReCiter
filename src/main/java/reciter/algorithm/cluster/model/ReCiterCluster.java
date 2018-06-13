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
package reciter.algorithm.cluster.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;
import reciter.model.article.ReCiterArticle;
import reciter.model.article.ReCiterArticleGrant;
import reciter.model.article.ReCiterAuthor;
import reciter.model.identity.Identity;

public class ReCiterCluster implements Comparable<ReCiterCluster>{

	/**
	 * Cluster Id.
	 */
	private final long clusterId;
	
	/**
	 * Atomic integer counter
	 */
	private static AtomicInteger clusterIDCounter = new AtomicInteger(0);

	/**
	 * List of articles in its cluster.
	 */
	private List<ReCiterArticle> articleCluster;

	/**
	 * Cluster originator.
	 */
	private long clusterOriginator;
	
	private boolean isSelected;

	private String clusterInfo = "";
	
	public static class MeshTermCount {
		private String mesh;
		private long count;
		
		public MeshTermCount() {}
		
		public String getMesh() {
			return mesh;
		}
		public void setMesh(String mesh) {
			this.mesh = mesh;
		}
		public long getCount() {
			return count;
		}
		public void setCount(long count) {
			this.count = count;
		}
	}
	
	private List<MeshTermCount> meshTermCounts;
	
	/**
	 * Returns a list of pmids of articles in this cluster.
	 */
	public Set<Long> getPmidSet() {
		Set<Long> pmidSet = new HashSet<Long>();
		for (ReCiterArticle reCiterArticle : articleCluster) {
			pmidSet.add(reCiterArticle.getArticleId());
		}
		return pmidSet;
	}

	/**
	 * Calculates the similarity of this cluster with another cluster.
	 * @param c
	 * @return
	 */
	public double similar(ReCiterCluster c) {
		double maxSim = -1;
		for (ReCiterArticle article : c.getArticleCluster()) {
			double sim = contentSimilarity(article);
			if (sim > maxSim) {
				maxSim = sim;
			}
		}
		return maxSim;
	}

	/**
	 * Calculates the similarity between an ReCiterArticle and a ReCiterCluster.
	 * @param currentArticle
	 * @return
	 */
	public double contentSimilarity(ReCiterArticle currentArticle) {
		double similarityScore = -1;
		for (ReCiterArticle article : articleCluster) {
//			double sim = article.getDocumentSimmilarity().documentSimilarity(article, currentArticle);
			double sim = 0;
			if (sim > similarityScore) {
				similarityScore = sim;
			}
		}
		return similarityScore;
	}

	/**
	 * Checks whether this cluster contains an author who has a name variant
	 * to the target author.
	 * @param type
	 * @param initials
	 * @return
	 */
	public boolean containsNameVariant(ReCiterAuthor targetAuthor) {
//		for (ReCiterArticle article : articleCluster) {
//			for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
//				if (targetAuthor.getAuthorName().isNameVariant(author.getAuthorName())) {
//					return true;
//				}
//			}
//		}
		return false;
	}

	/**
	 * Get number of matching coauthors except the target author for each of the articles in this cluster 
	 * with the <code>currentArticle</code> and return the max number of matching coauthors out of the articles in this cluster.
	 * @param currentArticle current article being compared.
	 * @return the max number of matching coauthors.
	 * 
	 * TODO revise the if statement logic.
	 */
	public int getMatchingCoauthorCount(ReCiterArticle currentArticle, Identity targetAuthor) {
		int matchingCoauthorCount = 0;
		// For each article in this cluster.
		for (ReCiterArticle article : articleCluster) {
			// For each author in this article.
			for (ReCiterAuthor author : article.getArticleCoAuthors().getAuthors()) {
				// For each author in the currentArticle.
				for (ReCiterAuthor currentAuthor : currentArticle.getArticleCoAuthors().getAuthors()) {
					
					// Check if the names match.
					if ((currentAuthor.getAuthorName().isFullNameMatch(author.getAuthorName()) 
							&& !currentAuthor.getAuthorName().firstInitialLastNameMatch(targetAuthor.getPrimaryName())
							&& !author.getAuthorName().firstInitialLastNameMatch(targetAuthor.getPrimaryName()))) {
						
						matchingCoauthorCount += 1;
						
					} else if (currentAuthor.getAffiliation() != null && author.getAffiliation() != null) {
						if (currentAuthor.getAuthorName().firstInitialLastNameMatch(author.getAuthorName())) {
							
//							slf4jLogger.debug(currentAuthor.getAuthorName() + " " + author.getAuthorName());
								matchingCoauthorCount += 1;
							}
					}
				}
			}
		}
		return matchingCoauthorCount;
	}

	public void add(ReCiterArticle article) {
		this.articleCluster.add(article);
	}

	public void addAll(List<ReCiterArticle> reCiterArticles) {
		articleCluster.addAll(reCiterArticles);
	}

	public List<ReCiterArticle> getArticleCluster() {
		return this.articleCluster;
	}

	public void setArticleCluster(List<ReCiterArticle> articleCluster) {
		this.articleCluster = articleCluster;
	}

	public ReCiterCluster() {
		this.clusterId = clusterIDCounter.incrementAndGet();
		this.articleCluster = new ArrayList<ReCiterArticle>();
	}

	public long getClusterID() {
		return clusterId;
	}
	
	public static AtomicInteger getClusterIDCounter() {
		return clusterIDCounter;
	}

	public long getClusterOriginator() {
		return clusterOriginator;
	}

	public void setClusterOriginator(long clusterOriginator) {
		this.clusterOriginator = clusterOriginator;
	}

	public String getClusterInfo() {
		return clusterInfo;
	}

	public void setClusterInfo(String clusterInfo) {
		this.clusterInfo = clusterInfo;
	}

	public List<MeshTermCount> getMeshTermCounts() {
		return meshTermCounts;
	}

	public void setMeshTermCounts(List<MeshTermCount> meshTermCounts) {
		this.meshTermCounts = meshTermCounts;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	@Override
	public int compareTo(ReCiterCluster o) {
		boolean emailMatch = false;
		for(ReCiterArticle reCiterArticle: o.getArticleCluster()) {
			for(ReCiterAuthor authoro: reCiterArticle.getArticleCoAuthors().getAuthors()) {
				if(authoro.getValidEmail() != null && !authoro.getValidEmail().isEmpty()) {
					emailMatch = this.articleCluster.stream().anyMatch(article -> article.getArticleCoAuthors().getAuthors().stream().anyMatch(author -> author.getValidEmail() != null && !author.getValidEmail().isEmpty() &&
							StringUtils.equalsIgnoreCase(author.getValidEmail(), authoro.getValidEmail())));
					if(emailMatch) {
						return 1;
					}
				}
			}
		}
		return 0;
	}
	
	/**
	 * @param o The ReCiterCluster to compare to
	 * @param comparisonType what kind of comparison happening e.g. email or grants etc.
	 * @return 1 if equal or 0 if not
	 */
	public int compareTo(ReCiterCluster o, String comparisonType) {
		boolean match = false;
		for(ReCiterArticle reCiterArticle: o.getArticleCluster()) {
			for(ReCiterArticleGrant granto: reCiterArticle.getGrantList()) {
				if(granto.getSanitizedGrantID() != null && !granto.getSanitizedGrantID().isEmpty()) {
					match = this.articleCluster.stream().anyMatch(articleList -> articleList.getGrantList().stream().anyMatch(grant -> grant.getSanitizedGrantID() != null && !grant.getSanitizedGrantID().isEmpty() &&
							StringUtils.equalsIgnoreCase(grant.getSanitizedGrantID().trim(), granto.getSanitizedGrantID().trim())));
					if(match) {
						return 1;
					}
				}
				
			}
		}
		return 0;
	}
	

}
