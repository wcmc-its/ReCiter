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
package reciter.algorithm.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reciter.algorithm.cluster.clusteringstrategy.article.ClusteringStrategy;
import reciter.algorithm.cluster.clusteringstrategy.article.NameMatchingClusteringStrategy;
import reciter.algorithm.cluster.model.ReCiterCluster;
import reciter.algorithm.cluster.similarity.clusteringstrategy.article.EmailFeatureClusteringStrategy;
import reciter.algorithm.cluster.similarity.clusteringstrategy.article.GrantFeatureClusteringStrategy;
import reciter.algorithm.cluster.similarity.clusteringstrategy.article.BaselineClusteringStrategy;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;

@Slf4j
@Getter
@Setter
public class ReCiterClusterer extends AbstractClusterer {

			private List<ReCiterArticle> reCiterArticles;
	private Identity identity;
	private Map<Long, ReCiterCluster> clusters;
	private ClusteringStrategy clusteringStrategy;
	
	public ReCiterClusterer(Identity identity, List<ReCiterArticle> reCiterArticles) {
		this.reCiterArticles = reCiterArticles;
		this.identity = identity;
		clusters = new HashMap<Long, ReCiterCluster>();
		//clusteringStrategy = new NameMatchingClusteringStrategy(identity);
		clusteringStrategy = new BaselineClusteringStrategy();
	}

	/**
	 * <p>
	 * Phase I clustering.
	 * <p>
	 * Cluster articles together based on the ClusteringStrategy selected.
	 */
	@Override
	public void cluster() {
		log.info("Running ReCiter for: [" + identity.getUid() + "] "
				+ "Number of articles to be clustered:" + reCiterArticles.size());
		//Baseline Clustering Strategy
		clusters = clusteringStrategy.cluster(reCiterArticles);
		log.info("Number of clusters after initial clustering: " + clusters.size());
		log.info("Initial CLustering Strategy results: " + toString());
		
		//Email Clustering Strategy
		clusteringStrategy = new EmailFeatureClusteringStrategy();
		clusters = clusteringStrategy.cluster(clusters);
		log.info("Number of clusters after email strategy clustering: " + clusters.size());
		log.info("email strategy CLustering Strategy results: " + toString());
		
		//Grant Clustering Strategy
		clusteringStrategy = new GrantFeatureClusteringStrategy();
		clusters = clusteringStrategy.cluster(clusters);
		log.info("Number of clusters after grant strategy clustering: " + clusters.size());
		log.info("grant strategy CLustering Strategy results: " + toString());
	}
	

	@Override
	public void cluster(Set<Long> seedPmids) {
		log.info("Running ReCiter for: [" + identity.getUid() + "] "
				+ "Number of articles to be clustered:" + reCiterArticles.size() + " initial seeds=" + seedPmids);
		clusters = clusteringStrategy.cluster(reCiterArticles, seedPmids);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Entry<Long, ReCiterCluster> cluster : clusters.entrySet()) {
			sb.append("\nCluster id: " + cluster.getKey() + "= ," + cluster.getValue().getClusterInfo());
			for (ReCiterArticle reCiterArticle : cluster.getValue().getArticleCluster()) {
				sb.append(reCiterArticle.getArticleId() + ", ");
			}
		}
		return sb.toString();
	}
}
