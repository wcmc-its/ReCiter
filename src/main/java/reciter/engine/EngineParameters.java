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
package reciter.engine;

import java.util.List;
import java.util.Map;

import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;

public class EngineParameters {

	private Identity identity;
	private List<PubMedArticle> pubMedArticles;
	private List<ScopusArticle> scopusArticles;
	private List<Long> knownPmids;
	private static Map<String, Long> meshCountMap;
	
	public Identity getIdentity() {
		return identity;
	}
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	public List<PubMedArticle> getPubMedArticles() {
		return pubMedArticles;
	}
	public void setPubMedArticles(List<PubMedArticle> pubMedArticles) {
		this.pubMedArticles = pubMedArticles;
	}
	public List<ScopusArticle> getScopusArticles() {
		return scopusArticles;
	}
	public void setScopusArticles(List<ScopusArticle> scopusArticles) {
		this.scopusArticles = scopusArticles;
	}
	public static Map<String, Long> getMeshCountMap() {
		return meshCountMap;
	}
	public static void setMeshCountMap(Map<String, Long> meshCountMap) {
		EngineParameters.meshCountMap = meshCountMap;
	}
	public List<Long> getKnownPmids() {
		return knownPmids;
	}
	public void setKnownPmids(List<Long> knownPmids) {
		this.knownPmids = knownPmids;
	}
}
