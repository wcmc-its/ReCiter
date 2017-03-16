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
package reciter.model.pubmed;

import java.util.List;

public class MedlineCitation {
	
	private enum MedlineCitationOwner {
		NLM,
		NASA,
		PIP,
		KIE,
		HSR,
		HMD,
		SIS,
		NOTNLM
	}
	
	private enum MedlineCitationStatus {
		COMPLETED,
		IN_PROCESS,
		PUBMED_NOT_MEDLINE,
		IN_DATA_REVIEW,
		PUBLISHER,
		MEDLINE,
		OLDMEDLINE
	}
	
	public MedlineCitation() {}
	
	private MedlineCitationPMID medlineCitationPMID;
	private MedlineCitationOwner medlineCitationOwner;
	private MedlineCitationStatus medlineCitationStatus;
	private MedlineCitationVersionDate medlineCitationVersionDate;
	private MedlineCitationVersionID medlineCitationVersionID;
	
	private MedlineCitationDate dateCreated;
	private MedlineCitationDate dateCompleted;
	private MedlineCitationDate dateRevised;
	
	private MedlineCitationArticle article;
	private List<MedlineCitationMeshHeading> meshHeadingList;
	private MedlineCitationKeywordList keywordList;
	private List<MedlineCitationCommentsCorrections> commentsCorrectionsList;
	
	public MedlineCitationPMID getMedlineCitationPMID() {
		return medlineCitationPMID;
	}
	public void setMedlineCitationPMID(MedlineCitationPMID medlineCitationPMID) {
		this.medlineCitationPMID = medlineCitationPMID;
	}
	public MedlineCitationArticle getArticle() {
		return article;
	}
	public void setArticle(MedlineCitationArticle article) {
		this.article = article;
	}
	public List<MedlineCitationMeshHeading> getMeshHeadingList() {
		return meshHeadingList;
	}
	public void setMeshHeadingList(List<MedlineCitationMeshHeading> meshHeadingList) {
		this.meshHeadingList = meshHeadingList;
	}
	public MedlineCitationKeywordList getKeywordList() {
		return keywordList;
	}
	public void setKeywordList(MedlineCitationKeywordList keywordList) {
		this.keywordList = keywordList;
	}
	public MedlineCitationDate getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(MedlineCitationDate dateCreated) {
		this.dateCreated = dateCreated;
	}
	public MedlineCitationDate getDateCompleted() {
		return dateCompleted;
	}
	public void setDateCompleted(MedlineCitationDate dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	public MedlineCitationDate getDateRevised() {
		return dateRevised;
	}
	public void setDateRevised(MedlineCitationDate dateRevised) {
		this.dateRevised = dateRevised;
	}
	public List<MedlineCitationCommentsCorrections> getCommentsCorrectionsList() {
		return commentsCorrectionsList;
	}
	public void setCommentsCorrectionsList(List<MedlineCitationCommentsCorrections> commentsCorrectionsList) {
		this.commentsCorrectionsList = commentsCorrectionsList;
	}
}
