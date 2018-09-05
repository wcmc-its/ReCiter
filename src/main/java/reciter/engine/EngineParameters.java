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

import reciter.database.dynamodb.model.ScienceMetrix;
import reciter.database.dynamodb.model.ScienceMetrixDepartmentCategory;
import reciter.model.article.ReCiterArticle;
import reciter.model.identity.Identity;
import reciter.model.pubmed.PubMedArticle;
import reciter.model.scopus.ScopusArticle;

import java.util.List;
import java.util.Map;

public class EngineParameters {

    private static Map<String, Long> meshCountMap;
    private static Map<String, List<String>> afiliationNameToAfidMap;
    private static List<ScienceMetrix> scienceMetrixJournals;
    private static List<ScienceMetrixDepartmentCategory> scienceMetrixDepartmentCategories;
    private Identity identity;
    private List<PubMedArticle> pubMedArticles;
    private List<ScopusArticle> scopusArticles;
    private List<ReCiterArticle> reciterArticles;
    private List<Long> knownPmids;
    private List<Long> rejectedPmids;
    private double totalStandardzizedArticleScore;

    public static Map<String, Long> getMeshCountMap() {
        return meshCountMap;
    }

    public static void setMeshCountMap(Map<String, Long> meshCountMap) {
        EngineParameters.meshCountMap = meshCountMap;
    }

    public static Map<String, List<String>> getAfiliationNameToAfidMap() {
        return afiliationNameToAfidMap;
    }

    public static void setAfiliationNameToAfidMap(Map<String, List<String>> afiliationNameToAfidMap) {
        EngineParameters.afiliationNameToAfidMap = afiliationNameToAfidMap;
    }

    public static List<ScienceMetrix> getScienceMetrixJournals() {
		return scienceMetrixJournals;
	}

	public static void setScienceMetrixJournals(List<ScienceMetrix> scienceMetrixJournals) {
		EngineParameters.scienceMetrixJournals = scienceMetrixJournals;
	}

	public static List<ScienceMetrixDepartmentCategory> getScienceMetrixDepartmentCategories() {
		return scienceMetrixDepartmentCategories;
	}

	public static void setScienceMetrixDepartmentCategories(
			List<ScienceMetrixDepartmentCategory> scienceMetrixDepartmentCategories) {
		EngineParameters.scienceMetrixDepartmentCategories = scienceMetrixDepartmentCategories;
	}

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

    public List<ReCiterArticle> getReciterArticles() {
        return reciterArticles;
    }

    public void setReciterArticles(List<ReCiterArticle> reciterArticles) {
        this.reciterArticles = reciterArticles;
    }

    public List<Long> getKnownPmids() {
        return knownPmids;
    }

    public void setKnownPmids(List<Long> knownPmids) {
        this.knownPmids = knownPmids;
    }

    public List<Long> getRejectedPmids() {
        return rejectedPmids;
    }

    public void setRejectedPmids(List<Long> rejectedPmids) {
        this.rejectedPmids = rejectedPmids;
    }

    public double getTotalStandardzizedArticleScore() {
        return totalStandardzizedArticleScore;
    }

    public void setTotalStandardzizedArticleScore(double totalStandardzizedArticleScore) {
        this.totalStandardzizedArticleScore = totalStandardzizedArticleScore;
    }
}
